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

package models.appeals

import java.time.LocalDateTime

import models.{PenaltyTypeEnum, UserRequest}
import play.api.libs.json._
import utils.SessionKeys

sealed trait AppealInformation {
  val reasonableExcuse: String
  val honestyDeclaration: Boolean
  val statement: Option[String]
  val isClientResponsibleForSubmission: Option[Boolean]
  val isClientResponsibleForLateSubmission: Option[Boolean]
}

case class BereavementAppealInformation(
                                         reasonableExcuse: String,
                                         honestyDeclaration: Boolean,
                                         startDateOfEvent: String,
                                         statement: Option[String],
                                         lateAppeal: Boolean,
                                         lateAppealReason: Option[String],
                                         isClientResponsibleForSubmission: Option[Boolean],
                                         isClientResponsibleForLateSubmission: Option[Boolean]
                                       ) extends AppealInformation

object BereavementAppealInformation {
  implicit val bereavementAppealInformationFormatter: OFormat[BereavementAppealInformation] = Json.format[BereavementAppealInformation]

  val bereavementAppealWrites: Writes[BereavementAppealInformation] = (bereavementAppealInformation: BereavementAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> bereavementAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> bereavementAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> bereavementAppealInformation.startDateOfEvent,
      "lateAppeal" -> bereavementAppealInformation.lateAppeal
    ).deepMerge(
      bereavementAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      bereavementAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      bereavementAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      bereavementAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class CrimeAppealInformation(
                                   reasonableExcuse: String,
                                   honestyDeclaration: Boolean,
                                   startDateOfEvent: String,
                                   reportedIssueToPolice: Boolean,
                                   statement: Option[String],
                                   lateAppeal: Boolean,
                                   lateAppealReason: Option[String],
                                   isClientResponsibleForSubmission: Option[Boolean],
                                   isClientResponsibleForLateSubmission: Option[Boolean]
                                 ) extends AppealInformation

object CrimeAppealInformation {
  implicit val crimeAppealInformationFormatter: OFormat[CrimeAppealInformation] = Json.format[CrimeAppealInformation]

  val crimeAppealWrites: Writes[CrimeAppealInformation] = (crimeAppealInformation: CrimeAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> crimeAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> crimeAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> crimeAppealInformation.startDateOfEvent,
      "reportedIssueToPolice" -> crimeAppealInformation.reportedIssueToPolice,
      "lateAppeal" -> crimeAppealInformation.lateAppeal
    ).deepMerge(
      crimeAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      crimeAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      crimeAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      crimeAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class FireOrFloodAppealInformation(
                                         reasonableExcuse: String,
                                         honestyDeclaration: Boolean,
                                         startDateOfEvent: String,
                                         statement: Option[String],
                                         lateAppeal: Boolean,
                                         lateAppealReason: Option[String],
                                         isClientResponsibleForSubmission: Option[Boolean],
                                         isClientResponsibleForLateSubmission: Option[Boolean]
                                       ) extends AppealInformation

object FireOrFloodAppealInformation {
  implicit val fireOrFloodAppealInformationFormatter: OFormat[FireOrFloodAppealInformation] = Json.format[FireOrFloodAppealInformation]

  val fireOrFloodAppealWrites: Writes[FireOrFloodAppealInformation] = (fireOrFloodAppealInformation: FireOrFloodAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> fireOrFloodAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> fireOrFloodAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> fireOrFloodAppealInformation.startDateOfEvent,
      "lateAppeal" -> fireOrFloodAppealInformation.lateAppeal
    ).deepMerge(
      fireOrFloodAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      fireOrFloodAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      fireOrFloodAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      fireOrFloodAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class LossOfStaffAppealInformation(
                                         reasonableExcuse: String,
                                         honestyDeclaration: Boolean,
                                         startDateOfEvent: String,
                                         statement: Option[String],
                                         lateAppeal: Boolean,
                                         lateAppealReason: Option[String],
                                         isClientResponsibleForSubmission: Option[Boolean],
                                         isClientResponsibleForLateSubmission: Option[Boolean]
                                       ) extends AppealInformation

object LossOfStaffAppealInformation {
  implicit val lossOfStaffAppealInformationFormatter: OFormat[LossOfStaffAppealInformation] = Json.format[LossOfStaffAppealInformation]

  val lossOfStaffAppealWrites: Writes[LossOfStaffAppealInformation] = (lossOfStaffAppealInformation: LossOfStaffAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> lossOfStaffAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> lossOfStaffAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> lossOfStaffAppealInformation.startDateOfEvent,
      "lateAppeal" -> lossOfStaffAppealInformation.lateAppeal
    ).deepMerge(
      lossOfStaffAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      lossOfStaffAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      lossOfStaffAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      lossOfStaffAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class TechnicalIssuesAppealInformation(
                                             reasonableExcuse: String,
                                             honestyDeclaration: Boolean,
                                             startDateOfEvent: String,
                                             endDateOfEvent: String,
                                             statement: Option[String],
                                             lateAppeal: Boolean,
                                             lateAppealReason: Option[String],
                                             isClientResponsibleForSubmission: Option[Boolean],
                                             isClientResponsibleForLateSubmission: Option[Boolean]
                                           ) extends AppealInformation

object TechnicalIssuesAppealInformation {
  implicit val technicalIssuesAppealInformationFormatter: OFormat[TechnicalIssuesAppealInformation] = Json.format[TechnicalIssuesAppealInformation]

  val technicalIssuesAppealWrites: Writes[TechnicalIssuesAppealInformation] = (technicalIssuesAppealInformation: TechnicalIssuesAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> technicalIssuesAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> technicalIssuesAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> technicalIssuesAppealInformation.startDateOfEvent,
      "endDateOfEvent" -> technicalIssuesAppealInformation.endDateOfEvent,
      "lateAppeal" -> technicalIssuesAppealInformation.lateAppeal
    ).deepMerge(
      technicalIssuesAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      technicalIssuesAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      technicalIssuesAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      technicalIssuesAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class HealthAppealInformation(
                                    reasonableExcuse: String,
                                    honestyDeclaration: Boolean,
                                    hospitalStayInvolved: Boolean,
                                    startDateOfEvent: Option[String],
                                    endDateOfEvent: Option[String],
                                    eventOngoing: Boolean,
                                    statement: Option[String],
                                    lateAppeal: Boolean,
                                    lateAppealReason: Option[String],
                                    isClientResponsibleForSubmission: Option[Boolean],
                                    isClientResponsibleForLateSubmission: Option[Boolean]
                                  ) extends AppealInformation

object HealthAppealInformation {
  implicit val healthAppealInformationFormatter: OFormat[HealthAppealInformation] = Json.format[HealthAppealInformation]

  val healthAppealWrites: Writes[HealthAppealInformation] = (healthAppealInformation: HealthAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> healthAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> healthAppealInformation.honestyDeclaration,
      "hospitalStayInvolved" -> healthAppealInformation.hospitalStayInvolved,
      "eventOngoing" -> healthAppealInformation.eventOngoing,
      "lateAppeal" -> healthAppealInformation.lateAppeal
    ).deepMerge(
      healthAppealInformation.statement.fold(
        Json.obj()
      )(
        statement => Json.obj("statement" -> statement)
      )
    ).deepMerge(
      healthAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      (healthAppealInformation.hospitalStayInvolved, healthAppealInformation.eventOngoing) match {
        case (true, false) => {
          Json.obj(
            "startDateOfEvent" -> healthAppealInformation.startDateOfEvent.get,
            "endDateOfEvent" -> healthAppealInformation.endDateOfEvent.get
          )
        }
        case _ => {
          Json.obj(
            "startDateOfEvent" -> healthAppealInformation.startDateOfEvent.get
          )
        }
      }
    ).deepMerge(
      healthAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      healthAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class OtherAppealInformation(
                                   reasonableExcuse: String,
                                   honestyDeclaration: Boolean,
                                   startDateOfEvent: String,
                                   statement: Option[String],
                                   supportingEvidence: Option[Evidence],
                                   lateAppeal: Boolean,
                                   lateAppealReason: Option[String],
                                   isClientResponsibleForSubmission: Option[Boolean],
                                   isClientResponsibleForLateSubmission: Option[Boolean]
                                 ) extends AppealInformation

object OtherAppealInformation {
  implicit val evidenceFormatter: OFormat[Evidence] = Evidence.format
  implicit val otherAppealInformationFormatter: OFormat[OtherAppealInformation] = Json.format[OtherAppealInformation]

  val otherAppealInformationWrites: Writes[OtherAppealInformation] = (otherAppealInformation: OtherAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> otherAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> otherAppealInformation.honestyDeclaration,
      "startDateOfEvent" -> otherAppealInformation.startDateOfEvent,
      "statement" -> otherAppealInformation.statement.get,
      "lateAppeal" -> otherAppealInformation.lateAppeal
    ).deepMerge(
      otherAppealInformation.lateAppealReason.fold(
        Json.obj()
      )(
        lateAppealReason => Json.obj("lateAppealReason" -> lateAppealReason)
      )
    ).deepMerge(
      otherAppealInformation.supportingEvidence.fold(
        Json.obj()
      )(
        evidence => Json.obj("supportingEvidence" -> evidence)
      )
    ).deepMerge(
      otherAppealInformation.isClientResponsibleForSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForSubmission => Json.obj("isClientResponsibleForSubmission" -> isClientResponsibleForSubmission)
      )
    ).deepMerge(
      otherAppealInformation.isClientResponsibleForLateSubmission.fold(
        Json.obj()
      )(
        isClientResponsibleForLateSubmission => Json.obj("isClientResponsibleForLateSubmission" -> isClientResponsibleForLateSubmission)
      )
    )
  }
}

case class ObligationAppealInformation(
                                        reasonableExcuse: String,
                                        honestyDeclaration: Boolean,
                                        statement: Option[String],
                                        supportingEvidence: Option[Evidence],
                                        isClientResponsibleForSubmission: Option[Boolean] = None,
                                        isClientResponsibleForLateSubmission: Option[Boolean] = None
                                      ) extends AppealInformation

object ObligationAppealInformation {
  implicit val evidenceFormatter: OFormat[Evidence] = Evidence.format
  implicit val obligationAppealInformationFormatter: OFormat[ObligationAppealInformation] = Json.format[ObligationAppealInformation]

  val obligationAppealInformationWrites: Writes[ObligationAppealInformation] = (obligationAppealInformation: ObligationAppealInformation) => {
    Json.obj(
      "reasonableExcuse" -> obligationAppealInformation.reasonableExcuse,
      "honestyDeclaration" -> obligationAppealInformation.honestyDeclaration,
      "statement" -> obligationAppealInformation.statement.get
    ).deepMerge(
      obligationAppealInformation.supportingEvidence.fold(
        Json.obj()
      )(
        supportingEvidence => Json.obj("supportingEvidence" -> supportingEvidence)
      )

    )
  }
}

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
  def parseAppealInformationToJson(payload: AppealInformation): JsValue = {
    payload.reasonableExcuse match {
      case "bereavement" => Json.toJson(payload.asInstanceOf[BereavementAppealInformation])(BereavementAppealInformation.bereavementAppealWrites)
      case "crime" => Json.toJson(payload.asInstanceOf[CrimeAppealInformation])(CrimeAppealInformation.crimeAppealWrites)
      case "fireOrFlood" => Json.toJson(payload.asInstanceOf[FireOrFloodAppealInformation])(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      case "lossOfStaff" => Json.toJson(payload.asInstanceOf[LossOfStaffAppealInformation])(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
      case "technicalIssues" => Json.toJson(payload.asInstanceOf[TechnicalIssuesAppealInformation])(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
      case "health" => Json.toJson(payload.asInstanceOf[HealthAppealInformation])(HealthAppealInformation.healthAppealWrites)
      case "other" => Json.toJson(payload.asInstanceOf[OtherAppealInformation])(OtherAppealInformation.otherAppealInformationWrites)
      case "obligation" => Json.toJson(payload.asInstanceOf[ObligationAppealInformation])(ObligationAppealInformation.obligationAppealInformationWrites)
    }
  }

  //scalastyle:off
  def constructModelBasedOnReasonableExcuse(reasonableExcuse: String, isLateAppeal: Boolean, amountOfFileUploads: Long, agentDetails: Option[AgentDetails])
                                           (implicit userRequest: UserRequest[_]): AppealSubmission = {
    reasonableExcuse match {
      case "bereavement" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = BereavementAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.whenDidThePersonDie).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }
      case "crime" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = CrimeAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.dateOfCrime).get,
            reportedIssueToPolice = userRequest.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get == "yes",
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }
      case "fireOrFlood" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = FireOrFloodAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.dateOfFireOrFlood).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }

      case "lossOfStaff" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = LossOfStaffAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.whenPersonLeftTheBusiness).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }

      case "technicalIssues" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = TechnicalIssuesAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.whenDidTechnologyIssuesBegin).get,
            endDateOfEvent = userRequest.session.get(SessionKeys.whenDidTechnologyIssuesEnd).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }

      case "health" => {
        val isHospitalStay = userRequest.session.get(SessionKeys.wasHospitalStayRequired).get == "yes"
        val isOngoingHospitalStay = userRequest.session.get(SessionKeys.hasHealthEventEnded).contains("no")
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = HealthAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            hospitalStayInvolved = isHospitalStay,
            startDateOfEvent = if (isHospitalStay) userRequest.session.get(SessionKeys.whenHealthIssueStarted) else userRequest.session.get(SessionKeys.whenHealthIssueHappened),
            endDateOfEvent = if (isOngoingHospitalStay) None else userRequest.session.get(SessionKeys.whenHealthIssueEnded),
            eventOngoing = isOngoingHospitalStay,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }

      case "other" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            startDateOfEvent = userRequest.session.get(SessionKeys.whenDidBecomeUnable).get,
            statement = userRequest.session.get(SessionKeys.whyReturnSubmittedLate),
            supportingEvidence = if (amountOfFileUploads > 0) Some(Evidence(amountOfFileUploads)) else None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason),
            isClientResponsibleForSubmission = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).map(_ == "client"),
            isClientResponsibleForLateSubmission = userRequest.session.get(SessionKeys.causeOfLateSubmissionAgent).map(_ == "client")
          )
        )
      }
      case "obligation" => {
        AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = s"VRN${userRequest.vrn}",
          dateOfAppeal = LocalDateTime.now(),
          isLPP = !userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString),
          appealSubmittedBy = if (userRequest.isAgent) "agent" else "client",
          agentDetails = agentDetails,
          appealInformation = ObligationAppealInformation(
            reasonableExcuse = reasonableExcuse,
            honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
            statement = userRequest.session.get(SessionKeys.otherRelevantInformation),
            supportingEvidence = if (amountOfFileUploads > 0) Some(Evidence(amountOfFileUploads)) else None
          )
        )
      }
    }
  }

  val writes: Writes[AppealSubmission] = (appealSubmission: AppealSubmission) => {
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
        details => Json.obj("agentDetails" -> details)
      )
    )
  }
}