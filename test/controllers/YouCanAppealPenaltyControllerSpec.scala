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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{status, _}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.obligation.YouCanAppealPenaltyPage

import scala.concurrent.{ExecutionContext, Future}

class YouCanAppealPenaltyControllerSpec extends SpecBase {
  val youCanAppealPenaltyPage: YouCanAppealPenaltyPage = injector.instanceOf[YouCanAppealPenaltyPage]

  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: YouCanAppealPenaltyController = new YouCanAppealPenaltyController(
      youCanAppealPenaltyPage,
      mainNavigator,
      mockSessionService
    )(mcc, appConfig, authPredicate, dataRequiredAction, dataRetrievalAction, checkObligationAvailabilityAction, ec)
  }

  "YouCanAppealPenaltyController" should {
    "onPageLoad" should {
      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }
      }

      "return OK and correct view (pre-populated option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.youCanAppealThisPenalty -> "yes")))))
        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#value").hasAttr("checked") shouldBe true
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user is unauthorised" when {
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

    "onSubmit" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing when a valid option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "yes")))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.youCanAppealThisPenalty -> "yes")
        }
      }
      "the user is unauthorised" when {
        "return 400 (BAD_REQUEST) when a no option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "")))
          status(result) shouldBe BAD_REQUEST
        }
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
}
