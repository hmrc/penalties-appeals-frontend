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

import connectors.HeaderGenerator
import models.UserRequest
import models.appeals._
import play.api.libs.json._
import services.monitoring.JsonAuditModel
import uk.gov.hmrc.http.HeaderCarrier
import utils.JsonUtils

case class AppealAuditModel(appealSubmission: AppealSubmission, penaltyType: String,headerGenerator: HeaderGenerator)
                           (implicit request: UserRequest[_], headerCarrier: HeaderCarrier) extends JsonAuditModel with JsonUtils {

  override val auditType: String = "PenaltyAppealSubmitted"
  override val transactionName: String = "penalties-appeal-submitted"
  override val detail: JsValue = jsonObjNoNulls(
    "submittedBy" -> appealSubmission.submittedBy,
    "taxIdentifier" -> request.vrn,
    "identifierType" -> "VRN",
    "agentReferenceNumber" -> request.arn,
    "penaltyId" -> appealSubmission.penaltyId,
    "penaltyType" -> penaltyType,
    "appealInformation" -> appealInformationJsonObj(appealSubmission),
    "correlationId"-> headerGenerator.headersForPEGA().find(_._1 == "CorrelationId").map(_._2)
  )

  def appealInformationJsonObj(appealSubmission: AppealSubmission): JsObject = {
    appealSubmission.appealInformation match {
      case bereavement if bereavement.isInstanceOf[BereavementAppealInformation] =>
        val appealInfo = bereavement.asInstanceOf[BereavementAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "startDateOfEvent" -> appealInfo.dateOfEvent,
        "lateAppeal" -> appealInfo.lateAppeal,
        "lateAppealReason" -> appealInfo.lateAppealReason
      )
      case crime if crime.isInstanceOf[CrimeAppealInformation] =>
        val appealInfo = crime.asInstanceOf[CrimeAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "reportedIssue" -> appealInfo.reportedIssue,
        "startDateOfEvent" -> appealInfo.dateOfEvent,
        "lateAppeal" -> appealInfo.lateAppeal,
        "lateAppealReason" -> appealInfo.lateAppealReason
      )
      case fireOrFlood if fireOrFlood.isInstanceOf[FireOrFloodAppealInformation] =>
        val appealInfo = fireOrFlood.asInstanceOf[FireOrFloodAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "startDateOfEvent" -> appealInfo.dateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> appealInfo.lateAppealReason
        )
      case lossOfStaff if lossOfStaff.isInstanceOf[LossOfStaffAppealInformation] =>
        val appealInfo = lossOfStaff.asInstanceOf[LossOfStaffAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
            "startDateOfEvent" -> appealInfo.dateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> appealInfo.lateAppealReason
        )
      case technicalIssues if technicalIssues.isInstanceOf[TechnicalIssuesAppealInformation] =>
        val appealInfo = technicalIssues.asInstanceOf[TechnicalIssuesAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "startDateOfEvent" -> appealInfo.startDateOfEvent,
          "endDateOfEvent" -> appealInfo.endDateOfEvent,
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> appealInfo.lateAppealReason
        )
      case health if health.isInstanceOf[HealthAppealInformation] =>
        val appealInfo = health.asInstanceOf[HealthAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "hospitalStayInvolved" -> appealInfo.hospitalStayInvolved,
          "startDateOfEvent" -> {
            try appealInfo.dateOfEvent.get
            catch {
              case e: NoSuchElementException =>
                try appealInfo.startDateOfEvent.get
                catch {
                  case e: NoSuchElementException => None
                }
            }
          },
          "endDateOfEvent" -> {
            try appealInfo.endDateOfEvent.get
            catch {
              case e: NoSuchElementException => None
            }
          },
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> appealInfo.lateAppealReason
        )
      case other if other.isInstanceOf[OtherAppealInformation] =>
        val appealInfo = other.asInstanceOf[OtherAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "startDateOfEvent" -> appealInfo.dateOfEvent,
          "statement" -> appealInfo.statement,
          "noOfUploadedFiles" -> {
            try appealInfo.supportingEvidence.get.noOfUploadedFiles.toString
            catch {
              case e: NoSuchElementException => "0"
            }
          },
          "uploadedFiles" -> { //TODO: Implement actual checks with upscan
            if(appealInfo.supportingEvidence.isDefined) Json.obj(
              "upscanReference" -> "12345",
              "uploadTimestamp" -> "2021-04-23T18:25:43.511Z",
              "fileName" -> "certificate.png",
              "checksum" -> "12345",
              "fileMimeType" -> "image/png",
              "downloadUrl" -> "www.test.com"
            ) else None
          },
          "lateAppeal" -> appealInfo.lateAppeal,
          "lateAppealReason" -> appealInfo.lateAppealReason
        )
      case obligation if obligation.isInstanceOf[ObligationAppealInformation] =>
        val appealInfo = obligation.asInstanceOf[ObligationAppealInformation]
        jsonObjNoNulls(
          "type" -> appealInfo.`type`,
          "statement" -> appealInfo.statement,
          "noOfUploadedFiles" -> {
            try appealInfo.supportingEvidence.get.noOfUploadedFiles.toString
            catch {
              case e: NoSuchElementException => "0"
            }
          },
        "uploadedFiles" -> { //TODO: Implement actual checks with upscan
          if(appealInfo.supportingEvidence.isDefined) Json.obj(
            "upscanReference" -> "12345",
            "uploadTimestamp" -> "2021-04-23T18:25:43.511Z",
            "fileName" -> "certificate.png",
            "checksum" -> "12345",
            "fileMimeType" -> "image/png",
            "downloadUrl" -> "www.test.com"
          ) else None
        }
      )
    }
  }
}
