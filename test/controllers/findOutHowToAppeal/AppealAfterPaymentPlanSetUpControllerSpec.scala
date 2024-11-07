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
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.ArgumentCaptor
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import services.monitoring.JsonAuditModel
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SessionKeys
import views.html.findOutHowToAppeal.AppealAfterPaymentPlanSetUpPage

import scala.concurrent.{ExecutionContext, Future}

class AppealAfterPaymentPlanSetUpControllerSpec extends SpecBase {
  val AppealAfterPaymentPlanSetUpPage: AppealAfterPaymentPlanSetUpPage = injector.instanceOf[AppealAfterPaymentPlanSetUpPage]
  val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], extraSessionData: JsObject = Json.obj()) {
    val sessionAnswers: UserAnswers = userAnswers(correctUserAnswers ++ extraSessionData)
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)
    reset(mockAuditService)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockSessionService.getUserAnswers(any()))
      .thenReturn(Future.successful(Some(sessionAnswers)))

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller = new AppealAfterPaymentPlanSetUpController(AppealAfterPaymentPlanSetUpPage, errorHandler)(mcc, mockAppConfig,
      authPredicate, dataRetrievalAction, mainNavigator, mockAuditService,
      mockSessionService, config, ec)
  }

  "AppealAfterPaymentPlanSetUpController" should {

    "onPageLoad" when {
      "the user is authorised" must {
        "return 200 (OK) and the correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoad()(fakeRequestConverter(fakeRequest = fakeRequest
            .withFormUrlEncodedBody("value" -> "no")))
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
            .withFormUrlEncodedBody("value" -> "no")))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue.data.decryptedValue shouldBe correctUserAnswers ++ Json.obj(SessionKeys.appealAfterPaymentPlanSetUp -> "no")
        }
      }
      "the user is unauthorised" when {
        "return 400 (BAD_REQUEST) when a no option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(fakeRequest = fakeRequest
            .withFormUrlEncodedBody("value" -> "")))
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
      "auditRadioOptionsForAfterPaymentPlanSetup" should {

        "send an audit with userWentToSetUpTTP = yes" when {
          def auditTestDidPaymentSetUpPlan(userWentToSetUpTTP: String): Unit = {
            "The user goes to pay their vat return" in new Setup(AuthTestModels.successfulAuthResult){
              val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
              val auditDetails: JsValue = Json.obj(
                "taxIdentifier" -> "123456789",
                "identifierType" -> "VRN",
                "chargeReference" -> "123456789",
                "amountToBePaidInPence" -> "10000",
                "canUserPay" -> "no",
                "userWentToSetUpTTP" -> "yes"
              )
              controller.auditRadioOptionsForAfterPaymentPlanSetup(userWentToSetUpTTP)(HeaderCarrier(), userRequestAfterPaymentPlanSetupWithCorrectKeys)
              verify(mockAuditService, times(1)).audit(auditCapture.capture())(any[HeaderCarrier](),
                any[ExecutionContext], any())
              auditCapture.getValue.transactionName shouldBe "penalties-find-out-how-to-appeal-set-up-ttp"
              auditCapture.getValue.auditType shouldBe "PenaltyFindOutHowToAppealSetUpTimeToPay"
              auditCapture.getValue.detail shouldBe auditDetails
            }
          }
          def auditTestDidNotPaymentSetUpPlan(userWentToSetUpTTP: String): Unit = {
            "The user does not go to pay their vat return" in new Setup(AuthTestModels.successfulAuthResult){
              val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
              val auditDetails: JsValue = Json.obj(
                "taxIdentifier" -> "123456789",
                "identifierType" -> "VRN",
                "chargeReference" -> "123456789",
                "amountToBePaidInPence" -> "10000",
                "canUserPay" -> "no",
                "userWentToSetUpTTP" -> "no"
              )
              controller.auditRadioOptionsForAfterPaymentPlanSetup(userWentToSetUpTTP)(HeaderCarrier(), userRequestAfterPaymentPlanSetupWithCorrectKeys)
              verify(mockAuditService, times(1)).audit(auditCapture.capture())(any[HeaderCarrier](),
                any[ExecutionContext], any())
              auditCapture.getValue.transactionName shouldBe "penalties-find-out-how-to-appeal-set-up-ttp"
              auditCapture.getValue.auditType shouldBe "PenaltyFindOutHowToAppealSetUpTimeToPay"
              auditCapture.getValue.detail shouldBe auditDetails
            }
          }
          auditTestDidPaymentSetUpPlan("yes")
          auditTestDidNotPaymentSetUpPlan("no")
        }
      }
    }
  }
}
