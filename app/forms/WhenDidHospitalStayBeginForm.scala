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

import config.AppConfig
import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate


object WhenDidHospitalStayBeginForm extends Mappings {

  def whenHospitalStayBeginForm()(implicit messages: Messages, appConfig: AppConfig): Form[LocalDate] = {
    Form(
      "date" -> localDate(
        invalidKey = "healthReason.whenDidHospitalStayBegin.error.invalid",
        allRequiredKey = "healthReason.whenDidHospitalStayBegin.error.required.all",
        twoRequiredKey = "healthReason.whenDidHospitalStayBegin.error.required.two",
        requiredKey = "healthReason.whenDidHospitalStayBegin.error.required",
        futureKey = Some("healthReason.whenDidHospitalStayBegin.error.notInFuture")
      )
    )
  }
}
