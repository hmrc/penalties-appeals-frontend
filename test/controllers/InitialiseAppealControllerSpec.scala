/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{AppealData, PenaltyTypeEnum}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.SessionKeys

import java.time.LocalDateTime
import scala.concurrent.Future

class InitialiseAppealControllerSpec extends SpecBase {
  val mockAppealsService = mock(classOf[AppealService])
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAppealsService)
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
    val controller = new InitialiseAppealController(
      mockAppealsService,
      errorHandler
    )(mcc, authPredicate)
  }

  "onPageLoad" should {
    "call the penalties backend and handle a failed response" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val result = await(controller.onPageLoad("12345", isLPP = false)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDateTime.of(2020, 1, 1, 1, 1, 0),
        endDate = LocalDateTime.of(2020, 1, 2, 1, 1, 0),
        dueDate = LocalDateTime.of(2020, 2, 7, 1, 1, 0),
        dateCommunicationSent = LocalDateTime.of(2020, 2, 8, 1, 1, 0)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result = controller.onPageLoad("12345", isLPP = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to start appeal for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDateTime.of(2020, 1, 1, 1, 1, 0),
        endDate = LocalDateTime.of(2020, 1, 2, 1, 1, 0),
        dueDate = LocalDateTime.of(2020, 2, 7, 1, 1, 0),
        dateCommunicationSent = LocalDateTime.of(2020, 2, 8, 1, 1, 0)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result = controller.onPageLoad("12345", isLPP = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
    }
  }

  "onPageLoadForObligation" should {
    "call the penalties backend and handle a failed response" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val result = await(controller.onPageLoadForObligation("12345", isLPP = false)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Submission,
        startDate = LocalDateTime.of(2020, 1, 1, 1, 1, 0),
        endDate = LocalDateTime.of(2020, 1, 2, 1, 1, 0),
        dueDate = LocalDateTime.of(2020, 2, 7, 1, 1, 0),
        dateCommunicationSent = LocalDateTime.of(2020, 2, 8, 1, 1, 0)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result = controller.onPageLoadForObligation("12345", isLPP = false)(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
    }

    "call the penalties backend and handle a success response and add the keys to the session " +
      "- redirect to Cancel VAT Registration page for LPP" in new Setup(AuthTestModels.successfulAuthResult) {
      val appealDataToReturn: AppealData = AppealData(
        `type` = PenaltyTypeEnum.Late_Payment,
        startDate = LocalDateTime.of(2020, 1, 1, 1, 1, 0),
        endDate = LocalDateTime.of(2020, 1, 2, 1, 1, 0),
        dueDate = LocalDateTime.of(2020, 2, 7, 1, 1, 0),
        dateCommunicationSent = LocalDateTime.of(2020, 2, 8, 1, 1, 0)
      )
      when(mockAppealsService.validatePenaltyIdForEnrolmentKey(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataToReturn)))
      val result = controller.onPageLoadForObligation("12345", isLPP = true)(fakeRequest)
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).header.status shouldBe SEE_OTHER
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
    }
  }
}
