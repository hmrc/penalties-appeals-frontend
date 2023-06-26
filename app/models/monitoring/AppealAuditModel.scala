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

package models.monitoring

import models.UserRequest
import models.appeals._
import models.appeals.submission._
import models.upload.UploadJourney
import play.api.libs.json._
import services.monitoring.JsonAuditModel
import utils.JsonUtils
import utils.Logger.logger

case class AppealAuditModel(appealSubmission: AppealSubmission, penaltyType: AuditPenaltyTypeEnum.Value,
                            correlationId: String, optUploads: Option[Seq[UploadJourney]], caseId: String,
                            penaltyNumber: String)
                           (implicit request: UserRequest[_]) extends JsonAuditModel with JsonUtils {
  implicit val agentDetailsFormatter: OFormat[AgentDetails] = AgentDetails.format

  override val auditType: String = "PenaltyAppealSubmitted"
  override val transactionName: String = "penalties-appeal-submitted"
  val appealPayload: JsObject = appealInformationJsonObj(appealSubmission)
  override val detail: JsValue = jsonObjNoNulls(
    "submittedBy" -> appealSubmission.appealSubmittedBy,
    "taxIdentifier" -> request.vrn,
    "identifierType" -> "VRN",
    "agentDetails" -> Json.toJson(appealSubmission.agentDetails),
    "penaltyType" -> penaltyType,
    "penaltyNumber" -> penaltyNumber,
    "appealInformation" -> appealPayload,
    "correlationId"-> correlationId,
    "caseId" -> caseId
  )

  logger.debug(s"[AppealAuditModel] [appeal Payload] $appealPayload")

  //scalastyle:off
  def appealInformationJsonObj(appealSubmission: AppealSubmission): JsObject = {
    appealSubmission.appealInformation match {
      case bereavement if bereavement.isInstanceOf[BereavementAppealInformation] =>
        val appealInfo = bereavement.asInstanceOf[BereavementAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
        "lateAppeal" -> appealInfo.lateAppeal,
        "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
      )
      case crime if crime.isInstanceOf[CrimeAppealInformation] =>
        val appealInfo = crime.asInstanceOf[CrimeAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "reportedIssue" -> appealInfo.reportedIssueToPolice,
        "startDateOfEvent" -> appealInfo.startDateOfEvent,
        "lateAppeal" -> appealInfo.lateAppeal,
        "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
      )
      case fireOrFlood if fireOrFlood.isInstanceOf[FireOrFloodAppealInformation] =>
        val appealInfo = fireOrFlood.asInstanceOf[FireOrFloodAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
        )
      case lossOfStaff if lossOfStaff.isInstanceOf[LossOfStaffAppealInformation] =>
        val appealInfo = lossOfStaff.asInstanceOf[LossOfStaffAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
            "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
        )
      case technicalIssues if technicalIssues.isInstanceOf[TechnicalIssuesAppealInformation] =>
        val appealInfo = technicalIssues.asInstanceOf[TechnicalIssuesAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "endDateOfEvent" -> appealInfo.endDateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
        )
      case health if health.isInstanceOf[HealthAppealInformation] =>
        val appealInfo = health.asInstanceOf[HealthAppealInformation]
        val healthReason = if(appealInfo.hospitalStayInvolved) "unexpectedHospitalStay" else "seriousOrLifeThreateningIllHealth"
        jsonObjNoNulls(
          "type" -> healthReason,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "endDateOfEvent" -> appealInfo.endDateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
        )
      case other if other.isInstanceOf[OtherAppealInformation] =>
        val appealInfo = other.asInstanceOf[OtherAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "statement" -> appealInfo.statement,
          "noOfUploadedFiles" -> s"${optUploads.map(_.size).getOrElse(0)}",
          "uploadedFiles" -> {
            if(optUploads.isDefined) {
              optUploads.get.map {
                upload => {
                  Json.obj(
                    "upscanReference" -> upload.reference,
                    "uploadTimestamp" -> upload.uploadDetails.get.uploadTimestamp,
                    "fileName" -> upload.uploadDetails.get.fileName,
                    "checksum" -> upload.uploadDetails.get.checksum,
                    "fileMimeType" -> upload.uploadDetails.get.fileMimeType,
                    "downloadUrl" -> upload.downloadUrl.get
                  )
                }
              }
            } else None
          },
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> (if(appealInfo.lateAppeal) appealInfo.lateAppealReason else None)
        )
      case obligation if obligation.isInstanceOf[ObligationAppealInformation] =>
        val appealInfo = obligation.asInstanceOf[ObligationAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.reasonableExcuse,
          "statement" -> appealInfo.statement,
          "noOfUploadedFiles" -> s"${optUploads.map(_.size).getOrElse(0)}",
          "uploadedFiles" -> {
            if(optUploads.isDefined) {
              optUploads.get.map {
                upload => {
                  Json.obj(
                    "upscanReference" -> upload.reference,
                    "uploadTimestamp" -> upload.uploadDetails.get.uploadTimestamp,
                    "fileName" -> upload.uploadDetails.get.fileName,
                    "checksum" -> upload.uploadDetails.get.checksum,
                    "fileMimeType" -> upload.uploadDetails.get.fileMimeType,
                    "downloadUrl" -> upload.downloadUrl.get
                  )
                }
              }
            } else None
          }
      )
    }
  }
}
