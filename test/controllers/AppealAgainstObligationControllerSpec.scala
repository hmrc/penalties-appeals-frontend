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
import models.session.UserAnswers
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.obligation.OtherRelevantInformationPage

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AppealAgainstObligationControllerSpec extends SpecBase {
  val otherRelevantInformationPage: OtherRelevantInformationPage = injector.instanceOf[OtherRelevantInformationPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(true)

    val controller: AppealAgainstObligationController = new AppealAgainstObligationController(
      otherRelevantInformationPage,
      mainNavigator,
      mockSessionService
    )(authPredicate, dataRequiredAction, appConfig, mcc, dataRetrievalAction, checkObligationAvailabilityAction, ec)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
  }

  "onPageLoad" should {
    "the user is authorised" must {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated text when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(
          Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.otherRelevantInformation -> "this is some relevant information")))))
        val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#other-relevant-information-text").text() shouldBe "this is some relevant information"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      s"redirect to appeal by letter page when the feature switch ($ShowFullAppealAgainstTheObligation) is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
        val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
        when(mockSessionService.getUserAnswers(any())).thenReturn(
          Future.successful(Some(userAnswers(answers))))
        when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
        val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys.copy(answers = userAnswers(answers)))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
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
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
          "other-relevant-information-text" -> "This is some information")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
        answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.otherRelevantInformation -> "This is some information")
      }

      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        val result: Future[Result] = controller.onSubmit(CheckMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
          "other-relevant-information-text" -> "This is some information")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.CheckYourAnswersController.onPageLoad().url
        answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.otherRelevantInformation -> "This is some information")
      }

      "return 400 (BAD_REQUEST) when the user does not enter a reason" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
          "other-relevant-information-text" -> "")))
        status(result) shouldBe BAD_REQUEST
      }

      "return 400 (BAD_REQUEST) when the user enters an invalid character" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
          "other-relevant-information-text" -> "コし")))
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) should include("The text must contain only letters, numbers and standard special characters")
      }

      s"redirect to appeal by letter page when the feature switch ($ShowFullAppealAgainstTheObligation) is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
        val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
        when(mockSessionService.getUserAnswers(any())).thenReturn(
          Future.successful(Some(userAnswers(answers))))
        when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
        val result: Future[Result] = controller.onSubmit(CheckMode)(fakeRequestConverter(answers, fakeRequest = fakeRequest.withFormUrlEncodedBody(
          "other-relevant-information-text" -> "This is some information")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
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
