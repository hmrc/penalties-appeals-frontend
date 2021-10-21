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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, _}
import stubs.PenaltiesStub._
import utils.{IntegrationSpecCommonBase, SessionKeys}

class InitialiseAppealControllerISpec extends IntegrationSpecCommonBase {
  val controller: InitialiseAppealController = injector.instanceOf[InitialiseAppealController]

  "GET /initialise-appeal" should {
    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoad("1234", isLPP = false, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
    }

    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned for LPP" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true)
      val result = controller.onPageLoad("1234", isLPP = true, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
    }

    "call the service to validate the penalty ID and redirect to the Appeal Start page when data is returned for LPP additional" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      val result = controller.onPageLoad("1234", isLPP = true, isAdditional = true)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.AppealStartController.onPageLoad().url
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal).isDefined shouldBe false
    }

    "render an ISE when the appeal data can not be retrieved" in {
      failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoad("1234", isLPP = false, isAdditional = false)(FakeRequest())
      await(result).header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "GET /initialise-appeal-against-the-obligation" should {
    "call the service to validate the penalty ID and redirect to the Cancel VAT Registration page when data is returned" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoadForObligation("1234", isLPP = false, isAdditional = false)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")

    }

    "call the service to validate the penalty ID and redirect to the Cancel VAT Registration page when data is returned for LPP additional" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      val result = controller.onPageLoadForObligation("1234", isLPP = true, isAdditional = true)(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      await(result).session.get(SessionKeys.appealType).isDefined shouldBe true
      await(result).session.get(SessionKeys.startDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.endDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.penaltyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.dueDateOfPeriod).isDefined shouldBe true
      await(result).session.get(SessionKeys.dateCommunicationSent).isDefined shouldBe true
      await(result).session.get(SessionKeys.journeyId).isDefined shouldBe true
      await(result).session.get(SessionKeys.isObligationAppeal) shouldBe Some("true")
    }

    "render an ISE when the appeal data can not be retrieved" in {
      failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val result = controller.onPageLoadForObligation("1234", isLPP = false, isAdditional = false)(FakeRequest())
      await(result).header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
