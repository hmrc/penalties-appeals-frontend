/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import base.SpecBase
import models.NormalMode
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.SessionKeys
import views.html.MakingALateAppealPage

import scala.concurrent.Future

class MakingALateAppealControllerSpec extends SpecBase {
  val makingALateAppealPage: MakingALateAppealPage = injector.instanceOf[MakingALateAppealPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    val controller = new MakingALateAppealController(makingALateAppealPage)(mcc, appConfig, authPredicate, dataRequiredAction)
  }

  "onPageLoad" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated textbox when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.lateAppealReason -> "This is a reason.")))
        status(result) shouldBe OK
        val documentParsed = Jsoup.parse(contentAsString(result))
        documentParsed.select("#late-appeal-text").first().text() shouldBe "This is a reason."
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmit" should {
    "the user is authorised" when {
      "redirect the user to the CYA page when an appeal reason has been entered " +
        "- adding the key to the session with a non-empty value" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit()(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "crime"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "late-appeal-text": "Royale with cheese"
                |}
                |""".stripMargin)
          ))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        await(result).session.get(SessionKeys.lateAppealReason).get shouldBe "Royale with cheese"
      }

      "redirect the user to the CYA page when an appeal reason has NOT been entered " +
        "- adding the key to the session with an empty value" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit()(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "crime"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "late-appeal-text": ""
                |}
                |""".stripMargin)
          ))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        await(result).session.get(SessionKeys.lateAppealReason).get shouldBe ""
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
