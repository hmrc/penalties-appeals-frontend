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

package services

import com.github.tomakehurst.wiremock.client.WireMock._
import models.session.UserAnswers
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{PenaltyTypeEnum, UserRequest}
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._


class AppealServiceISpec extends IntegrationSpecCommonBase with LogCapturing {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val uploadJourneyRepository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val appealService: AppealService = injector.instanceOf[AppealService]
  val correlationId: String = "correlationId"
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


  "submitAppeal" should {

    def appealSubmissionTestForReasonableExcuse(reasonableExcuse: String, userRequest: UserRequest[_], extraDetailsForTestName: Option[String] = None): Unit = {
      s"return Right when the connector call succeeds for $reasonableExcuse ${extraDetailsForTestName.getOrElse("")}" in {
        successfulAppealSubmission(isLPP = false, "1234")
        val result = await(appealService.submitAppeal(reasonableExcuse)(userRequest, implicitly, implicitly))
        eventually {
          findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
        }
        result shouldBe Right((): Unit)
      }
    }

    val baseUserAnswers = Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    )

    def userRequest(answers: JsObject): UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234", answers))(fakeRequest)

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "lossOfStaff", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "lossOfStaff",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
    )))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "crime", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
    )))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "technicalIssues", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "technicalIssues",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
      SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
    )))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "fireOrFlood", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "fireOrFlood",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
    )))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "bereavement", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
    )))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "other", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "other",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whyReturnSubmittedLate -> "this is information",
      SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
    )), extraDetailsForTestName = Some("- no file upload"))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "health", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "health",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.wasHospitalStayRequired -> "no",
      SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2022-01-01")
    )), extraDetailsForTestName = Some("- no hospital stay"))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "health", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "health",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.wasHospitalStayRequired -> "yes",
      SessionKeys.hasHealthEventEnded -> "no",
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
    )), extraDetailsForTestName = Some("- ongoing hospital stay"))

    appealSubmissionTestForReasonableExcuse(reasonableExcuse = "health", userRequest(baseUserAnswers ++ Json.obj(
      SessionKeys.reasonableExcuse -> "health",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.wasHospitalStayRequired -> "yes",
      SessionKeys.hasHealthEventEnded -> "yes",
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
      SessionKeys.whenHealthIssueEnded -> LocalDate.parse("2022-01-02")
    )), extraDetailsForTestName = Some("- hospital stay ended"))

    "return Right when the connector call succeeds for other - user selects no to uploading files (some files already uploaded)" in {
      successfulAppealSubmission(isLPP = false, "1234")
      await(uploadJourneyRepository.updateStateOfFileUpload("1234", uploadAsReady, isInitiateCall = true))
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
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.find(_.getBodyAsString.contains("PenaltyAppealSubmitted")).head.getBodyAsString.contains("uploadedFiles") shouldBe false
      result shouldBe Right((): Unit)
    }

    "return Right when the connector call succeeds for LPP" in {
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
      eventually {
        findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
      }
      result shouldBe Right((): Unit)
    }
  }

  "return Right when appealing two penalties at the same time (and they both succeed)" in {
    successfulAppealSubmission(isLPP = true, "1234")
    successfulAppealSubmission(isLPP = true, "5678")
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
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.doYouWantToAppealBothPenalties -> "yes",
      SessionKeys.firstPenaltyChargeReference -> "1234",
      SessionKeys.secondPenaltyChargeReference -> "5678"
    )))(fakeRequest)
    val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
    eventually {
      findAll(postRequestedFor(urlMatching("/write/audit"))).asScala.exists(_.getBodyAsString.contains("PenaltyAppealSubmitted")) shouldBe true
    }
    result shouldBe Right((): Unit)
  }

  "return Right when one the of multiple appeal submissions fails (logging a PD)" in {
    failedAppealSubmission(isLPP = true, "1234")
    successfulAppealSubmission(isLPP = true, "5678")
    val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-31"),
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
    withCaptureOfLoggingFrom(logger) {
      logs => {
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Right((): Unit)
        logs.exists(_.getMessage.contains(s"MULTI_APPEAL_FAILURE Multiple appeal covering 2020-01-01-2020-01-31 for user with VRN 123456789 failed. ")) shouldBe true
        logs.exists(_.getMessage.contains(s"LPP1 appeal was not submitted successfully, Reason given Some issue with document storage. Correlation ID for LPP1:")) shouldBe true
        logs.exists(_.getMessage.contains(s"LPP2 appeal was submitted successfully, case ID is Some(PR-1234). Correlation ID for LPP2:")) shouldBe true
      }
    }
  }

  "return Right when one of the multiple appeal submissions fails because of a fault (logging a PD)" in {
    failedAppealSubmissionWithFault(isLPP = true, "1234")
    successfulAppealSubmission(isLPP = true, "5678")
    val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-31"),
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
    withCaptureOfLoggingFrom(logger) {
      logs => {
        val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
        result shouldBe Right((): Unit)
        logs.exists(_.getMessage.contains("MULTI_APPEAL_FAILURE Multiple appeal covering 2020-01-01-2020-01-31 for user with VRN 123456789 failed. ")) shouldBe true
        logs.exists(_.getMessage.contains("LPP1 appeal was not submitted successfully, Reason given An issue occurred whilst appealing a penalty with error: Connection reset. Correlation ID for LPP1:")) shouldBe true
        logs.exists(_.getMessage.contains("LPP2 appeal was submitted successfully, case ID is Some(PR-1234). Correlation ID for LPP2:")) shouldBe true
      }
    }
  }

  "return Left" when {
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

    "both of multiple appeal submissions fails" in {
      failedAppealSubmissionWithFault(isLPP = true, "1234")
      failedAppealSubmissionWithFault(isLPP = true, "5678")
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
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
        SessionKeys.doYouWantToAppealBothPenalties -> "yes",
        SessionKeys.firstPenaltyChargeReference -> "1234",
        SessionKeys.secondPenaltyChargeReference -> "5678"
      )))(fakeRequest)
      val result = await(appealService.submitAppeal("crime")(userRequest, implicitly, implicitly))
      result shouldBe Left(INTERNAL_SERVER_ERROR)
    }
  }

  "removePreviouslySubmittedAppealData" should {
    "remove all user answers and upload data when called with a valid journey ID" in {
      await(uploadJourneyRepository.collection.deleteMany(Document()).toFuture())
      await(userAnswersRepository.collection.deleteMany(Document()).toFuture())
      await(uploadJourneyRepository.updateStateOfFileUpload("1240", uploadAsReady, isInitiateCall = true))
      await(userAnswersRepository.upsertUserAnswer(UserAnswers("1240", Json.obj())))
      await(uploadJourneyRepository.collection.countDocuments().toFuture()) shouldBe 1
      await(userAnswersRepository.collection.countDocuments().toFuture()) shouldBe 1
      await(appealService.removePreviouslySubmittedAppealData(Some("1240")))
      await(uploadJourneyRepository.collection.countDocuments().toFuture()) shouldBe 0
      await(userAnswersRepository.collection.countDocuments().toFuture()) shouldBe 0
    }
  }
}
