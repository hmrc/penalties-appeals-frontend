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

package services

import models.UserRequest
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class AppealServiceISpec extends IntegrationSpecCommonBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val appealService: AppealService = injector.instanceOf[AppealService]

  "submitAppeal" should {
    "return true when the connector call succeeds for crime" in {
      successfulAppealSubmission
      val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
      ))
      val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
      result shouldBe true
    }

    "return true when the connector call succeeds for loss of staff" in {
      successfulAppealSubmission
      val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
      ))
      val result = await(appealService.submitAppeal("lossOfStaff")(userRequest, implicitly, implicitly))
      result shouldBe true
    }

    "return true when the connector call succeeds for technical issues" in {
      successfulAppealSubmission
      val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
      ))
      val result = await(appealService.submitAppeal("technicalIssues")(userRequest, implicitly, implicitly))
      result shouldBe true
    }

    "return true for hospital stay" when {
      "there is no hospital stay" in {
        successfulAppealSubmission
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2021-01-01T12:00:00"
        ))
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        result shouldBe true
      }

      "there is a ongoing hospital stay" in {
        successfulAppealSubmission
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00"
        ))
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        result shouldBe true
      }

      "there has been a hospital stay that has ended" in {
        successfulAppealSubmission
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00",
          SessionKeys.whenHealthIssueEnded -> "2021-01-02T12:00:00"
        ))
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        result shouldBe true
      }
    }

    "return false" when {
      "the connector returns a fault" in {
        failedAppealSubmissionWithFault
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.dateOfCrime -> "2022-01-01"
        ))
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe false
      }

      "the connector returns an unknown status code" in {
        failedAppealSubmission
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.dateOfCrime -> "2022-01-01"
        ))
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe false
      }
    }

    "return an exception" when {
      "not all keys are present in the session" in {
        val userRequest = UserRequest("")(FakeRequest("POST", "/check-your-answers"))
        intercept[Exception](await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly)))
      }
    }
  }
}
