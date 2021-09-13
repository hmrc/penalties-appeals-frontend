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
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.obligation.OtherRelevantInformationPage
import java.time.LocalDateTime

import org.jsoup.nodes.Document

import scala.concurrent.Future

class AppealAgainstObligationControllerSpec extends SpecBase {
  val otherRelevantInformationPage: OtherRelevantInformationPage = injector.instanceOf[OtherRelevantInformationPage]
  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: AppealAgainstObligationController = new AppealAgainstObligationController(
      otherRelevantInformationPage,
      mainNavigator
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(
      LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "onPageLoad" should {
      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated text when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.otherRelevantInformation -> "this is some relevant information")))
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#other-relevant-information-text").text() shouldBe "this is some relevant information"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

  "onSubmit" should {
    "the user is authorised" must {
      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to file upload when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
          Json.parse(
            """
              |{
              | "other-relevant-information-text": "This is some information"
              |}
              |""".stripMargin))))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
        await(result).session.get(SessionKeys.otherRelevantInformation).get shouldBe "This is some information"
      }

      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
          Json.parse(
            """
              |{
              | "other-relevant-information-text": "This is some information"
              |}
              |""".stripMargin))))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.CheckYourAnswersController.onPageLoad().url
        await(result).session.get(SessionKeys.otherRelevantInformation).get shouldBe "This is some information"
      }

      "return 400 (BAD_REQUEST) when the user does not enter a reason" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
          Json.parse(
            """
              |{
              | "other-relevant-information-text": ""
              |}
              |""".stripMargin))))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
