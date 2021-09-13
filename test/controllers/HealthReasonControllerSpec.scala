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
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health.{HasTheHospitalStayEndedPage, WasHospitalStayRequiredPage, WhenDidHealthReasonHappenPage, WhenDidHospitalStayBeginPage}
import viewtils.ConditionalRadioHelper

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class HealthReasonControllerSpec extends SpecBase {

  val hospitalStayPage: WasHospitalStayRequiredPage = injector.instanceOf[WasHospitalStayRequiredPage]
  val whenHealthIssueHappenedPage: WhenDidHealthReasonHappenPage = injector.instanceOf[WhenDidHealthReasonHappenPage]
  val whenDidHospitalStayBeginPage: WhenDidHospitalStayBeginPage = injector.instanceOf[WhenDidHospitalStayBeginPage]
  val conditionalRadioHelper: ConditionalRadioHelper = injector.instanceOf[ConditionalRadioHelper]
  val hasTheHosptialStayEndedPage: HasTheHospitalStayEndedPage = injector.instanceOf[HasTheHospitalStayEndedPage]
  val fakeRequestWithCorrectKeysAndStartDate: FakeRequest[AnyContent] =
    fakeRequestWithCorrectKeys.withSession(SessionKeys.whenHealthIssueStarted -> "2020-01-01")

  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: HealthReasonController = new HealthReasonController(
      mainNavigator,
      hospitalStayPage,
      whenHealthIssueHappenedPage,
      whenDidHospitalStayBeginPage,
      conditionalRadioHelper,
      hasTheHosptialStayEndedPage,
      errorHandler
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
      2020, 2, 1, 0, 0, 0))
  }

  "HealthReasonController" should {
    "onPageLoadForWasHospitalStayRequired" when {

      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated option when present in session) - when answer is no" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate
            .withSession(SessionKeys.wasHospitalStayRequired -> "no")))
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }

    }

    "onSubmitForWasHospitalStayRequired" should {

      "user submits the form" when {
        "the validation is performed against possible values " +
          "- redirects to when health issue happened page and set the session key value" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] =
            controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": "no"
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
          await(result).session.get(SessionKeys.wasHospitalStayRequired).get shouldBe "no"
        }

        "the validation is performed against possible values " +
          "- redirects to when hospital stay begin page and set the session key value" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate
            .withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "yes"
                  |}
                  |""".stripMargin
              )
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url
          await(result).session.get(SessionKeys.wasHospitalStayRequired).get shouldBe "yes"
        }

        "the validation is performed against possible values - value does not appear in options list" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] =
            controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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
          contentAsString(result) should include("Tell us if you or someone else was admitted to hospital")
        }

        "the validation is performed against an empty value - value is an empty string" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] =
            controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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
          contentAsString(result) should include("Tell us if you or someone else was admitted to hospital")
        }
      }

      "return 500 when the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest.withJsonBody(
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

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForWhenHealthReasonHappened" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate
            .withSession(SessionKeys.whenHealthIssueHappened -> "2021-01-01")))
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenHealthReasonHappened" should {

      "the user is authorised" must {

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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
          await(result).session.get(SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "redirects to late appeal page when appeal > 30 days late" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
            2020, 4, 1, 0, 0, 0))
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          await(result).session.get(SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.of(
            2021, 2, 1).toString
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForWhenDidHospitalStayBegin" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate
            .withSession(SessionKeys.whenHealthIssueStarted -> "2021-01-01")))
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenDidHospitalStayBegin" should {

      "the user is authorised" must {

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to has when hospital stay ended page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
          await(result).session.get(SessionKeys.whenHealthIssueStarted).get shouldBe LocalDate.of(
            2021, 2, 1).toString
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
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

      "return 500" when {
        "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest.withJsonBody(
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
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }


  "onPageLoadForHasHospitalStayEnded" should {

    "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
      val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectKeysAndStartDate)
      status(result) shouldBe OK
    }

    "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
      val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate
        .withSession(SessionKeys.hasHealthEventEnded -> "yes")
        .withSession(SessionKeys.whenHealthIssueEnded -> "2022-01-01")))
      status(result) shouldBe OK
      val documentParsed: Document = Jsoup.parse(contentAsString(result))
      documentParsed.select("#hasStayEnded").get(0).hasAttr("checked") shouldBe true
      documentParsed.select("#stayEndDate .govuk-date-input__input").get(0).attr("value") shouldBe "1"
      documentParsed.select("#stayEndDate .govuk-date-input__input").get(1).attr("value") shouldBe "1"
      documentParsed.select("#stayEndDate .govuk-date-input__input").get(2).attr("value") shouldBe "2022"
    }

    "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
      val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user has no start date in the session" in new Setup(AuthTestModels.successfulAuthResult) {
      val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForHasHospitalStayEnded" should {
    "the user is authorised" when {
      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- redirects to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
          fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
          Json.parse(
            """
              |{
              | "hasStayEnded": "yes",
              | "stayEndDate.day": 1,
              | "stayEndDate.month": 2,
              | "stayEndDate.year": 2021
              |}
              |""".stripMargin))))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        await(result).session.get(SessionKeys.whenHealthIssueEnded).get shouldBe LocalDate.of(
          2021, 2, 1).toString
        await(result).session.get(SessionKeys.hasHealthEventEnded).get shouldBe "yes"
      }

      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "redirects to late appeal page when appeal > 30 days late" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
          2020, 4, 1, 0, 0, 0))
        val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
          fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
          Json.parse(
            """
              |{
              | "hasStayEnded": "no"
              |}
              |""".stripMargin
          )
        )))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        await(result).session.get(SessionKeys.whenHealthIssueEnded).isEmpty shouldBe true
        await(result).session.get(SessionKeys.hasHealthEventEnded).get shouldBe "no"
      }

      "return 400 (BAD_REQUEST)" when {
        "the 'yes' option is selected but no date has been entered" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
            2020, 4, 1, 0, 0, 0))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
            Json.parse(
              """
                |{
                | "hasStayEnded": "yes",
                | "stayEndDate.day": "",
                | "stayEndDate.month": "",
                | "stayEndDate.year": ""
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe BAD_REQUEST
        }

        "no option selected" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
            2020, 4, 1, 0, 0, 0))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeysAndStartDate.withJsonBody(
            Json.parse(
              """
                |{
                | "hasStayEnded": ""
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe BAD_REQUEST
        }
      }

      "return 500 (ISE)" when {
        "the user does not have a start date in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
            fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "hasStayEnded": "yes",
                | "stayEndDate.day": 1,
                | "stayEndDate.month": 2,
                | "stayEndDate.year": 2021
                |}
                |""".stripMargin))))

          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
