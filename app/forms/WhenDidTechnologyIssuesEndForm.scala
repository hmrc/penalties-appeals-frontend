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
import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate

object WhenDidTechnologyIssuesEndForm extends Mappings {
  def whenDidTechnologyIssuesEndForm(startDate: LocalDate)(implicit messages: Messages): Form[LocalDate] = {
    Form(
      "date" -> localDate(
        invalidKey = "technicalIssues.end.error.invalid",
        allRequiredKey = "technicalIssues.end.error.required.all",
        twoRequiredKey = "technicalIssues.end.error.required.two",
        requiredKey = "technicalIssues.end.error.required",
        futureKey = Some("technicalIssues.end.error.notInFuture"),
        dateNotEqualOrAfterKeyAndCompareDate = Some(("technicalIssues.end.error.endDateLessThanStartDate", startDate))
      )
    )
  }
}
