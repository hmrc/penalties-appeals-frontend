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

package models.appeals

import models.appeals.submission._
import models.upload.UploadJourney
import models.{PenaltyTypeEnum, UserRequest}
import play.api.libs.json._
import utils.SessionKeys

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

case class AppealSubmission(
                             sourceSystem: String,
                             taxRegime: String,
                             customerReferenceNo: String,
                             dateOfAppeal: LocalDateTime,
                             isLPP: Boolean,
                             appealSubmittedBy: String,
                             agentDetails: Option[AgentDetails],
                             appealInformation: AppealInformation
                           )

object AppealSubmission {
  implicit val agentDetailsFormatter: OFormat[AgentDetails] = AgentDetails.format


  def parseAppealInformationToJson(payload: AppealInformation): JsValue = {
    payload.reasonableExcuse match {
      case "bereavement" => Json.toJson(payload.asInstanceOf[BereavementAppealInformation])(BereavementAppealInformation.bereavementAppealWrites)
      case "crime" => Json.toJson(payload.asInstanceOf[CrimeAppealInformation])(CrimeAppealInformation.crimeAppealWrites)
      case "fireandflood" => Json.toJson(payload.asInstanceOf[FireOrFloodAppealInformation])(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      case "lossOfEssentialStaff" => Json.toJson(payload.asInstanceOf[LossOfStaffAppealInformation])(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
      case "technicalIssue" => Json.toJson(
        payload.asInstanceOf[TechnicalIssuesAppealInformation])(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
      case "health" => Json.toJson(payload.asInstanceOf[HealthAppealInformation])(HealthAppealInformation.healthAppealWrites)
      case "other" => Json.toJson(payload.asInstanceOf[OtherAppealInformation])(OtherAppealInformation.otherAppealInformationWrites)
      case "obligation" => Json.toJson(payload.asInstanceOf[ObligationAppealInformation])(ObligationAppealInformation.obligationAppealInformationWrites)
    }
  }

  //scalastyle:off
  def constructModelBasedOnReasonableExcuse(reasonableExcuse: String, isLateAppeal: Boolean, agentReferenceNo: Option[String],
                                            uploadedFiles: Option[Seq[UploadJourney]])
                                           (implicit userRequest: UserRequest[_]): AppealSubmission = {
    val isLPP: Boolean = !userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission)
    val isAgentSubmission: Boolean = userRequest.isAgent
    val isClientResponsibleForSubmission: Option[Boolean] = if (isLPP && isAgentSubmission) Some(true) else userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client")
    val isClientResponsibleForLateSubmission: Option[Boolean] = if (isLPP && isAgentSubmission) Some(true)
    else if (userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).contains("agent")) {
      userRequest.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).map(_ == "client")
    } else None

    def baseAppealSubmission(appealInfo: AppealInformation) = AppealSubmission(
      sourceSystem = "MDTP",
      taxRegime = "VAT",
      customerReferenceNo = s"VRN${userRequest.vrn}",
      dateOfAppeal = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
      isLPP = isLPP,
      appealSubmittedBy = if (isAgentSubmission) "agent" else "customer",
      agentDetails = constructAgentDetails(agentReferenceNo),
      appealInformation = appealInfo
    )

    reasonableExcuse match {
      case "bereavement" =>
        baseAppealSubmission(BereavementAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidThePersonDie).get.atStartOfDay(),
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "crime" =>
        baseAppealSubmission(CrimeAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateOfCrime).get.atStartOfDay(),
            reportedIssueToPolice = userRequest.answers.getAnswer[String](SessionKeys.hasCrimeBeenReportedToPolice).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "fireOrFlood" =>
        baseAppealSubmission(FireOrFloodAppealInformation(
            reasonableExcuse = "fireandflood", //API spec outlines this - how can it be a fire AND flood? TODO: may change later
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateOfFireOrFlood).get.atStartOfDay(),
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "lossOfStaff" =>
        baseAppealSubmission(LossOfStaffAppealInformation(
            reasonableExcuse = "lossOfEssentialStaff",
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenPersonLeftTheBusiness).get.atStartOfDay(),
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "technicalIssues" =>
        baseAppealSubmission(TechnicalIssuesAppealInformation(
            reasonableExcuse = "technicalIssue",
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesBegin).get.atStartOfDay(),
            endDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesEnd).get.atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS),
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "health" =>
        val isHospitalStay = userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).get == "yes"
        val isOngoingHospitalStay = userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded).contains("no")
        baseAppealSubmission(HealthAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            hospitalStayInvolved = isHospitalStay,
            startDateOfEvent = (if (isHospitalStay) userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted) else userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueHappened)).map(_.atStartOfDay()),
            endDateOfEvent = if (isOngoingHospitalStay) None else userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueEnded).map(_.atStartOfDay().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS)),
            eventOngoing = isOngoingHospitalStay,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission
          )
        )

      case "other" =>
        baseAppealSubmission(OtherAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            startDateOfEvent = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidBecomeUnable).get.atStartOfDay(),
            statement = userRequest.answers.getAnswer[String](SessionKeys.whyReturnSubmittedLate),
            supportingEvidence = uploadedFiles.fold[Option[Evidence]](None)(files => if (files.isEmpty) None else Some(Evidence(files.size))),
            lateAppeal = isLateAppeal,
            lateAppealReason = if (isLateAppeal) userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason) else None,
            isClientResponsibleForSubmission = isClientResponsibleForSubmission,
            isClientResponsibleForLateSubmission = isClientResponsibleForLateSubmission,
            uploadedFiles = if (uploadedFiles.isDefined) uploadedFiles else None
          )
        )
      case "obligation" =>
        baseAppealSubmission(ObligationAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.answers.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get,
            statement = userRequest.answers.getAnswer[String](SessionKeys.otherRelevantInformation),
            supportingEvidence = uploadedFiles.fold[Option[Evidence]](None)(files => if (files.isEmpty) None else Some(Evidence(files.size))),
            isClientResponsibleForSubmission = None,
            isClientResponsibleForLateSubmission = None,
            uploadedFiles = if (uploadedFiles.isDefined) uploadedFiles else None
          )
        )
    }
  }

  private def constructAgentDetails(agentReferenceNo: Option[String])(implicit userRequest: UserRequest[_]): Option[AgentDetails] = {
    if (userRequest.isAgent) Some(
      AgentDetails(
        agentReferenceNo = agentReferenceNo.get,
        isExcuseRelatedToAgent = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).contains("agent") &&
          userRequest.session.get(SessionKeys.whatCausedYouToMissTheDeadline).contains("agent")
      )) else None
  }

  implicit val writes: Writes[AppealSubmission] = (appealSubmission: AppealSubmission) => {
    Json.obj(
      "sourceSystem" -> appealSubmission.sourceSystem,
      "taxRegime" -> appealSubmission.taxRegime,
      "customerReferenceNo" -> appealSubmission.customerReferenceNo,
      "dateOfAppeal" -> appealSubmission.dateOfAppeal,
      "isLPP" -> appealSubmission.isLPP,
      "appealSubmittedBy" -> appealSubmission.appealSubmittedBy,
      "appealInformation" -> parseAppealInformationToJson(appealSubmission.appealInformation)
    ).deepMerge(
      appealSubmission.agentDetails.fold(
        Json.obj()
      )(
        agentDetails => Json.obj("agentDetails" -> agentDetails)
      )
    )
  }
}