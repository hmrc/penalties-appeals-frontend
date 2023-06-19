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

package models.appeals.submission

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.LocalDateTime

case class BereavementAppealInformation(
                                         reasonableExcuse: String,
                                         honestyDeclaration: Boolean,
                                         startDateOfEvent: LocalDateTime,
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
