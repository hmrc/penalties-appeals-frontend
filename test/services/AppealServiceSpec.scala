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

import base.SpecBase
import com.mongodb.client.result.DeleteResult
import connectors.PenaltiesConnector
import connectors.httpParsers.{InvalidJson, UnexpectedFailure}
import models._
import models.appeals.{AppealSubmissionResponseModel, MultiplePenaltiesData}
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import repositories.UserAnswersRepository
import services.monitoring.JsonAuditModel
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.{SessionKeys, UUIDGenerator}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

//scalastyle:off
class AppealServiceSpec extends SpecBase with LogCapturing {
  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
  val mockUUIDGenerator: UUIDGenerator = mock(classOf[UUIDGenerator])

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockUserAnswersRepository: UserAnswersRepository = mock(classOf[UserAnswersRepository])

  val fakeRequestForCrimeJourney: UserRequest[AnyContent] = UserRequest("123456789", answers = userAnswers(Json.obj(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2021-12-01"),
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.hasConfirmedDeclaration -> true,
    SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
    SessionKeys.penaltyNumber -> "123456789",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.doYouWantToAppealBothPenalties -> "no"
  )))(fakeRequest)

  val fakeRequestForCrimeJourneyMultiple: UserRequest[AnyContent] = UserRequest("123456789", answers = userAnswers(Json.obj(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2021-12-01"),
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.hasConfirmedDeclaration -> true,
    SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
    SessionKeys.penaltyNumber -> "123456789",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.doYouWantToAppealBothPenalties -> "yes",
    SessionKeys.firstPenaltyChargeReference -> "123456789",
    SessionKeys.secondPenaltyChargeReference -> "123456788",
    SessionKeys.startDateOfPeriod -> "2024-01-01",
    SessionKeys.endDateOfPeriod -> "2024-01-31"
  )))(fakeRequest)

  val fakeRequestForOtherJourney: UserRequest[AnyContent] = UserRequest("123456789", answers = userAnswers(Json.obj(
    SessionKeys.reasonableExcuse -> "other",
    SessionKeys.hasConfirmedDeclaration -> true,
    SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2021-12-01"),
    SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
    SessionKeys.isUploadEvidence -> "yes",
    SessionKeys.penaltyNumber -> "123456789",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.doYouWantToAppealBothPenalties -> "no"
  )))(fakeRequest)

