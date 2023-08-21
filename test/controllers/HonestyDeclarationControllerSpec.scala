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
import models.PenaltyTypeEnum
import models.PenaltyTypeEnum.{Additional, Late_Payment, Late_Submission}
import models.session.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.monitoring.JsonAuditModel
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SessionKeys
import views.html.HonestyDeclarationPage

import scala.concurrent.{ExecutionContext, Future}

class HonestyDeclarationControllerSpec extends SpecBase {

  val honestyDeclarationPage: HonestyDeclarationPage = injector.instanceOf[HonestyDeclarationPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockConfig: Configuration = mock(classOf[Configuration])

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)
    reset(mockAuditService)
    reset(mockConfig)
    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(true)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: HonestyDeclarationController = new HonestyDeclarationController(
      honestyDeclarationPage,
      errorHandler,
      mainNavigator,
      mockAuditService,
      mockSessionService,
    )(mcc, appConfig, config, authPredicate, dataRequiredAction, dataRetrievalAction, checkObligationAvailabilityAction, ec)
  }

  "onPageLoad" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(
          Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "crime")))))
        val result: Future[Result] = controller.onPageLoad()(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        status(result) shouldBe OK
      }
    }

    s"redirect to appeal by letter page when the feature switch ($ShowFullAppealAgainstTheObligation) is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
      val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
      when(mockSessionService.getUserAnswers(any())).thenReturn(
        Future.successful(Some(userAnswers(answers))))
      when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
      val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys.copy(answers = userAnswers(answers)))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
    }

    "return 500" when {
      "the user hasn't selected an option on the reasonable excuse page" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
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
    "return 303" when {
      def onSubmitTest(reasonableExcuse: String): Unit = {
        s"the reasonable excuse selected is '$reasonableExcuse' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(
            Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> reasonableExcuse)))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmit()(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue shouldBe userAnswers(correctUserAnswers ++ Json.obj(
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.reasonableExcuse -> reasonableExcuse
          ))
        }
      }

      onSubmitTest("crime")
      onSubmitTest("lossOfStaff")
      onSubmitTest("fireOrFlood")
      onSubmitTest("bereavement")
      onSubmitTest("other")
      onSubmitTest("technicalIssues")
      onSubmitTest("health")

    }

    "return 500" when {
      "the user hasn't selected an option on the reasonable excuse page" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))

        val result: Future[Result] = controller.onSubmit()(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
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

  "auditStartOfAppealJourney" should {

    "send an audit" when {
      def auditTest(penaltyType: PenaltyTypeEnum.Value): Unit = {
        s"the appeal is for a $penaltyType" in new Setup(AuthTestModels.successfulAuthResult) {
          val penaltyTypeForAudit: String = penaltyType match {
            case Late_Submission => "LSP"
            case Late_Payment => "LPP1"
            case Additional => "LPP2"
            case _ => s"$penaltyType"
          }
          val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
          val auditDetails: JsValue = Json.obj(
            "startedBy" -> "client",
            "taxIdentifier" -> "123456789",
            "identifierType" -> "VRN",
            "penaltyNumber" -> "123",
            "penaltyType" -> penaltyTypeForAudit
          )
          controller.auditStartOfAppealJourney()(HeaderCarrier(), userRequestWithCorrectKeys.copy(answers = userAnswers(correctUserAnswers.deepMerge(Json.obj(SessionKeys.appealType -> penaltyType)))))
          verify(mockAuditService, times(1)).audit(auditCapture.capture())(any[HeaderCarrier](),
            any[ExecutionContext], any())
          auditCapture.getValue.transactionName shouldBe "penalties-appeal-started"
          auditCapture.getValue.auditType shouldBe "PenaltyAppealStarted"
          auditCapture.getValue.detail shouldBe auditDetails
        }
      }

      def auditAgentTest(penaltyType: PenaltyTypeEnum.Value): Unit = {
        s"the appeal is for a $penaltyType (Agent)" in new Setup(AuthTestModels.successfulAuthResult) {
          val penaltyTypeForAudit: String = penaltyType match {
            case Late_Submission => "LSP"
            case Late_Payment => "LPP1"
            case Additional => "LPP2"
            case _ => s"$penaltyType"
          }
          val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
          val auditDetails: JsValue = Json.obj(
            "startedBy" -> "agent",
            "taxIdentifier" -> "123456789",
            "identifierType" -> "VRN",
            "agentReferenceNumber" -> "BARN123456789",
            "penaltyNumber" -> "123",
            "penaltyType" -> penaltyTypeForAudit
          )
          controller.auditStartOfAppealJourney()(HeaderCarrier(), userRequestWithCorrectKeys.copy(arn = Some("BARN123456789"), answers = userAnswers(correctUserAnswers.deepMerge(Json.obj(SessionKeys.appealType -> penaltyType)))))
          verify(mockAuditService, times(1)).audit(auditCapture.capture())(any(), any(), any())
          auditCapture.getValue.transactionName shouldBe "penalties-appeal-started"
          auditCapture.getValue.auditType shouldBe "PenaltyAppealStarted"
          auditCapture.getValue.detail shouldBe auditDetails
        }
      }

      auditTest(PenaltyTypeEnum.Late_Submission)
      auditTest(PenaltyTypeEnum.Late_Payment)
      auditTest(PenaltyTypeEnum.Additional)

      auditAgentTest(PenaltyTypeEnum.Late_Submission)
      auditAgentTest(PenaltyTypeEnum.Late_Payment)
      auditAgentTest(PenaltyTypeEnum.Additional)
    }
  }
}
