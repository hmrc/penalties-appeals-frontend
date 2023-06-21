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
import models.PenaltyTypeEnum._
import models.appeals.MultiplePenaltiesData
import models.session.UserAnswers
import models.{AppealData, AuthRequest, PenaltyTypeEnum}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import services.AppealService
import services.monitoring.JsonAuditModel
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SessionKeys

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class InitialiseAppealControllerSpec extends SpecBase {
  val mockAppealsService: AppealService = mock(classOf[AppealService])
  val mockConfig: Configuration = mock(classOf[Configuration])

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], expectedContent: Option[MultiplePenaltiesData] = None) {
    reset(mockAuthConnector)
    reset(mockAppealsService)
    reset(mockSessionService)
    reset(mockConfig)
    reset(mockAuditService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockAppealsService.validateMultiplePenaltyDataForEnrolmentKey(any())(any(), any(), any()))
      .thenReturn(Future.successful(expectedContent))
    val controller = new InitialiseAppealController(
      mockAppealsService,
      mockSessionService,
      mockAuditService,
      errorHandler
    )(implicitly, implicitly, mockConfig, implicitly)
  }

  val sampleExamplePenalties: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456790",
    secondPenaltyAmount = 101.02,
    firstPenaltyCommunicationDate = LocalDate.of(2022, 1, 1),
    secondPenaltyCommunicationDate = LocalDate.of(2022, 1, 2)
  )

  "onPageLoad" should {
    "call the penalties backend and handle a failed response" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val result: Result = await(controller.onPageLoad("12345", isLPP = false, isAdditional = false)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8)
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = false, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8)
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal for Additional Penalty (LPP)" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Additional,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8)
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Additional,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

    "call the penalties backend for multiple penalties and handle a success response and" +
      " do not call multiple penalty endpoint when appeal is LSP" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8)
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))

      val result: Future[Result] = controller.onPageLoad("12345", isLPP = false, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

    "call the penalties backend for multiple penalties and handle a success response and add multi penalties details keys " +
      "to session" in new Setup(AuthTestModels.successfulAuthResult, Some(sampleExamplePenalties)) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Additional,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8),
        SessionKeys.firstPenaltyAmount -> "101.01",
        SessionKeys.firstPenaltyChargeReference -> "123456789",
        SessionKeys.secondPenaltyAmount -> "101.02",
        SessionKeys.secondPenaltyChargeReference -> "123456790",
        SessionKeys.firstPenaltyCommunicationDate -> LocalDate.of(2022, 1, 1),
        SessionKeys.secondPenaltyCommunicationDate -> LocalDate.of(2022, 1, 2)
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Additional,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))

      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

  }

  "onPageLoadForObligation" should {
    "call the penalties backend and handle a failed response" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val result: Result = await(controller.onPageLoadForObligation("12345")(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8),
        SessionKeys.isObligationAppeal -> true
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoadForObligation("12345")(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8),
        SessionKeys.isObligationAppeal -> true
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoadForObligation("12345")(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }
    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page for LPP additional" in new Setup(AuthTestModels.successfulAuthResult) {
      val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswersToReturn: UserAnswers = userAnswers(Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Additional,
        SessionKeys.startDateOfPeriod -> LocalDate.of(2020, 1, 1),
        SessionKeys.endDateOfPeriod -> LocalDate.of(2020, 1, 31),
        SessionKeys.penaltyNumber -> "12345",
        SessionKeys.dueDateOfPeriod -> LocalDate.of(2020, 3, 7),
        SessionKeys.dateCommunicationSent -> LocalDate.of(2020, 3, 8),
        SessionKeys.isObligationAppeal -> true
      ))
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Additional,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2020, 1, 31),
        dueDate = LocalDate.of(2020, 3, 7),
        dateCommunicationSent = LocalDate.of(2020, 3, 8)
      )
      when(mockSessionService.updateAnswers(answerCaptor.capture()))
        .thenReturn(Future.successful(true))
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoadForObligation("12345")(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      answerCaptor.getValue.data shouldBe userAnswersToReturn.data
    }
  }

  "auditStartOfAppealJourney" should {
    def appealData(appealType: PenaltyTypeEnum.Value): AppealData = AppealData(
      `type` = appealType,
      startDate = LocalDate.of(2020, 1, 1),
      endDate = LocalDate.of(2020, 1, 31),
      dueDate = LocalDate.of(2020, 3, 7),
      dateCommunicationSent = LocalDate.of(2020, 3, 8)
    )

    "send an audit" when {
      def auditTest(penaltyType: PenaltyTypeEnum.Value): Unit = {
        s"the appeal is for a $penaltyType" in new Setup(AuthTestModels.successfulAuthResult) {
          val penaltyTypeForAudit: String = penaltyType match {
            case Late_Submission => "LSP"
            case Late_Payment => "LPP1"
            case Additional => "LPP2"
            case _ => s"$penaltyType"
          }
          val authRequest = new AuthRequest[AnyContent]("123456789")
          val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
          val auditDetails: JsValue = Json.obj(
            "startedBy" -> "client",
            "taxIdentifier" -> "123456789",
            "identifierType" -> "VRN",
            "penaltyNumber" -> "123456789000",
            "penaltyType" -> penaltyTypeForAudit
          )
          controller.auditStartOfAppealJourney("123456789000", appealData(penaltyType))(implicitly, HeaderCarrier(), authRequest)
          verify(mockAuditService, times(1)).audit(auditCapture.capture())(ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any())
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
          val authRequest = new AuthRequest[AnyContent]("123456789", arn = Some("BARN123456789"))
          val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
          val auditDetails: JsValue = Json.obj(
            "startedBy" -> "agent",
            "taxIdentifier" -> "123456789",
            "identifierType" -> "VRN",
            "agentReferenceNumber" -> "BARN123456789",
            "penaltyNumber" -> "123456789000",
            "penaltyType" -> penaltyTypeForAudit
          )
          controller.auditStartOfAppealJourney("123456789000", appealData(penaltyType))(implicitly, HeaderCarrier(), authRequest)
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