  val fakeRequestForOtherJourneyDeclinedUploads: UserRequest[AnyContent] = UserRequest("123456789", answers = userAnswers(Json.obj(
    SessionKeys.reasonableExcuse -> "other",
    SessionKeys.hasConfirmedDeclaration -> true,
    SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2021-12-01"),
    SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
    SessionKeys.isUploadEvidence -> "no",
    SessionKeys.penaltyNumber -> "123456789",
    SessionKeys.doYouWantToAppealBothPenalties -> "no"
  )))(fakeRequest)

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPP: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_PAYMENT",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPPAdditional: JsValue = Json.parse(
    """
      |{
      | "type": "ADDITIONAL",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01",
      | "dueDate": "2020-02-07",
      | "dateCommunicationSent": "2020-02-08"
      |}
      |""".stripMargin)

  val multiplePenaltiesModel: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456790",
    secondPenaltyAmount = 101.02,
    firstPenaltyCommunicationDate = LocalDate.parse("2022-01-01"),
    secondPenaltyCommunicationDate = LocalDate.parse("2022-01-02")
  )

  class Setup {
    reset(mockPenaltiesConnector)
    reset(mockDateTimeHelper)
    reset(mockAuditService)
    reset(mockUploadJourneyRepository)
    reset(mockUUIDGenerator)
    reset(mockUserAnswersRepository)

    val service: AppealService =
      new AppealService(mockPenaltiesConnector, appConfig, mockDateTimeHelper, mockAuditService, mockUUIDGenerator, mockUserAnswersRepository, mockUploadJourneyRepository)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
    when(mockUUIDGenerator.generateUUID).thenReturn("uuid-1", "uuid-2")
  }

  "validatePenaltyIdForEnrolmentKey" should {
    "return None when the connector returns None" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(Json.parse("{}"))))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return Some when the connector returns Json that is parsable to a model" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJson)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }

    "return Some when the connector returns Json that is parsable to a model for LPP" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPP)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = false)(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }

    "return Some when the connector returns Json that is parsable to a model for LPP - Additional penalty" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPPAdditional)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = true)(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }
  }

  "validateMultiplePenaltyDataForEnrolmentKey" should {
    "return None when the connector returns a left with an UnexpectedFailure" in new Setup {
      when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))

      val result: Future[Option[MultiplePenaltiesData]] = service.validateMultiplePenaltyDataForEnrolmentKey("123")(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return None when the connector returns returns InvalidJson that cannot be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(InvalidJson)))

      val result: Future[Option[MultiplePenaltiesData]] = service.validateMultiplePenaltyDataForEnrolmentKey("123")(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return Some when the connector returns Json that can be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getMultiplePenaltiesForPrincipleCharge(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(multiplePenaltiesModel)))

      val result: Future[Option[MultiplePenaltiesData]] = service.validateMultiplePenaltyDataForEnrolmentKey("123")(
        new AuthRequest[AnyContent]("123456789"), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }
  }

  "getReasonableExcuseListAndParse" should {
    s"call the connector and parse the result to $Some $Seq $ReasonableExcuse" in new Setup {
      val jsonRepresentingSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crimeReason"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      when(mockPenaltiesConnector.getListOfReasonableExcuses()(any(), any()))
        .thenReturn(Future.successful(
          Some(jsonRepresentingSeqOfReasonableExcuses)
        ))

      val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuseListAndParse())
      result.isDefined shouldBe true
      result.get shouldBe Seq(
        ReasonableExcuse(
          `type` = "bereavement",
          descriptionKey = "reasonableExcuses.bereavementReason",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "crime",
          descriptionKey = "reasonableExcuses.crimeReason",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "fireOrFlood",
          descriptionKey = "reasonableExcuses.fireOrFloodReason",
          isOtherOption = false
        )
      )
    }

    s"call the connector and return $None" when {
      "the connector call succeeds but invalid json is returned and therefore can not be parsed" in new Setup {
        val jsonRepresentingInvalidSeqOfReasonableExcuses: JsValue = Json.parse(
          """
            |{
            |  "excusesssss": [
            |    {
            |      "type": "bereavement",
            |      "descriptionKey": "reasonableExcuses.bereavementReason"
            |    },
            |    {
            |      "type": "crime",
            |      "descriptionKey": "reasonableExcuses.crimeReason"
            |    },
            |    {
            |      "type": "fireOrFlood",
            |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
            |    }
            |  ]
            |}
            |""".stripMargin
        )
        when(mockPenaltiesConnector.getListOfReasonableExcuses()(any(), any()))
          .thenReturn(Future.successful(
            Some(jsonRepresentingInvalidSeqOfReasonableExcuses)
          ))

        val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuseListAndParse())
        result.isDefined shouldBe false
      }

      "the connector call fails" in new Setup {
        when(mockPenaltiesConnector.getListOfReasonableExcuses()(any(), any()))
          .thenReturn(Future.successful(None))

        val result: Option[Seq[ReasonableExcuse]] = await(service.getReasonableExcuseListAndParse())
        result.isDefined shouldBe false
      }
    }
  }

  "submitAppeal" should {
    "parse the session keys into a model and return true when the connector call is successful and audit the response" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
      result shouldBe Right((): Unit)
      verify(mockAuditService, times(1)).audit(any[JsonAuditModel]())(any[HeaderCarrier](),
        any[ExecutionContext](), any())
    }

    "parse the session keys into a model and return true when the connector call is successful and audit the response" +
      " - for appealing multiple penalties" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
      result shouldBe Right((): Unit)
      verify(mockAuditService, times(2)).audit(any[JsonAuditModel]())(any[HeaderCarrier](),
        any[ExecutionContext](), any())
    }

    "parse the session keys into a model and audit the response - removing file uploads if the user selected no to uploading files" in new Setup {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 1, 1)
      val uploadInReadyState: UploadJourney = UploadJourney(
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
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(Some(Seq(uploadInReadyState))))
      val result: Either[Int, Unit] = await(service.submitAppeal("other")(fakeRequestForOtherJourneyDeclinedUploads, implicitly, implicitly))
      result shouldBe Right((): Unit)

      verify(mockAuditService, times(1)).audit(any[JsonAuditModel]())(any[HeaderCarrier](),
        any[ExecutionContext](), any())
    }

    "ignore FAILED or WAITING files for audit" in new Setup {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 1, 1)
      val auditCapture: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
      val uploadInFailedState: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.FAILED,
        downloadUrl = None,
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
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(Some(Seq(uploadInFailedState))))
      val result: Either[Int, Unit] = await(service.submitAppeal("other")(fakeRequestForOtherJourney, implicitly, implicitly))
      result shouldBe Right((): Unit)

      verify(mockAuditService, times(1)).audit(auditCapture.capture())(any[HeaderCarrier](),
        any[ExecutionContext](), any())
      (auditCapture.getValue.detail \ "appealInformation" \ "uploadedFiles").as[List[JsValue]] shouldBe Seq.empty
    }

    "succeed if one of 2 appeal submissions fail and log a PD (LPP1 fails)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          verify(mockUUIDGenerator, times(2)).generateUUID
          logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was submitted successfully, case ID is Some(REV-1234). Correlation ID for LPP2: uuid-2. ") shouldBe true
        }
      }
    }

    "succeed if one of 2 appeal submissions fail and log a PD (LPP2 fails)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was submitted successfully, case ID is Some(REV-1234). Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP2: uuid-2. ") shouldBe true
        }
      }
    }

    "succeed if an error occurs during file notification storage and the other submission fails and log a PD (LPP1 docs fail, LPP2 fails submission)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), MULTI_STATUS, error = Some("Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator")))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          val logMessage = s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was submitted successfully (case ID is Some(REV-1234)) but there was an issue storing the notification for uploaded files, response body (Some(Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator)). Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP2: uuid-2. "
          logs.exists(_.getMessage == logMessage) shouldBe true
        }
      }
    }

    "succeed if an error occurs during file notification storage and the other submission succeeds and log a PD (LPP2 docs fail, LPP1 successful submission)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1235"), MULTI_STATUS, error = Some("Appeal submitted (case ID: REV-1235) but received 500 response from file notification orchestrator")))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          val logMessage = s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was submitted successfully, case ID is Some(REV-1234). Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was submitted successfully (case ID is Some(REV-1235)) but there was an issue storing the notification for uploaded files, response body (Some(Appeal submitted (case ID: REV-1235) but received 500 response from file notification orchestrator)). Correlation ID for LPP2: uuid-2. "
          logs.exists(_.getMessage == logMessage) shouldBe true
        }
      }
    }

    "succeed if an error occurs during file notification storage and the other submission fails and log a PD (LPP2 docs fail, LPP1 fails submission)" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, "Some issue with submission"))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), MULTI_STATUS, error = Some("Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator")))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          val logMessage = s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was not submitted successfully, Reason given Some issue with submission. Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was submitted successfully (case ID is Some(REV-1234)) but there was an issue storing the notification for uploaded files, response body (Some(Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator)). Correlation ID for LPP2: uuid-2. "
          logs.exists(_.getMessage == logMessage) shouldBe true
        }
      }
    }

    "succeed if an error occurs during file notification storage for both submissions and log a PD" in new Setup {
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), MULTI_STATUS, error = Some("Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator")))))
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1235"), MULTI_STATUS, error = Some("Appeal submitted (case ID: REV-1235) but received 500 response from file notification orchestrator")))))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
          result shouldBe Right((): Unit)
          logs.exists(_.getMessage == s"MULTI_APPEAL_FAILURE Multiple appeal covering 2024-01-01-2024-01-31 for user with VRN 123456789 failed. " +
            s"LPP1 appeal was submitted successfully (case ID is Some(REV-1234)) but there was an issue storing the notification for uploaded files, response body (Some(Appeal submitted (case ID: REV-1234) but received 500 response from file notification orchestrator)). Correlation ID for LPP1: uuid-1. " +
            s"LPP2 appeal was submitted successfully (case ID is Some(REV-1235)) but there was an issue storing the notification for uploaded files, response body (Some(Appeal submitted (case ID: REV-1235) but received 500 response from file notification orchestrator)). Correlation ID for LPP2: uuid-2. ") shouldBe true
        }
      }
    }

    "return Left" when {
      "the connector returns a non-200 response" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.successful(None))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(BAD_GATEWAY)
      }

      "the connector returns a non-200 response for multiple submissions" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456789"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), ArgumentMatchers.eq("123456788"), any(), any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(BAD_GATEWAY, ""))))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.successful(None))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourneyMultiple, implicitly, implicitly))
        result shouldBe Left(BAD_GATEWAY)
      }

      "the connector throws an exception" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.failed(new Exception("I failed.")))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.successful(None))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }

      "the repository throws and exception" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(AppealSubmissionResponseModel(Some("REV-1234"), OK))))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.failed(new Exception("I failed.")))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }
    }
  }

  "isAppealLate" should {
    val fakeRequestForAppealingBothPenalties: (LocalDate, LocalDate) => UserRequest[AnyContent] = (lpp1Date: LocalDate, lpp2Date: LocalDate) =>
      UserRequest("123456789", answers = userAnswers(Json.obj(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.doYouWantToAppealBothPenalties -> "yes",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
        SessionKeys.penaltyNumber -> "123456789",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.firstPenaltyCommunicationDate -> lpp1Date,
        SessionKeys.secondPenaltyCommunicationDate -> lpp2Date
      )))(fakeRequest)

    val fakeRequestForAppealingSinglePenalty: LocalDate => UserRequest[AnyContent] = (date: LocalDate) => UserRequest("123456789", answers = userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.dateCommunicationSent -> date,
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.penaltyNumber -> "123456789",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment
    )))(fakeRequest)

    "return true" when {
      "communication date of penalty > 30 days ago" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 1, 1))
        val result = service.isAppealLate()(fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 1)))
        result shouldBe true
      }

      "appealing both penalties and LPP1 is late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 1, 1))
        val result = service.isAppealLate()(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)))
        result shouldBe true
      }

      "appealing both penalties and both are late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 4, 1))
        val result = service.isAppealLate()(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 1)))
        result shouldBe true
      }
    }

    "return false" when {
      "communication date of penalty < 30 days ago" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 1, 1))
        val result = service.isAppealLate()(fakeRequestForAppealingSinglePenalty(LocalDate.of(2021, 12, 31)))
        result shouldBe false
      }

      "appealing both penalties and LPP1 and LPP2 are not late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 1, 1))
        val result = service.isAppealLate()(fakeRequestForAppealingBothPenalties(LocalDate.of(2021, 12, 31), LocalDate.of(2021, 12, 31)))
        result shouldBe false
      }
    }
  }

  "removePreviouslySubmittedAppealData" should {
    "do nothing when no journey ID is provided" in new Setup {
      await(service.removePreviouslySubmittedAppealData(None))
      verify(mockUserAnswersRepository, never).removeUserAnswers(any())
      verify(mockUploadJourneyRepository, never).removeAllFilesForJourney(any())
    }

    "request to remove any appeal data when the journey ID has been provided" in new Setup {
      when(mockUserAnswersRepository.removeUserAnswers(ArgumentMatchers.eq("J1234"))).thenReturn(Future.successful(Some(DeleteResult.acknowledged(1))))
      when(mockUploadJourneyRepository.removeAllFilesForJourney(ArgumentMatchers.eq("J1234"))).thenReturn(Future.successful(Some(DeleteResult.acknowledged(1))))
      await(service.removePreviouslySubmittedAppealData(Some("J1234")))
      verify(mockUserAnswersRepository, times(1)).removeUserAnswers(any())
      verify(mockUploadJourneyRepository, times(1)).removeAllFilesForJourney(any())
    }
  }
}
