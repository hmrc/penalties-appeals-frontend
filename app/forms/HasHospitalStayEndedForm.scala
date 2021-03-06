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
import models.appeals.HospitalStayEndInput
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.voa.play.form.ConditionalMappings

import java.time.LocalDate

object HasHospitalStayEndedForm extends Mappings {
  final val options = Seq("yes", "no")

  def hasHospitalStayEndedForm(healthIssueStartDate: LocalDate)(implicit messages: Messages, appConfig: AppConfig): Form[HospitalStayEndInput] = {
    Form(mapping(
      "hasStayEnded" -> text("healthReason.hasTheHospitalStayEnded.error.required")
        .verifying("healthReason.hasTheHospitalStayEnded.error.required", value => options.contains(value)),
      "stayEndDate" -> ConditionalMappings.mandatoryIfEqual("hasStayEnded", "yes", localDate(
        invalidKey = "healthReason.hasTheHospitalStayEnded.date.error.invalid",
        allRequiredKey = "healthReason.hasTheHospitalStayEnded.date.error.required.all",
        twoRequiredKey = "healthReason.hasTheHospitalStayEnded.date.error.required.two",
        requiredKey = "healthReason.hasTheHospitalStayEnded.date.error.required",
        futureKey = Some("healthReason.hasTheHospitalStayEnded.date.error.notInFuture"),
        dateNotEqualOrAfterKeyAndCompareDate = Some(("healthReason.hasTheHospitalStayEnded.date.error.endDateLessThanStartDate", healthIssueStartDate))
      ))
    )(HospitalStayEndInput.apply)(HospitalStayEndInput.unapply)
    )
  }
}
