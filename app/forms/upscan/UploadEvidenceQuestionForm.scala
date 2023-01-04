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

package forms.upscan

import forms.mappings.Mappings
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, optional}
import models.upload.UploadEvidenceFormModel

object UploadEvidenceQuestionForm extends Mappings {
  final val options = Seq("yes", "no")
  val uploadEvidenceQuestionForm: Form[UploadEvidenceFormModel] = Form[UploadEvidenceFormModel](
    mapping(
      "value" -> text("otherReason.uploadEvidence.question.required")
        .verifying("otherReason.uploadEvidence.question.required", value => options.contains(value)),
      "isJsEnabled" -> optional(Forms.boolean)
    )(UploadEvidenceFormModel.apply)(UploadEvidenceFormModel.unapply)
  )
}