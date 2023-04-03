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

import config.AppConfig
import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import viewtils.ImplicitDateFormatter

import java.time.LocalDate


object WhenDidHospitalStayEndForm extends Mappings {

  def whenDidHospitalStayEndForm(startDate: LocalDate)(implicit messages: Messages, appConfig: AppConfig): Form[LocalDate] = {
    Form(
      "date" -> localDate(
        invalidKey = "healthReason.whenDidHospitalStayEnd.error.invalid",
        allRequiredKey = "healthReason.whenDidHospitalStayEnd.error.required.all",
        twoRequiredKey = "healthReason.whenDidHospitalStayEnd.error.required.two",
        requiredKey = "healthReason.whenDidHospitalStayEnd.error.required",
        futureKey = Some("healthReason.whenDidHospitalStayEnd.error.notInFuture"),
        //Using the messages API as it's easier to pass in the startDate message param
        dateNotEqualOrAfterKeyAndCompareDate = Some(messages("healthReason.whenDidHospitalStayEnd.error.endDateLessThanStartDate", ImplicitDateFormatter.dateToString(startDate)), startDate)
      )
    )
  }
}
