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
import config.featureSwitches.ShowFullAppealAgainstTheObligation
import helpers.{IsLateAppealHelper, SessionAnswersHelper}
import models.UserRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, session, status}
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.CheckYourAnswersPage

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val page: CheckYourAnswersPage = injector.instanceOf[CheckYourAnswersPage]
  val mockAppealService: AppealService = mock(classOf[AppealService])
  val sessionAnswersHelper: SessionAnswersHelper = injector.instanceOf[SessionAnswersHelper]
  val mockIsLateAppeal:IsLateAppealHelper = mock(classOf[IsLateAppealHelper])

  def fakeRequest(answers: JsObject, fakeRequest: FakeRequest[AnyContent] = fakeRequest): UserRequest[AnyContent] = fakeRequestConverter(answers, fakeRequest)

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], isLateAppeal: Boolean = false) {
    reset(mockAuthConnector)
    reset(mockAppealService)
    reset(mockSessionService)
    reset(mockAppConfig)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    when(mockIsLateAppeal.isAppealLate()(any)).thenReturn(isLateAppeal)
    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(true)
  }

  object Controller extends CheckYourAnswersController(
    page,
    mockAppealService,
    errorHandler,
    sessionAnswersHelper,
    mockIsLateAppeal
  )(stubMessagesControllerComponents(), implicitly, implicitly, implicitly, authPredicate, dataRetrievalAction, dataRequiredAction, checkObligationAvailabilityAction)

  "onPageLoad" should {
    "the user is authorised" must {

      "return OK and correct view - when all answers exist for crime" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(crimeAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for loss of staff" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(lossOfStaffAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(lossOfStaffAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for technical issues" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(techIssuesAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(techIssuesAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - no hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(noHospitalStayAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(noHospitalStayAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - ongoing hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(hospitalOngoingAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(hospitalOngoingAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - ended hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(hospitalEndedAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(hospitalEndedAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for other" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(otherAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(otherAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for bereavement" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(bereavementAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(bereavementAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when it is an appeal against the obligation" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(obligationAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when file is uploaded" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(obligationAnswers))
        status(result) shouldBe OK
      }

      "return OK and correct view - when user is agent submitting an LPP appeal" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(agentLPPAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(agentLPPAnswers))
        status(result) shouldBe OK
      }

      "return ISE" when {
        "the user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      s"return SEE_OTHER" when {
        "the user has not selected a reasonable excuse" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest(crimeNoReasonAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
        }

        "the user has not completed all required answers" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(crimeMissingAnswers))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest(crimeMissingAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
        }

        "the user has not completed the late appeal question when required" in new Setup(AuthTestModels.successfulAuthResult, true) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest(noLateAppealAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }

        s"the feature switch ($ShowFullAppealAgainstTheObligation) is disabled and its an obligation appeal" in new Setup(AuthTestModels.successfulAuthResult) {
          val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
          when(mockSessionService.getUserAnswers(any())).thenReturn(
            Future.successful(Some(userAnswers(answers))))
          when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
          val result: Future[Result] = Controller.onPageLoad()(userRequestWithCorrectKeys.copy(answers = userAnswers(answers)))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
        }
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmit" should {
    "the user is authorised" must {
      "redirect the user to the confirmation page on success" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockAppealService.submitAppeal(any())(any(), any(), any()))
          .thenReturn(Future.successful(Right((): Unit)))
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
        val result: Future[Result] = Controller.onSubmit()(fakeRequest(crimeAnswers))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.AppealConfirmationController.onPageLoad().url
        session(result).get(SessionKeys.previouslySubmittedJourneyId).get shouldBe "1234"
      }

      "redirect the user to the confirmation page on success when it's an obligation reason" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockAppealService.submitAppeal(any())(any(), any(), any()))
          .thenReturn(Future.successful(Right((): Unit)))
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
        val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.AppealConfirmationController.onPageLoad().url
      }

      "redirect the user to an ISE" when {

        "the appeal submission fails" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(INTERNAL_SERVER_ERROR)))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(crimeAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
        }
        "the obligation appeal submission fails" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(INTERNAL_SERVER_ERROR)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
        }
      }

      "redirect the user to the session data incomplete page" when {
        "there is no reasonable excuse selected" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(crimeNoReasonAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(crimeNoReasonAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
        }

        "not all session keys are present" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(crimeMissingAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(crimeMissingAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
        }
      }

      "redirect the user to the service unavailable page" when {
        "the downstream service returns SERVICE_UNAVAILABLE" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(SERVICE_UNAVAILABLE)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ServiceUnavailableController.onPageLoad().url
        }

        "the downstream service returns INTERNAL_SERVER_ERROR" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(INTERNAL_SERVER_ERROR)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
        }

        "the downstream service returns BAD_REQUEST" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(BAD_REQUEST)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
        }

        "the downstream service returns UNPROCESSABLE_ENTITY" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(UNPROCESSABLE_ENTITY)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
        }
      }

      "redirect the user to the duplicate appeal page" when {
        "the downstream service returns CONFLICT" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(any())(any(), any(), any()))
            .thenReturn(Future.successful(Left(CONFLICT)))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          val result: Future[Result] = Controller.onSubmit()(fakeRequest(obligationAnswers))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.DuplicateAppealController.onPageLoad().url
        }
      }

      s"redirect to the appeal by letter page when" +
        s" the feature switch ($ShowFullAppealAgainstTheObligation) is disabled and its an obligation appeal" in new Setup(AuthTestModels.successfulAuthResult) {
        val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
        when(mockSessionService.getUserAnswers(any())).thenReturn(
          Future.successful(Some(userAnswers(answers))))
        when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
        val result: Future[Result] = Controller.onSubmit()(userRequestWithCorrectKeys.copy(answers = userAnswers(answers)))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "changeAnswer" should {
    implicit def stringToRedirectUrl(urlAsString: String): RedirectUrl = RedirectUrl(urlAsString)

    "redirect to the URL provided and add the page name to the session" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockSessionService.getUserAnswers(any()))
        .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
      val result: Future[Result] = Controller.changeAnswer(controllers.routes.ReasonableExcuseController.onPageLoad().url, "ReasonableExcuseSelectionPage")(fakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
    }

    "when the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.changeAnswer(controllers.routes.ReasonableExcuseController.onPageLoad().url, "ReasonableExcuseSelectionPage")(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.changeAnswer(controllers.routes.ReasonableExcuseController.onPageLoad().url, "ReasonableExcuseSelectionPage")(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
