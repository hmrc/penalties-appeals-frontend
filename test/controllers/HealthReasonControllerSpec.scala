/*
 * Copyright 2023 HM Revenue & Customs
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
import models.session.UserAnswers
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class HealthReasonControllerSpec extends SpecBase {

  val hospitalStayPage: WasHospitalStayRequiredPage = injector.instanceOf[WasHospitalStayRequiredPage]
  val whenHealthIssueHappenedPage: WhenDidHealthReasonHappenPage = injector.instanceOf[WhenDidHealthReasonHappenPage]
  val whenDidHospitalStayBeginAndEndPage: WhenDidHospitalStayBeginAndEndPage = injector.instanceOf[WhenDidHospitalStayBeginAndEndPage]
  val hasTheHospitalStayEndedPage: HasTheHospitalStayEndedPage = injector.instanceOf[HasTheHospitalStayEndedPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: HealthReasonController = new HealthReasonController(
      mainNavigator,
      hospitalStayPage,
      whenHealthIssueHappenedPage,
      whenDidHospitalStayBeginAndEndPage,
      hasTheHospitalStayEndedPage,
      errorHandler,
      mockSessionService
    )(authPredicate, dataRequiredAction, dataRetrievalAction, appConfig, ec, mcc)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
  }

  "HealthReasonController" should {
    "onPageLoadForWasHospitalStayRequired" when {

      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated option when present in session) - when answer is no" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.wasHospitalStayRequired -> "no")))))
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "value" -> "no"
          )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.wasHospitalStayRequired -> "no")
        }

        "the validation is performed against possible values " +
          "- redirects to when hospital stay begin page and set the session key value" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "value" -> "yes"
          )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.wasHospitalStayRequired -> "yes")
        }

        "the validation is performed against possible values - value does not appear in options list" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "value" -> "this_is_fake"
          )))
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) should include("There is a problem")
          contentAsString(result) should include("Tell us if you or someone else was admitted to hospital")
        }

        "the validation is performed against an empty value - value is an empty string" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "value" -> ""
          )))
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) should include("There is a problem")
          contentAsString(result) should include("Tell us if you or someone else was admitted to hospital")
        }
      }

      "return 500 when the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest.withFormUrlEncodedBody(
          "value" -> "no"
        ))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(
            Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2021-01-01"))))))
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueHappened -> LocalDate.of(2021, 2, 1))
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "redirects to late appeal page when appeal > 30 days late" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
            2020, 4, 1))
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueHappened -> LocalDate.of(2021, 2, 1))
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
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(
            Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01"))))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 2, 1))
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "what",
                "date.month" -> "is",
                "date.year" -> "this"
              )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "31",
                "date.month" -> "2",
                "date.year" -> "2021"
              )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "124356",
                "date.month" -> "432567",
                "date.year" -> "3124567"
              )))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "return 500" when {
        "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest.withFormUrlEncodedBody("value" -> "no"))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated radio when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
            SessionKeys.hasHealthEventEnded -> "yes"
          )))))
        val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#hasStayEnded").get(0).hasAttr("checked") shouldBe true
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
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
          "- redirects to when hospital stay ended page when in Normal Mode and user answers yes" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "hasStayEnded" -> "yes"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(NormalMode).url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.hasHealthEventEnded -> "yes"
          )
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to when hospital stay ended page when in Check Mode and user answers yes" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(CheckMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "hasStayEnded" -> "yes"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(CheckMode).url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.hasHealthEventEnded -> "yes"
          )
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to CYA page when in Normal Mode and user answers no" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "hasStayEnded" -> "no"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.hasHealthEventEnded -> "no"
          )
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to making a late appeal page when in Normal Mode and user answers no " +
          "(communications date > 30 days ago)" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
              2021, 2, 1))
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
                SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
              )))))
            val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            when(mockSessionService.updateAnswers(answerCaptor.capture()))
              .thenReturn(Future.successful(true))
            val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "hasStayEnded" -> "no"
              )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
            answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
              SessionKeys.hasHealthEventEnded -> "no"
            )
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to CYA page when in Check Mode and user answers no" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(CheckMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "hasStayEnded" -> "no"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.hasHealthEventEnded -> "no"
          )
        }

        "return 400 (BAD_REQUEST)" when {
          "no option selected" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
                SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
              )))))
            when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
              2020, 2, 1))
            val result: Future[Result] = controller.onSubmitForHasHospitalStayEnded(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "hasStayEnded" -> ""
              )))
            status(result) shouldBe BAD_REQUEST
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

    "onPageLoadForWhenDidHospitalStayEnd" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(
            userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01"))))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(
            Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.whenHealthIssueEnded -> LocalDate.parse("2021-01-01")) ++
              Json.obj(SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01"))))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenDidHospitalStayEnd" should {

      "the user is authorised" when {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.whenHealthIssueEnded -> LocalDate.of(2021, 2, 1)
          )
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "redirects to late appeal page when appeal > 30 days late" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
            )))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
            2020, 4, 1))
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.whenHealthIssueStarted -> LocalDate.of(2021, 1, 1),
            SessionKeys.whenHealthIssueEnded -> LocalDate.of(2021, 2, 1)
          )
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
                SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
              )))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "what",
                "date.month" -> "is",
                "date.year" -> "this"
              )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
                SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
              )))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "31",
                "date.month" -> "2",
                "date.year" -> "2021"
              )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(
                SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2021-01-01")
              )))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "124356",
                "date.month" -> "432567",
                "date.year" -> "3124567"
              )))
            status(result) shouldBe BAD_REQUEST
          }
        }

        "return 500 (ISE)" when {
          "the user does not have a start date in the session" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(
              fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
                "date.day" -> "1",
                "date.month" -> "2",
                "date.year" -> "2021"
              )))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }

}
