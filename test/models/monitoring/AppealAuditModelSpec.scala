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

package models.monitoring

import base.SpecBase
import models.UserRequest
import models.appeals._
import play.api.libs.json.Json
import play.api.mvc.AnyContent

class AppealAuditModelSpec extends SpecBase {

  "AppealAuditModel" must {

    def appealSubmission(appealInformation: AppealInformation): AppealSubmission = AppealSubmission(
      submittedBy = "client",
      penaltyId = "123456789",
      reasonableExcuse = appealInformation.`type`,
      honestyDeclaration = true,
      appealInformation
    )

    def appealAgentSubmission(appealInformation: AppealInformation) = appealSubmission(appealInformation).copy(submittedBy = "agent")

    val bereavementAppealInformation: BereavementAppealInformation = BereavementAppealInformation(
        `type` = "bereavement",
        dateOfEvent = "2021-04-23T18:25:43.511Z",
        statement = None,
        lateAppeal = false,
        lateAppealReason = None,
        whoPlannedToSubmit = None,
        causeOfLateSubmissionAgent = None
    )

    val bereavementLateAppealInformation = bereavementAppealInformation.copy(lateAppeal = true, lateAppealReason = Some("this is a very good reason"))

    val bereavementAgentAppealInformation = bereavementAppealInformation.copy(whoPlannedToSubmit = Some("agent"), causeOfLateSubmissionAgent = Some("client"))

    val crimeAppealInformation: CrimeAppealInformation = CrimeAppealInformation(
      `type` = "crime",
      dateOfEvent = "2021-04-23T18:25:43.511Z",
      reportedIssue = true,
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      whoPlannedToSubmit = None,
      causeOfLateSubmissionAgent = None
    )

    val fireOrFloodAppealInformation: FireOrFloodAppealInformation = FireOrFloodAppealInformation(
      `type` = "fireOrFlood",
      dateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      whoPlannedToSubmit = None,
      causeOfLateSubmissionAgent = None
    )

    val lossOfStaffAppealInformation: LossOfStaffAppealInformation = LossOfStaffAppealInformation(
      `type` = "lossOfStaff",
      dateOfEvent = "2021-04-23T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      whoPlannedToSubmit = None,
      causeOfLateSubmissionAgent = None
    )

    val technicalIssuesAppealInformation: TechnicalIssuesAppealInformation = TechnicalIssuesAppealInformation(
      `type` = "technicalIssues",
      startDateOfEvent = "2021-04-23T18:25:43.511Z",
      endDateOfEvent = "2021-04-25T18:25:43.511Z",
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      whoPlannedToSubmit = None,
      causeOfLateSubmissionAgent = None
    )

    val healthAppealInformation: HealthAppealInformation = HealthAppealInformation(
      `type` = "health",
      hospitalStayInvolved = true,
      dateOfEvent = None,
      startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
      endDateOfEvent = Some("2021-04-25T18:25:43.511Z"),
      eventOngoing = false,
      statement = None,
      lateAppeal = false,
      lateAppealReason = None,
      whoPlannedToSubmit = None,
      causeOfLateSubmissionAgent = None
    )

    val healthAppealInformationOngoingHospitalStay = healthAppealInformation.copy(endDateOfEvent = None, eventOngoing = true)
    val healthAppealInformationNoHospitalStay = healthAppealInformation.copy(
      dateOfEvent = Some("2021-04-23T18:25:43.511Z"), startDateOfEvent = None, endDateOfEvent = None, eventOngoing = false, hospitalStayInvolved = false)

    val otherAppealInformation: OtherAppealInformation = OtherAppealInformation(
        `type` = "other",
        dateOfEvent = "2021-04-23T18:25:43.511Z",
        statement = Some("this is a reason"),
        supportingEvidence = None,
        lateAppeal = false,
        lateAppealReason = None,
        whoPlannedToSubmit = None,
        causeOfLateSubmissionAgent = None
    )
    val otherAppealInformationWithEvidence = otherAppealInformation.copy(
      supportingEvidence = Some(Evidence(
        noOfUploadedFiles = 1, referenceId = "ref1"
      ))
    )

    val obligationAppealInformation: ObligationAppealInformation = ObligationAppealInformation(
      `type` = "obligation", statement = Some("this is another reason"), supportingEvidence = None
    )
    val obligationAppealInformationWithEvidence = obligationAppealInformation.copy(
      supportingEvidence = Some(Evidence(
        noOfUploadedFiles = 1, referenceId = "ref1"
      ))
    )

    implicit val userRequest: UserRequest[AnyContent] = userRequestWithCorrectKeys
    val lppBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LPP")
    val lppCrimeModel = AppealAuditModel(appealSubmission(crimeAppealInformation), "LPP")
    val lppFireOrFloodModel = AppealAuditModel(appealSubmission(fireOrFloodAppealInformation), "LPP")
    val lppHealthModel = AppealAuditModel(appealSubmission(healthAppealInformation), "LPP")
    val lppHealthOngoingModel = AppealAuditModel(appealSubmission(healthAppealInformationOngoingHospitalStay), "LPP")
    val lppHealthNoHospitalModel = AppealAuditModel(appealSubmission(healthAppealInformationNoHospitalStay), "LPP")
    val lppLossOfStaffModel = AppealAuditModel(appealSubmission(lossOfStaffAppealInformation), "LPP")
    val lppTechnicalIssuesModel = AppealAuditModel(appealSubmission(technicalIssuesAppealInformation), "LPP")
    val lppOtherModel = AppealAuditModel(appealSubmission(otherAppealInformation), "LPP")
    val lppOtherModelWithEvidence = AppealAuditModel(appealSubmission(otherAppealInformationWithEvidence), "LPP")
    val lppObligationModel = AppealAuditModel(appealSubmission(obligationAppealInformation), "LPP")
    val lppObligationWithEvidenceModel = AppealAuditModel(appealSubmission(obligationAppealInformationWithEvidence), "LPP")

    val lppAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAppealInformation), "LPP")
    val lppBereavementLateAppealModel = AppealAuditModel(appealSubmission(bereavementLateAppealInformation), "LPP")

    val lspBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LSP")
    val lspAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "LSP")

    val lsppBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "LSPP")
    val lsppAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "LSPP")

    val additionalBereavementModel = AppealAuditModel(appealSubmission(bereavementAppealInformation), "Additional")
    val additionalAgentBereavementModel = AppealAuditModel(appealAgentSubmission(bereavementAgentAppealInformation), "Additional")

    "have the correct auditType" in {
      lppBereavementModel.auditType shouldBe "PenaltyAppealSubmitted"
    }

    "have the correct transactionName" in {
      lppBereavementModel.transactionName shouldBe "penalties-appeal-submitted"
    }

    "output the correct details for a lpp bereavement submission" in {
        lppBereavementModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "bereavement",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct audit details for a lpp crime submission" in {
        lppCrimeModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "crime",
            "reportedIssue" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp fireOrFlood submission" in {
        lppFireOrFloodModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "fireOrFlood",
            "startDateOfEvent"-> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp lossOfStaff submission" in {
        lppLossOfStaffModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "lossOfStaff",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp technicalIssues submission" in {
        lppTechnicalIssuesModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "technicalIssues",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-25T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp health with hospital stay submission" in {
        lppHealthModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "health",
            "hospitalStayInvolved" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-25T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp health with ongoing hospital stay submission" in {
        lppHealthOngoingModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "health",
            "hospitalStayInvolved" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp health with no hospital stay submission" in {
        lppHealthNoHospitalModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "health",
            "hospitalStayInvolved" -> false,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false
          )
        )
      }

    "output the correct details for a lpp other submission without evidence" in {
      lppOtherModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LPP",
        "appealInformation" -> Json.obj(
          "type" -> "other",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "statement" -> "this is a reason",
          "noOfUploadedFiles" -> "0",
          "lateAppeal" -> false
        )
      )
    }
      "output the correct details for a lpp other submission with evidence" in {
        lppOtherModelWithEvidence.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "other",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "this is a reason",
            "noOfUploadedFiles" -> "1",
            "uploadedFiles" -> Json.obj(
              "upscanReference" -> "12345",
              "uploadTimestamp" -> "2021-04-23T18:25:43.511Z",
              "fileName" -> "certificate.png",
              "checksum" -> "12345",
              "fileMimeType" -> "image/png",
              "downloadUrl" -> "www.test.com"
            ),
            "lateAppeal" -> false
          )
        )
      }

      "output the correct details for a lpp obligation submission" in {
        lppObligationModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "obligation",
            "statement" -> "this is another reason",
            "noOfUploadedFiles" -> "0"
          )
        )
      }

      "output the correct details for a lpp obligation submission with evidence" in {
        lppObligationWithEvidenceModel.detail shouldBe Json.obj(
          "submittedBy" -> "client",
          "taxIdentifier" -> "123456789",
          "identifierType" -> "VRN",
          "penaltyId" -> "123456789",
          "penaltyType" -> "LPP",
          "appealInformation" -> Json.obj(
            "type" -> "obligation",
            "statement" -> "this is another reason",
            "noOfUploadedFiles" -> "1",
            "uploadedFiles" -> Json.obj(
              "upscanReference" -> "12345",
              "uploadTimestamp" -> "2021-04-23T18:25:43.511Z",
              "fileName" -> "certificate.png",
              "checksum" -> "12345",
              "fileMimeType" -> "image/png",
              "downloadUrl" -> "www.test.com"
            )
          )
        )
      }

    "output the correct details for a lpp appeal submission by an agent" in {
      lppAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LPP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lpp late bereavement appeal submission" in {
      lppBereavementLateAppealModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LPP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "this is a very good reason"
        )
      )
    }

    "output the correct details for a lsp bereavement appeal submission" in {
      lspBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LSP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lsp appeal submission by an agent" in {
      lspAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LSP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lspp appeal submission" in {
      lsppBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LSPP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a lspp appeal submission by an agent" in {
      lsppAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "LSPP",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a additional penalty appeal submission" in {
      additionalBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "Additional",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }

    "output the correct details for a additional penalty appeal submission by an agent" in {
      additionalAgentBereavementModel.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyId" -> "123456789",
        "penaltyType" -> "Additional",
        "appealInformation" -> Json.obj(
          "type" -> "bereavement",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> false
        )
      )
    }
  }
}