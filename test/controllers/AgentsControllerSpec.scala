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
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.agents.{WhoPlannedToSubmitVATReturnAgentPage, WhyWasTheReturnSubmittedLateAgentPage}

import scala.concurrent.Future

class AgentsControllerSpec extends SpecBase {
  val whyWasTheReturnSubmittedLatePage: WhyWasTheReturnSubmittedLateAgentPage = injector.instanceOf[WhyWasTheReturnSubmittedLateAgentPage]
  val whoPlannedToSubmitVATReturnPage: WhoPlannedToSubmitVATReturnAgentPage = injector.instanceOf[WhoPlannedToSubmitVATReturnAgentPage]

  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: AgentsController = new AgentsController(
      mainNavigator,
      whyWasTheReturnSubmittedLatePage,
      whoPlannedToSubmitVATReturnPage,
      errorHandler
    )(mcc, appConfig, authPredicate, dataRequiredAction)
  }

  "AgentsController" should {
    "onPageLoadForWhyReturnSubmittedLate" should {
      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.causeOfLateSubmissionAgent -> "agent"))
          )
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
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
            val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "agent"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            await(result).session.get(SessionKeys.causeOfLateSubmissionAgent).get shouldBe "agent"
          }

        "the validation is performed against possible values - value does not appear in options list" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
            val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest.withJsonBody(
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
    "onPageLoadForWhoPlannedToSubmitVATReturn" should {
      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.whoPlannedToSubmitVATReturn -> "client"))
          )
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
        }


        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhoPlannedToSubmitVATReturn" should {
      "user submits the form" when {
        "the validation is performed against possible values (agent) - redirect on success and set the session key value" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "agent"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
            await(result).session.get(SessionKeys.whoPlannedToSubmitVATReturn).get shouldBe "agent"
          }

          "the validation is performed against possible values - value does not appear in options list" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
            contentAsString(result) should include("Tell us who planned to submit the VAT return")
          }

        "the validation is performed against an empty value - value is an empty string" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
            contentAsString(result) should include("Tell us who planned to submit the VAT return")
          }
      }

      "return 500" when {
        "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest.withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
      "user submits the form as client" when {
        "the validation is performed against possible values (client) - redirect on success and set the session key value" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "client"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            await(result).session.get(SessionKeys.whoPlannedToSubmitVATReturn).get shouldBe "client"
          }
      }
    }
  }
}
