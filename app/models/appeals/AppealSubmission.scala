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

import models.UserRequest
import play.api.libs.json._
import utils.SessionKeys

sealed trait AppealInformation {
  val `type`: String
  val statement: Option[String]
  val lateAppeal: Boolean
}

case class CrimeAppealInformation(
                                   `type`: String,
                                   dateOfEvent: String,
                                   reportedIssue: Boolean,
                                   statement: Option[String],
                                   lateAppeal: Boolean,
                                   lateAppealReason: Option[String]
                                 ) extends AppealInformation

object CrimeAppealInformation {
  implicit val crimeAppealInformationFormatter: OFormat[CrimeAppealInformation] = Json.format[CrimeAppealInformation]

  val crimeAppealWrites: Writes[CrimeAppealInformation] = (crimeAppealInformation: CrimeAppealInformation) => {
    Json.obj(
      "type" -> crimeAppealInformation.`type`,
      "dateOfEvent" -> crimeAppealInformation.dateOfEvent,
      "reportedIssue" -> crimeAppealInformation.reportedIssue,
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
    )
  }
}

case class FireOrFloodAppealInformation(
                                         `type`: String,
                                         dateOfEvent: String,
                                         statement: Option[String],
                                         lateAppeal: Boolean,
                                         lateAppealReason: Option[String]
                                       ) extends AppealInformation

object FireOrFloodAppealInformation {
  implicit val fireOrFloodAppealInformationFormatter: OFormat[FireOrFloodAppealInformation] = Json.format[FireOrFloodAppealInformation]

  val fireOrFloodAppealWrites: Writes[FireOrFloodAppealInformation] = (fireOrFloodAppealInformation: FireOrFloodAppealInformation) => {
    Json.obj(
      "type" -> fireOrFloodAppealInformation.`type`,
      "dateOfEvent" -> fireOrFloodAppealInformation.dateOfEvent,
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
    )
  }
}

case class AppealSubmission(
                             submittedBy: String,
                             penaltyId: String,
                             reasonableExcuse: String,
                             honestyDeclaration: Boolean,
                             appealInformation: AppealInformation
                           )

object AppealSubmission {
  def parseAppealInformationToJson(payload: AppealInformation): JsValue = {
    payload.`type` match {
      case "crime" => {
        Json.toJson(payload.asInstanceOf[CrimeAppealInformation])(CrimeAppealInformation.crimeAppealWrites)
      }
      case "fireOrFlood" => {
        Json.toJson(payload.asInstanceOf[FireOrFloodAppealInformation])(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      }
    }
  }

  def constructModelBasedOnReasonableExcuse(reasonableExcuse: String, isLateAppeal: Boolean)
                                           (implicit userRequest: UserRequest[_]): AppealSubmission = {
    reasonableExcuse match {
      case "crime" => {
        AppealSubmission(
          submittedBy = if (userRequest.isAgent) "agent" else "client",
          penaltyId = userRequest.session.get(SessionKeys.penaltyId).get,
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
          appealInformation = CrimeAppealInformation(
            `type` = "crime",
            dateOfEvent = userRequest.session.get(SessionKeys.dateOfCrime).get,
            reportedIssue = userRequest.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get == "yes",
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason).getOrElse("") match {
              case "" => None
              case reason => Some(reason)
            }
          )
        )
      }
      case "fireOrFlood" => {
        AppealSubmission(
          submittedBy = if (userRequest.isAgent) "agent" else "client",
          penaltyId = userRequest.session.get(SessionKeys.penaltyId).get,
          reasonableExcuse = reasonableExcuse,
          honestyDeclaration = userRequest.session.get(SessionKeys.hasConfirmedDeclaration).get == "true",
          appealInformation = FireOrFloodAppealInformation(
            `type` = "fireOrFlood",
            dateOfEvent = userRequest.session.get(SessionKeys.dateOfFireOrFlood).get,
            statement = None,
            lateAppeal = isLateAppeal,
            lateAppealReason = userRequest.session.get(SessionKeys.lateAppealReason).getOrElse("") match {
              case "" => None
              case reason => Some(reason)
            }
          )
        )
      }
    }
  }

  val writes: Writes[AppealSubmission] = (appealSubmission: AppealSubmission) => {
    Json.obj(
      "submittedBy" -> appealSubmission.submittedBy,
      "penaltyId" -> appealSubmission.penaltyId,
      "reasonableExcuse" -> appealSubmission.reasonableExcuse,
      "honestyDeclaration" -> appealSubmission.honestyDeclaration,
      "appealInformation" -> parseAppealInformationToJson(appealSubmission.appealInformation)
    )
  }
}