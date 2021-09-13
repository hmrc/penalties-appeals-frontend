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
import views.html.reasonableExcuseJourneys.technicalIssues.TechnologyIssuesDatePage
import java.time.{LocalDate, LocalDateTime}

import org.jsoup.nodes.Document

import scala.concurrent.Future

class TechnicalIssuesReasonControllerSpec extends SpecBase {
  val whenDidTechnologyIssuesBeginPage: TechnologyIssuesDatePage = injector.instanceOf[TechnologyIssuesDatePage]

  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {
    reset(mockAuthConnector)

    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)
    val controller = new TechnicalIssuesReasonController(whenDidTechnologyIssuesBeginPage, mainNavigator, errorHandler)(
      authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
      2020, 2, 1, 0, 0, 0))

  }

  "onPageLoadForWhenTechnologyIssuesBegan" should {
    "the user is authorised" must {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01")))
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onPageLoadForWhenTechnologyIssuesEnded" should {
    "the user is authorised" must {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01")))
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(SessionKeys.whenDidTechnologyIssuesEnd -> "2021-01-01",
                       SessionKeys.whenDidTechnologyIssuesBegin -> "2020-12-31")))
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "user hasn't entered the start date for the technical issues" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForWhenTechnologyIssuesBegan" should {
    "the user is authorised" must {
      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to next page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
          Json.parse(
            """
              |{
              | "date.day": 1,
              | "date.month": 2,
              | "date.year": 2021
              |}
              |""".stripMargin))))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url
        await(result).session.get(SessionKeys.whenDidTechnologyIssuesBegin).get shouldBe LocalDate.of(
          2021, 2, 1).toString
      }

      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
          Json.parse(
            """
              |{
              | "date.day": 1,
              | "date.month": 2,
              | "date.year": 2021
              |}
              |""".stripMargin))))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
        await(result).session.get(SessionKeys.whenDidTechnologyIssuesBegin).get shouldBe LocalDate.of(
          2021, 2, 1).toString
      }

      "return 400 (BAD_REQUEST)" when {

        "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
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
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForWhenTechnologyIssuesEnded" should {
    "the user is authorised" must {
      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
          )
          .withJsonBody(
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
        await(result).session.get(SessionKeys.whenDidTechnologyIssuesEnd).get shouldBe LocalDate.of(
          2021, 2, 1).toString
      }

      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
          )
          .withJsonBody(
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
        await(result).session.get(SessionKeys.whenDidTechnologyIssuesEnd).get shouldBe LocalDate.of(
          2021, 2, 1).toString
      }

      "return 400 (BAD_REQUEST)" when {

        "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(
              SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
            )
            .withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(
              SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
            )
            .withJsonBody(
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
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(
              SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
            )
            .withJsonBody(
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

        "passed a date that is earlier than the start date" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(
              SessionKeys.whenDidTechnologyIssuesBegin -> "2021-01-01"
            )
            .withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 31,
                  | "date.month": 12,
                  | "date.year": 2020
                  |}
                  |""".stripMargin))))
          status(result) shouldBe BAD_REQUEST
        }
      }

      "return 500 (ISE)" when {
        "the user hasn't entered a start date for the technical issues" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 1,
                  | "date.month": 1,
                  | "date.year": 2021
                  |}
                  |""".stripMargin))))
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

}
