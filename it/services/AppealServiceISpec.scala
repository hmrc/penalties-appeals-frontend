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

package services

import com.github.tomakehurst.wiremock.client.WireMock._
import models.session.UserAnswers
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{PenaltyTypeEnum, UserRequest}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext


class AppealServiceISpec extends IntegrationSpecCommonBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val appealService: AppealService = injector.instanceOf[AppealService]
  val correlationId: String = "correlationId"

  "submitAppeal" should {
    "return true when the connector call succeeds for crime" in  {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for loss of staff" in  {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("lossOfStaff")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for technical issues" in {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
        SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("technicalIssues")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for fire or flood" in {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("fireOrFlood")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for bereavement" in {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "bereavement",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("bereavement")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for other - no file upload" in {
      successfulAppealSubmission(isLPP = false, "1234")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "this is information",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("other")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for other - file upload with duplicates" in {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("/url"),
        uploadDetails = Some(
          UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = sampleDate,
            checksum = "123456789",
            size = 100
          )
        ),
        failureDetails = None,
        lastUpdated = LocalDateTime.now()
      )
      val uploadAsDuplicate: UploadJourney = uploadAsReady.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE)
      val uploadAsWaiting: UploadJourney = uploadAsReady.copy(reference = "ref3", fileStatus = UploadStatusEnum.WAITING, downloadUrl = None, uploadDetails = None)
      successfulAppealSubmission(isLPP = false, "1234")
      await(repository.updateStateOfFileUpload("1234", uploadAsReady, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", uploadAsDuplicate, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", uploadAsWaiting, isInitiateCall = true))
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "this is information",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "yes"
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("other")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyDuplicateFilesSubmitted")) shouldBe true
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.find(_.getBodyAsString.contains("PenaltyAppealSubmitted")).head.getBodyAsString.contains("uploadedFiles") shouldBe true

      result shouldBe Right((): Unit)
    }

    "return true when the connector call succeeds for other - user selects no to uploading files (some files already uploaded)" in {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("/url"),
        uploadDetails = Some(
          UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = sampleDate,
            checksum = "123456789",
            size = 100
          )
        ),
        failureDetails = None,
        lastUpdated = LocalDateTime.now()
      )
      successfulAppealSubmission(isLPP = false, "1234")
      await(repository.updateStateOfFileUpload("1234", uploadAsReady, isInitiateCall = true))
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "this is information",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "no"
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("other")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyDuplicateFilesSubmitted")) shouldBe false
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.find(_.getBodyAsString.contains("PenaltyAppealSubmitted")).head.getBodyAsString.contains("uploadedFiles") shouldBe false
      result shouldBe Right((): Unit)
    }

    "return true for hospital stay" when {
      "there is no hospital stay" in  {
        successfulAppealSubmission(isLPP = false, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
        result shouldBe Right((): Unit)
      }

      "there is a ongoing hospital stay" in {
        successfulAppealSubmission(isLPP = false, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
        result shouldBe Right((): Unit)
      }

      "there has been a hospital stay that has ended" in {
        successfulAppealSubmission(isLPP = false, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "yes",
          SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenHealthIssueEnded -> LocalDate.parse("2022-01-02")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("health")(userRequest, implicitly, implicitly))
        findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
        result shouldBe Right((): Unit)
      }

      "return true when the connector call succeeds for LPP" in {
        successfulAppealSubmission(isLPP = true, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
        result shouldBe Right((): Unit)
      }
    }

    "return true when appealing two penalties at the same time" in {
      successfulAppealSubmission(isLPP = false, "1234")
      successfulAppealSubmission(isLPP = false, "5678")
      val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
        SessionKeys.doYouWantToAppealBothPenalties -> "yes",
        SessionKeys.firstPenaltyChargeReference -> "1234",
        SessionKeys.secondPenaltyChargeReference -> "5678"
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      result shouldBe Right((): Unit)
    }

    "return false" when {
      "the connector returns a fault" in {
        failedAppealSubmissionWithFault(isLPP = false, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }

      "the connector returns an unknown status code" in {
        failedAppealSubmission(isLPP = false, "1234")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }

      "not all keys are present in the session" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("", Json.obj()))(FakeRequest("POST", "/check-your-answers"))
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }

      "when one of multiple appeal submissions fails" in {
        failedAppealSubmissionWithFault(isLPP = false, "1234")
        successfulAppealSubmission(isLPP = false, "5678")
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
          SessionKeys.doYouWantToAppealBothPenalties -> "yes",
          SessionKeys.firstPenaltyChargeReference -> "1234",
          SessionKeys.secondPenaltyChargeReference -> "5678"
        )))(fakeRequest)
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }
    }
  }
}
