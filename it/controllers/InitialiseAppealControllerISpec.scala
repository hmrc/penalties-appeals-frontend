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

import models.PenaltyTypeEnum
import org.mongodb.scala.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, _}
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class InitialiseAppealControllerISpec extends IntegrationSpecCommonBase {
  val controller: InitialiseAppealController = injector.instanceOf[InitialiseAppealController]

  class Setup {
    await(userAnswersRepository.collection.deleteMany(Document()).toFuture())
  }

  "GET /initialise-appeal" should {
    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoad("1234", isLPP = false, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
    }

    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned for LPP" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true)
      val result = controller.onPageLoad("1234", isLPP = true, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
    }

    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned for LPP additional" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      val result = controller.onPageLoad("1234", isLPP = true, isAdditional = true)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined shouldBe false
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
    }

    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned for LPP - multiple penalties" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      successfulGetMultiplePenalties("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoad("1234", isLPP = true, isAdditional = true)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined shouldBe false
      userAnswers.getAnswer[String](SessionKeys.firstPenaltyAmount).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.secondPenaltyAmount).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.firstPenaltyChargeReference).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.secondPenaltyChargeReference).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
    }

    "render an ISE when the appeal data can not be retrieved" in new Setup {
      failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoad("1234", isLPP = false, isAdditional = false)(FakeRequest().withSession(
        authToken -> "1234"
      ))
      await(result).header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "GET /initialise-appeal-against-the-obligation" should {
    "call the service to validate the penalty ID and redirect to the Cancel VAT Registration page when data is returned" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoadForObligation("1234", isLPP = false, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true

    }

    "call the service to validate the penalty ID and redirect to the Cancel VAT Registration page when data is returned for LPP additional" in new Setup {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        authToken -> "1234"
      )
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      val result = controller.onPageLoadForObligation("1234", isLPP = true, isAdditional = true)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      val userAnswers = await(userAnswersRepository.collection.find(Document()).toFuture()).head
      userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[String](SessionKeys.penaltyNumber).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      userAnswers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).isDefined shouldBe true
      userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined shouldBe true
    }

    "render an ISE when the appeal data can not be retrieved" in new Setup {
      failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoadForObligation("1234", isLPP = false, isAdditional = false)(FakeRequest().withSession(
        authToken -> "1234"
      ))
      await(result).header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
