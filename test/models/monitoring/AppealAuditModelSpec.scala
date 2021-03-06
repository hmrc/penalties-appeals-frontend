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

package models.monitoring

import base.SpecBase
import models.UserRequest
import models.appeals._
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import play.api.libs.json.Json
import play.api.mvc.AnyContent

import java.time.LocalDateTime

class AppealAuditModelSpec extends SpecBase {

  val uploadJourneyModel: UploadJourney = UploadJourney(reference = "xyz", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("xyz.com"),
    uploadDetails =
      Some(UploadDetails(
        fileName = "filename.txt",
        fileMimeType = "txt",
        uploadTimestamp = LocalDateTime.of(2020,1,1,1,1),
        checksum = "abcde", size = 1)),
    failureDetails = None, lastUpdated = LocalDateTime.of(2021,2,2,2,2))

  val uploadJourneyModel2: UploadJourney = UploadJourney(reference = "abc", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("abc.com"),
    uploadDetails =
      Some(UploadDetails(
        fileName = "filename2.pdf",
        fileMimeType = "pdf",
        uploadTimestamp = LocalDateTime.of(2020,3,3,3,3),
        checksum = "zyxwv", size = 1)),
    failureDetails = None, lastUpdated = LocalDateTime.of(2021,4,4,4,4))

