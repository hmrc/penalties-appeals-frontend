/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate
import base.SpecBase
import models.appeals.MultiplePenaltiesData
import models.{AppealData, PenaltyTypeEnum}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Result
import play.api.test.Helpers._
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys

import scala.concurrent.Future

class InitialiseAppealControllerSpec extends SpecBase {
  val mockAppealsService: AppealService = mock(classOf[AppealService])
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], expectedContent: Option[MultiplePenaltiesData] = None) {
    reset(mockAppealsService)
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockAppealsService.validateMultiplePenaltyDataForEnrolmentKey(any())(any(), any(), any()))
      .thenReturn(Future.successful(expectedContent))
    val controller = new InitialiseAppealController(
      mockAppealsService,
      errorHandler
    )(mcc, authPredicate)
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
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(
          2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = false, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).isDefined shouldBe false
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(
          2020, 2, 8)
      )
        when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(),any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).isDefined shouldBe false
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal for Additional Penalty (LPP)" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Additional,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.appealType).get shouldBe PenaltyTypeEnum.Additional.toString
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).isDefined shouldBe false
  }

    "call the penalties backend for multiple penalties and handle a success response and do not call multiple penalty endpoint when appeal is LSP" in new Setup(AuthTestModels.successfulAuthResult) {
      reset(mockAppealsService)
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))

      val result: Future[Result] = controller.onPageLoad("12345", isLPP = false, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.firstPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyAmount).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).isDefined shouldBe false
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).isDefined shouldBe false
    }

    "call the penalties backend for multiple penalties and handle a success response and add multi penalties details keys " +
      "to session" in new Setup(AuthTestModels.successfulAuthResult, Some(sampleExamplePenalties)) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Additional,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))

      val result: Future[Result] = controller.onPageLoad("12345", isLPP = true, isAdditional = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.appealType).get shouldBe PenaltyTypeEnum.Additional.toString
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.firstPenaltyAmount).isDefined shouldBe true
      await(result).session.get(SessionKeys.firstPenaltyAmount).get shouldBe "101.01"
      await(result).session.get(SessionKeys.secondPenaltyAmount).isDefined shouldBe true
      await(result).session.get(SessionKeys.secondPenaltyAmount).get shouldBe "101.02"
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).isDefined shouldBe true
      await(result).session.get(SessionKeys.firstPenaltyChargeReference).get shouldBe "123456789"
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).isDefined shouldBe true
      await(result).session.get(SessionKeys.secondPenaltyChargeReference).get shouldBe "123456790"
    }

   }

  "onPageLoadForObligation" should {
    "call the penalties backend and handle a failed response" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val result: Result = await(controller.onPageLoadForObligation("12345", isLPP = false, isAdditional = false)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(
          2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoadForObligation("12345", isLPP = false, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDate.of(
          2020, 1, 1),
        endDate = LocalDate.of(
          2020, 1, 2),
        dueDate = LocalDate.of(
          2020, 2, 7),
        dateCommunicationSent = LocalDate.of(
          2020, 2, 8)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result: Future[Result] = controller.onPageLoadForObligation("12345", isLPP = true, isAdditional = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
    }
    "call the penalties backend and handle a success response and add the keys to the session " +
          "- redirect to Cancel VAT Registration page for LPP additional" in new Setup(AuthTestModels.successfulAuthResult) {
          val appealDataToReturn: AppealData = AppealData(
            `type` = PenaltyTypeEnum.Additional,
            startDate = LocalDate.of(
              2020, 1, 1),
            endDate = LocalDate.of(
              2020, 1, 2),
            dueDate = LocalDate.of(
              2020, 2, 7),
            dateCommunicationSent = LocalDate.of(
              2020, 2, 8)
          )
          when(mockAppealsService.validatePenaltyIdForEnrolmentKey(any(), any(),any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(appealDataToReturn)))
          val result: Future[Result] = controller.onPageLoadForObligation("12345", isLPP = true, isAdditional = true)(fakeRequest)
          redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
          await(result).header.status shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
          await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
          await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
          await(result).session.get(SessionKeys.penaltyNumber).isDefined shouldBe true
          await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
          await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
          await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
        }
}

