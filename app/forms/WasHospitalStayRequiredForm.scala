/*
 * Copyright 2022 HM Revenue & Customs
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
import models.UserRequest
import play.api.data.Form
import play.api.data.Forms.single
import utils.MessageRenderer.getMessageKey

object WasHospitalStayRequiredForm extends Mappings {
  final val options = Seq("yes", "no")

  def wasHospitalStayRequiredForm()(implicit user: UserRequest[_]): Form[String] = Form(
    single(
      "value" -> text(getMessageKey("healthReason.wasHospitalStayRequired.error.required"))
        .verifying(getMessageKey("healthReason.wasHospitalStayRequired.error.required"), value => options.contains(value))
    )
  )

}
