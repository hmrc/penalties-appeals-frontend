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
import views.html.agents.WhyWasTheReturnSubmittedLateAgentPage

import scala.concurrent.Future

class AgentsControllerSpec extends SpecBase {
  val whyWasTheReturnSubmittedLatePage: WhyWasTheReturnSubmittedLateAgentPage = injector.instanceOf[WhyWasTheReturnSubmittedLateAgentPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: AgentsController = new AgentsController(
      whyWasTheReturnSubmittedLatePage,
      mainNavigator
    )(mcc, appConfig, authPredicate, dataRequiredAction)
  }

  "onPageLoadForWhyReturnSubmittedLate" should {
    "the user is authorised" must {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.causeOfLateSubmissionAgent -> "agent")))
        status(result) shouldBe OK
        val documentParsed = Jsoup.parse(contentAsString(result))
        documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForWhyReturnSubmittedLate" should {
    "user submits the form" when {
      "the validation is performed against possible values - redirect on success and set the session key value" in
        new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": "agent"
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe SEE_OTHER
          //TODO: change to Reasonable excuse selection page
          redirectLocation(result).get shouldBe "#"
          await(result).session.get(SessionKeys.causeOfLateSubmissionAgent).get shouldBe "agent"
        }

      "the validation is performed against possible values - value does not appear in options list" in
        new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": "this_is_fake"
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) should include("There is a problem")
          contentAsString(result) should include("Tell us the reason the return was submitted late")
        }

      "the validation is performed against an empty value - value is an empty string" in
        new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": ""
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) should include("There is a problem")
          contentAsString(result) should include("Tell us the reason the return was submitted late")
        }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest.withJsonBody(
          Json.parse(
            """
              |{
              |   "value": "no"
              |}
              |""".stripMargin
          )
        ))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
