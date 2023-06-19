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

import models.appeals.Evidence
import models.upload.UploadJourney
import play.api.libs.json.{Json, OFormat, Writes}

import java.time.LocalDateTime

case class OtherAppealInformation(
                                   reasonableExcuse: String,
                                   honestyDeclaration: Boolean,
                                   startDateOfEvent: LocalDateTime,
                                   statement: Option[String],
                                   supportingEvidence: Option[Evidence],
                                   lateAppeal: Boolean,
                                   lateAppealReason: Option[String],
                                   isClientResponsibleForSubmission: Option[Boolean],
                                   isClientResponsibleForLateSubmission: Option[Boolean],
                                   uploadedFiles: Option[Seq[UploadJourney]]
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
    ).deepMerge(
      otherAppealInformation.uploadedFiles.fold(
        Json.obj()
      )(
        uploadedFiles => Json.obj("uploadedFiles" -> uploadedFiles)
      )
    )
  }
}
