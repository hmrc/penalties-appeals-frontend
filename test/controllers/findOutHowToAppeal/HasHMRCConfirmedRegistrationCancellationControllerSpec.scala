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

package controllers.findOutHowToAppeal

import base.SpecBase
import models.session.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentCaptor
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.findOutHowToAppeal.HasHMRCConfirmedRegistrationCancellationPage

import scala.concurrent.{ExecutionContext, Future}

class HasHMRCConfirmedRegistrationCancellationControllerSpec extends SpecBase {

  val page: HasHMRCConfirmedRegistrationCancellationPage = injector.instanceOf[HasHMRCConfirmedRegistrationCancellationPage]
  val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], extraSessionData: JsObject = Json.obj()) {
    val sessionAnswers: UserAnswers = userAnswers(correctUserAnswers ++ extraSessionData)
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    when(mockSessionService.getUserAnswers(any()))
      .thenReturn(Future.successful(Some(sessionAnswers)))

    val controller = new HasHMRCConfirmedRegistrationCancellationController(page, errorHandler)(mcc, mockAppConfig,
      authPredicate, dataRequiredAction, dataRetrievalAction, mainNavigator, mockSessionService, config, ec)
  }

  "onPageLoad" when {
    "the user is authorised" must {
      "return 200 (OK) and the correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }
    }

    "the user is unauthorised" must {
      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmit" when {
    "the user is authorised" must {
      "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "- routing when a valid option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(fakeRequest = fakeRequest
          .withFormUrlEncodedBody("value" -> "yes")))
        status(result) shouldBe SEE_OTHER
        answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.hasHMRCConfirmedRegistrationCancellation -> "yes")
      }

      "return 400 (BAD_REQUEST) when a no option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(fakeRequest = fakeRequest
          .withFormUrlEncodedBody("value" -> "")))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
