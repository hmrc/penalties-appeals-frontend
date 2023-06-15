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

case class ObligationAppealInformation(
                                        reasonableExcuse: String,
                                        honestyDeclaration: Boolean,
                                        statement: Option[String],
                                        supportingEvidence: Option[Evidence],
                                        isClientResponsibleForSubmission: Option[Boolean] = None,
                                        isClientResponsibleForLateSubmission: Option[Boolean] = None,
                                        uploadedFiles: Option[Seq[UploadJourney]]
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

    ).deepMerge(
      obligationAppealInformation.uploadedFiles.fold(
        Json.obj()
      )(
        uploadedFiles => Json.obj("uploadedFiles" -> uploadedFiles)
      )
    )
  }
}
