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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{status, _}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.CancelVATRegistrationPage

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CancelVATRegistrationControllerSpec extends SpecBase {
  val cancelVATRegistrationPage: CancelVATRegistrationPage = injector.instanceOf[CancelVATRegistrationPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], extraSessionData: JsObject = Json.obj()) {
    val sessionAnswers: UserAnswers = userAnswers(correctUserAnswers ++ extraSessionData)
    reset(mockAuthConnector)
    reset(mockSessionService)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockSessionService.getUserAnswers(any()))
      .thenReturn(Future.successful(Some(sessionAnswers)))

    val controller: CancelVATRegistrationController = new CancelVATRegistrationController(
      cancelVATRegistrationPage,
      mainNavigator,
      mockSessionService
    )(authPredicate, dataRequiredAction, dataRetrievalAction, appConfig, mcc, ec)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
  }

  "CancelVATRegistrationController" should {
    "onPageLoadForCancelVATRegistration" when {
      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "return OK and correct view (pre-populated option when present in session)" in new Setup(
          AuthTestModels.successfulAuthResult, Json.obj(SessionKeys.cancelVATRegistration -> "yes")) {
            val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(userRequestWithCorrectKeys)
            status(result) shouldBe OK
            val documentParsed: Document = Jsoup.parse(contentAsString(result))
            documentParsed.select("#value").hasAttr("checked") shouldBe true
        }
      }
      "the user is unauthorised" must {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForCancelVATRegistration" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing when a valid option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequestConverter(fakeRequest = fakeRequest
            .withFormUrlEncodedBody("value" -> "yes")))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue shouldBe userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.cancelVATRegistration -> "yes"))
        }
      }
      "the user is unauthorised" when {
        "return 400 (BAD_REQUEST) when a no option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequestConverter(fakeRequest = fakeRequest
            .withFormUrlEncodedBody("value" -> "")))
          status(result) shouldBe BAD_REQUEST
        }
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }
        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}