  "AppealAuditModel" must {

    def appealSubmission(appealInformation: AppealInformation): AppealSubmission = AppealSubmission(
      sourceSystem = "MDTP",
      taxRegime = "VAT",
      customerReferenceNo = "VRN1234567890",
      dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
      isLPP = true,
      appealSubmittedBy = "client",
      agentDetails = None,
      appealInformation
    )

    def appealAgentSubmission(appealInformation: AppealInformation): AppealSubmission = appealSubmission(appealInformation)
      .copy(appealSubmittedBy = "agent")

    val bereavementAppealInformation: BereavementAppealInformation = BereavementAppealInformation(
      reasonableExcuse = "bereavement",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val bereavementLateAppealInformation = bereavementAppealInformation.copy(lateAppeal = true, lateAppealReason = Some("this is a very good reason"))

    val bereavementAgentAppealInformation = bereavementAppealInformation.copy(
      isClientResponsibleForSubmission = Some(true), isClientResponsibleForLateSubmission = Some(true))

    val crimeAppealInformation: CrimeAppealInformation = CrimeAppealInformation(
      reasonableExcuse = "crime",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      reportedIssueToPolice = true,
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val fireOrFloodAppealInformation: FireOrFloodAppealInformation = FireOrFloodAppealInformation(
      reasonableExcuse = "fireOrFlood",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val lossOfStaffAppealInformation: LossOfStaffAppealInformation = LossOfStaffAppealInformation(
      reasonableExcuse = "lossOfStaff",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val technicalIssuesAppealInformation: TechnicalIssuesAppealInformation = TechnicalIssuesAppealInformation(
      reasonableExcuse = "technicalIssues",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      endDateOfEvent = "2021-04-25T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val healthAppealInformation: HealthAppealInformation = HealthAppealInformation(
      reasonableExcuse = "health",
      honestyDeclaration = true,
      hospitalStayInvolved = true,
      startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
      endDateOfEvent = Some("2021-04-25T18:25:43.511Z"),
      eventOngoing = false,
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None
    )

    val healthAppealInformationOngoingHospitalStay = healthAppealInformation.copy(endDateOfEvent = None, eventOngoing = true)
    val healthAppealInformationNoHospitalStay = healthAppealInformation.copy(
      startDateOfEvent = Some("2021-04-23T18:25:43.511Z"), endDateOfEvent = None, eventOngoing = false, hospitalStayInvolved = false)

    val otherAppealInformation: OtherAppealInformation = OtherAppealInformation(
      reasonableExcuse = "other",
      honestyDeclaration = true,
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = Some("this is a reason"),
      supportingEvidence = None,
      lateAppeal = false,
      lateAppealReason = None,
      isClientResponsibleForSubmission = None,
      isClientResponsibleForLateSubmission = None,
      uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
    )
    val otherAppealInformationWithEvidence = otherAppealInformation.copy(
      supportingEvidence = Some(Evidence(
        noOfUploadedFiles = 1
      ))
    )

    val obligationAppealInformation: ObligationAppealInformation = ObligationAppealInformation(
      reasonableExcuse = "obligation", honestyDeclaration = true, statement = Some("this is another reason"),
      supportingEvidence = None, None, None, Some(Seq(uploadJourneyModel, uploadJourneyModel2))
    )
    val obligationAppealInformationWithEvidence = obligationAppealInformation.copy(
      supportingEvidence = Some(Evidence(
        noOfUploadedFiles = 1
      ))
    )

    val seqOfUploads: Seq[UploadJourney] = Seq(
      UploadJourney(
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
      ),
      UploadJourney(
        reference = "ref2",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file"),
        uploadDetails = Some(UploadDetails(
          fileName = "file2.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
          checksum = "check12345678",
          size = 987
        ))
      )
    )

    implicit val userRequest: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeysAndHonestyDeclarationSet)
    val correlationId = "someUUID"
    val lppBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LPP", correlationId, None)
    val lppCrimeModel = AppealAuditModel(appealSubmission(crimeAppealInformation), "LPP", correlationId, None)
    val lppFireOrFloodModel = AppealAuditModel(appealSubmission(fireOrFloodAppealInformation), "LPP", correlationId, None)
    val lppHealthModel = AppealAuditModel(appealSubmission(healthAppealInformation), "LPP", correlationId, None)
    val lppHealthOngoingModel = AppealAuditModel(appealSubmission(healthAppealInformationOngoingHospitalStay), "LPP", correlationId, None)
    val lppHealthNoHospitalModel = AppealAuditModel(appealSubmission(healthAppealInformationNoHospitalStay), "LPP", correlationId, None)
    val lppLossOfStaffModel = AppealAuditModel(appealSubmission(lossOfStaffAppealInformation), "LPP", correlationId, None)
    val lppTechnicalIssuesModel = AppealAuditModel(appealSubmission(technicalIssuesAppealInformation), "LPP", correlationId, None)
    val lppOtherModel = AppealAuditModel(appealSubmission(otherAppealInformation), "LPP", correlationId, None)
    val lppOtherModelWithEvidence = AppealAuditModel(appealSubmission(otherAppealInformationWithEvidence), "LPP", correlationId, Some(seqOfUploads))
    val lppObligationModel = AppealAuditModel(appealSubmission(obligationAppealInformation), "LPP", correlationId, None)
    val lppObligationWithEvidenceModel = AppealAuditModel(appealSubmission(obligationAppealInformationWithEvidence), "LPP", correlationId, Some(seqOfUploads))

    val lppAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAppealInformation), "LPP", correlationId, None)
    val lppBereavementLateAppealModel = AppealAuditModel(appealSubmission(bereavementLateAppealInformation), "LPP", correlationId, None)

    val lspBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LSP", correlationId, None)
    val lspAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "LSP", correlationId, None)

    val lsppBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LSPP", correlationId, None)
    val lsppAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "LSPP", correlationId, None)

