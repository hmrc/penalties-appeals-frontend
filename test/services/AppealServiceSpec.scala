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

import base.SpecBase
import config.AppConfig
import connectors.PenaltiesConnector
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{AppealData, ReasonableExcuse, UserRequest}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import services.monitoring.JsonAuditModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.{SessionKeys, UUIDGenerator}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class AppealServiceSpec extends SpecBase {
  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  val mockUUIDGenerator: UUIDGenerator = mock(classOf[UUIDGenerator])
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val fakeRequestForCrimeJourney: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.dateOfCrime -> "2022-01-01",
    SessionKeys.journeyId -> "1234")
  )

  val fakeRequestForOtherJourney: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "other",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.whenDidBecomeUnable -> "2022-01-01",
    SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
    SessionKeys.journeyId -> "1234",
    SessionKeys.isUploadEvidence -> "yes")
  )

  val fakeRequestForOtherJourneyDeclinedUploads: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "other",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.whenDidBecomeUnable -> "2022-01-01",
    SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
    SessionKeys.journeyId -> "1234",
    SessionKeys.isUploadEvidence -> "no"
  ))

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "dueDate": "2020-02-07T13:00:00",
      | "dateCommunicationSent": "2020-02-08T13:00:00"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPP: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_PAYMENT",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "dueDate": "2020-02-07T13:00:00",
      | "dateCommunicationSent": "2020-02-08T13:00:00"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPPAdditional: JsValue = Json.parse(
    """
      |{
      | "type": "ADDITIONAL",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "dueDate": "2020-02-07T13:00:00",
      | "dateCommunicationSent": "2020-02-08T13:00:00"
      |}
      |""".stripMargin)

  class Setup {
    reset(mockPenaltiesConnector, mockDateTimeHelper, mockAuditService, mockUploadJourneyRepository)
    val service: AppealService =
      new AppealService(mockPenaltiesConnector, appConfig, mockDateTimeHelper, mockAuditService, mockUUIDGenerator, mockUploadJourneyRepository)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
      2020, 2, 1, 0, 0, 0))
  }

  "validatePenaltyIdForEnrolmentKey" should {
    "return None when the connector returns None" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
          new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(Json.parse("{}"))))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
          new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return Some when the connector returns Json that is parsable to a model" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJson)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = false, isAdditional = false)(
        new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }

    "return Some when the connector returns Json that is parsable to a model for LPP" in new Setup {

      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPP)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = false)(
        new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }

    "return Some when the connector returns Json that is parsable to a model for LPP - Additional penalty" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(appealDataAsJsonLPPAdditional)))

      val result: Future[Option[AppealData]] = service.validatePenaltyIdForEnrolmentKey("1234", isLPP = true, isAdditional = true)(
        new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
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
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(None))
      val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
      result shouldBe Right((): Unit)
      verify(mockAuditService, times(1)).audit(ArgumentMatchers.any[JsonAuditModel])(ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any())
    }

    "parse the session keys into a model and audit the response - for duplicate file upload" in new Setup {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      val uploadAsDuplicate: UploadJourney = UploadJourney(
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
      val uploadAsDuplicate2: UploadJourney = uploadAsDuplicate.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE)
      val failedUpload: UploadJourney = uploadAsDuplicate.copy(reference = "ref3", fileStatus = UploadStatusEnum.FAILED)
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(Some(Seq(uploadAsDuplicate, uploadAsDuplicate2, failedUpload))))
      val result: Either[Int, Unit] = await(service.submitAppeal("other")(fakeRequestForOtherJourney, implicitly, implicitly))
      result shouldBe Right((): Unit)

      verify(mockAuditService, times(2)).audit(ArgumentMatchers.any[JsonAuditModel])(ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any())
    }

    "parse the session keys into a model and audit the response - removing file uploads if the user selected no to uploading files" in new Setup {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      val uploadAsDuplicate: UploadJourney = UploadJourney(
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
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(Some(Seq(uploadAsDuplicate))))
      val result: Either[Int, Unit] = await(service.submitAppeal("other")(fakeRequestForOtherJourneyDeclinedUploads, implicitly, implicitly))
      result shouldBe Right((): Unit)

      verify(mockAuditService, times(1)).audit(ArgumentMatchers.any[JsonAuditModel])(ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any())
    }

    "ignore FAILED or WAITING files for audit" in new Setup {
      val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      val auditCapture:ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])
      val uploadAsDuplicate: UploadJourney = UploadJourney(
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
      when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockUploadJourneyRepository.getUploadsForJourney(any()))
        .thenReturn(Future.successful(Some(Seq(uploadAsDuplicate))))
      val result: Either[Int, Unit] = await(service.submitAppeal("other")(fakeRequestForOtherJourney, implicitly, implicitly))
      result shouldBe Right((): Unit)

      verify(mockAuditService, times(1)).audit(auditCapture.capture())(ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any())
      (auditCapture.getValue.detail \ "appealInformation" \ "uploadedFiles").as[List[JsValue]] shouldBe Seq.empty
    }

    "return false" when {
      "the connector returns a non-200 response" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(BAD_GATEWAY, "")))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.successful(None))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(BAD_GATEWAY)
      }

      "the connector throws an exception" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.failed(new Exception("I failed.")))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.successful(None))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }

      "the repository throws and exception" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))
        when(mockUploadJourneyRepository.getUploadsForJourney(any()))
          .thenReturn(Future.failed(new Exception("I failed.")))
        val result: Either[Int, Unit] = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe Left(INTERNAL_SERVER_ERROR)
      }
    }
  }

  "otherPenaltiesInTaxPeriod" should {
    "return true" when {
      "the connector returns a OK response" in new Setup {
        when(mockPenaltiesConnector.getOtherPenaltiesInTaxPeriod(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val result: Boolean = await(service.otherPenaltiesInTaxPeriod("penaltyId", isLPP = false)(fakeRequestForCrimeJourney, implicitly, implicitly))

        result shouldBe true
      }
    }

    "return false" when {
      "the connector returns a NO_CONTENT response" in new Setup {
        when(mockPenaltiesConnector.getOtherPenaltiesInTaxPeriod(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val result: Boolean = await(service.otherPenaltiesInTaxPeriod("penaltyId", isLPP = false)(fakeRequestForCrimeJourney, implicitly, implicitly))

        result shouldBe false
      }

      "the connector throws an exception" in new Setup {
        when(mockPenaltiesConnector.getOtherPenaltiesInTaxPeriod(any(), any(), any())(any(), any()))
          .thenReturn(Future.failed(new Exception("I failed.")))

        val result: Boolean = await(service.otherPenaltiesInTaxPeriod("penaltyId", isLPP = false)(fakeRequestForCrimeJourney, implicitly, implicitly))

        result shouldBe false
      }
    }
  }

  "sendAuditIfDuplicatesExist" should {
    "not send any audit if no duplicate exists" in new Setup {
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
          checksum = "check12345678",
          size = 987
        ))
      )
      service.sendAuditIfDuplicatesExist(Some(Seq(uploadAsReady)))(fakeRequestForOtherJourney, implicitly, implicitly)
      verify(mockAuditService, times(0)).audit(any())(any(), any(), any())
    }

    "not send any audit if no uploads exist" in new Setup {
      service.sendAuditIfDuplicatesExist(None)(fakeRequestForOtherJourney, implicitly, implicitly)
      verify(mockAuditService, times(0)).audit(any())(any(), any(), any())
    }

    "send an audit and filter out WAITING documents" in new Setup {
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
          checksum = "check12345678",
          size = 987
        ))
      )
      val duplicateUpload: UploadJourney = uploadAsReady.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE)
      val waitingUpload: UploadJourney = uploadAsReady.copy(reference = "ref3", fileStatus = UploadStatusEnum.WAITING, downloadUrl = None, uploadDetails = None)
      when(mockAuditService.getAllDuplicateUploadsForAppealSubmission(any()))
        .thenReturn(Json.obj("mocked" -> "value"))
      service.sendAuditIfDuplicatesExist(Some(Seq(uploadAsReady, duplicateUpload, waitingUpload)))(fakeRequestForOtherJourney, implicitly, implicitly)
      verify(mockAuditService, times(1)).audit(any())(any(), any(), any())
    }

    "send an audit when duplicates exist" in new Setup {
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
          checksum = "check12345678",
          size = 987
        ))
      )
      val duplicateUpload: UploadJourney = uploadAsReady.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE)
      when(mockAuditService.getAllDuplicateUploadsForAppealSubmission(any()))
        .thenReturn(Json.obj("mocked" -> "value"))
      service.sendAuditIfDuplicatesExist(Some(Seq(uploadAsReady, duplicateUpload)))(fakeRequestForOtherJourney, implicitly, implicitly)
      verify(mockAuditService, times(1)).audit(any())(any(), any(), any())
    }
  }
}
