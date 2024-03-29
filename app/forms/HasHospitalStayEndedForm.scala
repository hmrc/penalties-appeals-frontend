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

package forms

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.single

object HasHospitalStayEndedForm extends Mappings {
  final val options = Seq("yes", "no")
  val hasHospitalStayEndedForm: Form[String] = Form[String](
    single(
      "hasStayEnded" -> text("healthReason.hasTheHospitalStayEnded.error.required")
        .verifying("healthReason.hasTheHospitalStayEnded.error.required", value => options.contains(value))
    )
  )
}
