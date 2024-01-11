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
import models.NormalMode
import models.session.UserAnswers
import navigation.Navigation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.{AppealCoverBothPenaltiesPage, AppealSinglePenaltyPage, PenaltySelectionPage}

import scala.concurrent.{ExecutionContext, Future}

class PenaltySelectionControllerSpec extends SpecBase {
  val mockNavigator: Navigation = mock(classOf[Navigation])
  val penaltySelectionPage: PenaltySelectionPage = injector.instanceOf[PenaltySelectionPage]
  val appealSinglePenaltyPage: AppealSinglePenaltyPage = injector.instanceOf[AppealSinglePenaltyPage]
  val page: PenaltySelectionPage = injector.instanceOf[PenaltySelectionPage]
  val appealCoverBothPenaltiesPage: AppealCoverBothPenaltiesPage = injector.instanceOf[AppealCoverBothPenaltiesPage]
  val multiPenaltyAnswers: JsObject = Json.toJsObject(Map(
    SessionKeys.firstPenaltyAmount -> "4.20",
    SessionKeys.secondPenaltyAmount -> "3000"
  ))
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockNavigator)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
  }

  val controller = new PenaltySelectionController(
    penaltySelectionPage,
    appealCoverBothPenaltiesPage,
    appealSinglePenaltyPage,
    mockNavigator,
    mockSessionService)(stubMessagesControllerComponents(), implicitly, authPredicate, dataRequiredAction, dataRetrievalAction, ec)

  "onPageLoadForPenaltySelection" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers))))
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated radio option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(
          Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers ++
            Json.obj(SessionKeys.doYouWantToAppealBothPenalties -> "yes")))))
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#value").hasAttr("checked") shouldBe true
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForPenaltySelection" should {
    "the user is authorised" when {
      "redirect the user to the single penalty page when no is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers))))
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        when(mockNavigator.nextPage(any(), any(), any(), any())(any()))
          .thenReturn(controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode))
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest
          .withFormUrlEncodedBody("value" -> "no")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
        answerCaptor.getValue.data.decryptedValue shouldBe correctLPPUserAnswers ++ multiPenaltyAnswers ++ Json.obj(SessionKeys.doYouWantToAppealBothPenalties -> "no")
      }

      "redirect the user to the multiple penalty page when yes is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers))))
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        when(mockNavigator.nextPage(any(), any(), any(), any())(any()))
          .thenReturn(controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode))
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(
          fakeRequestConverter(correctLPPUserAnswers ++ multiPenaltyAnswers, fakeRequest
            .withFormUrlEncodedBody("value" -> "yes")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
        answerCaptor.getValue.data.decryptedValue shouldBe correctLPPUserAnswers ++ multiPenaltyAnswers ++ Json.obj(SessionKeys.doYouWantToAppealBothPenalties -> "yes")
      }

      "return a 400 (BAD REQUEST) and show page with error when no option has been selected" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers))))
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(
          fakeRequestConverter(correctLPPUserAnswers ++ multiPenaltyAnswers, fakeRequest
            .withFormUrlEncodedBody("value" -> "")))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onPageLoadForSinglePenaltySelection" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session - first LPP" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctLPPUserAnswers ++ multiPenaltyAnswers))))
        when(mockNavigator.nextPage(any(), any(), any(), any())(any()))
          .thenReturn(controllers.routes.AppealStartController.onPageLoad())
        val result: Future[Result] = controller.onPageLoadForSinglePenaltySelection(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "the user is authorised and has the correct keys in the session - second LPP" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctAdditionalLPPUserAnswers ++ multiPenaltyAnswers))))
        when(mockNavigator.nextPage(any(), any(), any(), any())(any()))
          .thenReturn(controllers.routes.AppealStartController.onPageLoad())
        val result: Future[Result] = controller.onPageLoadForSinglePenaltySelection(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onPageLoadForAppealCoverBothPenalties" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctAdditionalLPPUserAnswers ++ multiPenaltyAnswers))))
        when(mockNavigator.nextPage(any(), any(), any(), any())(any()))
          .thenReturn(controllers.routes.AppealStartController.onPageLoad())
        val result: Future[Result] = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.InternalServerErrorController.onPageLoad().url
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