    val additionalBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "Additional", correlationId, None)
    val additionalAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "Additional", correlationId, None)

    "have the correct auditType" in {
      lppBereavementModel.auditType shouldBe "PenaltyAppealSubmitted"
    }

    "have the correct transactionName" in {
      lppBereavementModel.transactionName shouldBe "penalties-appeal-submitted"
    }

    "output the correct details for a lpp bereavement submission with correlationID" in {
      lppBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct audit details for a lpp crime submission with correlationID" in {
      lppCrimeModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "crime",
          "reportedIssue" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp fireOrFlood submission with correlationID" in {
      lppFireOrFloodModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "fireOrFlood",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp lossOfStaff submission with correlationID" in {
      lppLossOfStaffModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "lossOfStaff",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp technicalIssues submission with correlationID" in {
      lppTechnicalIssuesModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "technicalIssues",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "endDateOfEvent" -> "2021-04-25T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp health with hospital stay submission with correlationID" in {
      lppHealthModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "unexpectedHospitalStay",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "endDateOfEvent" -> "2021-04-25T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp health with no/ongoing hospital stay submission with correlationID" in {
      lppHealthNoHospitalModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "seriousOrLifeThreateningIllHealth",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )

      lppHealthOngoingModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "unexpectedHospitalStay",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp other submission without evidence with correlationID" in {
      lppOtherModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "other",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "statement" -> "this is a reason",
          "noOfUploadedFiles" -> "0",
          "lateAppeal" -> false
        )
      )
    }
    "output the correct details for a lpp other submission with evidence with correlationID" in {
      lppOtherModelWithEvidence.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "appealInformation" -> Json.obj(
          "type" -> "other",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "statement" -> "this is a reason",
          "noOfUploadedFiles" -> "2",
          "uploadedFiles" -> Json.arr(
            Json.obj(
              "upscanReference" -> "ref1",
              "uploadTimestamp" -> "2018-04-24T09:30:00",
              "fileName" -> "file1.txt",
              "checksum" -> "check12345678",
              "fileMimeType" -> "text/plain",
              "downloadUrl" -> "download.file"
            ),
            Json.obj(
              "upscanReference" -> "ref2",
              "uploadTimestamp" -> "2018-04-24T09:30:00",
              "fileName" -> "file2.txt",
              "checksum" -> "check12345678",
              "fileMimeType" -> "text/plain",
              "downloadUrl" -> "download.file"
            )
          ),
          "lateAppeal" -> false
        ),
        "correlationId" -> "someUUID"
      )
    }

    "output the correct details for a lpp obligation submission with correlationID" in {
      lppObligationModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "obligation",
          "statement" -> "this is another reason",
          "noOfUploadedFiles" -> "0"
        )
      )
    }

    "output the correct details for a lpp obligation submission with evidence with correlationID" in {
      lppObligationWithEvidenceModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "appealInformation" -> Json.obj(
          "type" -> "obligation",
          "statement" -> "this is another reason",
          "noOfUploadedFiles" -> "2",
          "uploadedFiles" -> Json.arr(
            Json.obj(
              "upscanReference" -> "ref1",
              "uploadTimestamp" -> "2018-04-24T09:30:00",
              "fileName" -> "file1.txt",
              "checksum" -> "check12345678",
              "fileMimeType" -> "text/plain",
              "downloadUrl" -> "download.file"
            ),
            Json.obj(
              "upscanReference" -> "ref2",
              "uploadTimestamp" -> "2018-04-24T09:30:00",
              "fileName" -> "file2.txt",
              "checksum" -> "check12345678",
              "fileMimeType" -> "text/plain",
              "downloadUrl" -> "download.file"
            )
          )
        ),
        "correlationId" -> "someUUID"
      )
    }

    "output the correct details for a lpp appeal submission by an agent with correlationID" in {
      lppAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp late bereavement appeal submission with correlationID" in {
      lppBereavementLateAppealModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "this is a very good reason"
        )
      )
    }

    "output the correct details for a lsp bereavement appeal submission with correlationID" in {
      lspBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LSP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lsp appeal submission by an agent with correlationID" in {
      lspAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LSP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lspp appeal submission with correlationID" in {
      lsppBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LSPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lspp appeal submission by an agent with correlationID" in {
      lsppAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "LSPP",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a additional penalty appeal submission with correlationID" in {
      additionalBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "Additional",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a additional penalty appeal submission by an agent with correlationID" in {
      additionalAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyType" -> "Additional",
        "correlationId" -> "someUUID",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }
  }
}