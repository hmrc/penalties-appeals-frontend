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
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.crime.{HasCrimeBeenReportedToPolicePage, WhenDidCrimeHappenPage}

import java.time.LocalDate
import scala.concurrent.Future

class CrimeReasonControllerSpec extends SpecBase {
  val whenDidCrimeHappenPage: WhenDidCrimeHappenPage = injector.instanceOf[WhenDidCrimeHappenPage]
  val hasCrimeBeenReportedPage: HasCrimeBeenReportedToPolicePage = injector.instanceOf[HasCrimeBeenReportedToPolicePage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: CrimeReasonController = new CrimeReasonController(
      whenDidCrimeHappenPage,
      hasCrimeBeenReportedPage,
      mainNavigator
    )(authPredicate, dataRequiredAction, appConfig, mcc)
  }
  

  "CrimeReasonController" should {
    "onPageLoadForWhenCrimeHappened" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenCrimeHappened(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.dateOfCrime -> "2021-01-01")))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenCrimeHappened" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to has crime been reported page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.dateOfCrime).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          await(result).session.get(SessionKeys.dateOfCrime).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": "what",
                  | "date.month": "is",
                  | "date.year": "this"
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 31,
                  | "date.month": 2,
                  | "date.year": 2021
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 124356,
                  | "date.month": 432567,
                  | "date.year": 3124567
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForHasCrimeBeenReported" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForHasCrimeBeenReported(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.hasCrimeBeenReportedToPolice -> "unknown")))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-3").get(0).hasAttr("checked") shouldBe true
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForHasCrimeBeenReported" should {

      "user submits the form" when {
        "the validation is performed against possible values - redirect on success and set the session key value" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "yes"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
            await(result).session.get(SessionKeys.hasCrimeBeenReportedToPolice).get shouldBe "yes"
          }

        "the validation is performed against possible values - value does not appear in options list" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
            contentAsString(result) should include("Tell us if the police have been told about the crime")
          }

        "the validation is performed against an empty value - value is an empty string" in
          new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
            contentAsString(result) should include("Tell us if the police have been told about the crime")
          }
      }

      "return 500" when {
        "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequest.withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}
