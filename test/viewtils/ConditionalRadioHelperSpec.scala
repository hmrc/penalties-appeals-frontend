/*
 * Copyright 2021 HM Revenue & Customs
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

package viewtils

import base.SpecBase
import forms.HasHospitalStayEndedForm
import models.appeals.HospitalStayEndInput
import play.api.data.Form

import java.time.LocalDate

class ConditionalRadioHelperSpec extends SpecBase {
  val sampleStartDate = LocalDate.parse("2020-01-01")
  val radioHelper: ConditionalRadioHelper = injector.instanceOf[ConditionalRadioHelper]
  "conditionalYesNoOptions" should {
    "select the radio option if the form has a prefilled value - for yes" in {
      val form: Form[HospitalStayEndInput] = HasHospitalStayEndedForm.hasHospitalStayEndedForm(sampleStartDate).fill(HospitalStayEndInput("yes", Some(LocalDate.parse("2022-01-01"))))
      val result = radioHelper.conditionalYesNoOptions(form, "healthReason.hasTheHospitalStayEnded")
      result.head.checked shouldBe true
    }

    "select the radio option if the form has a prefilled value - for no" in {
      val form: Form[HospitalStayEndInput] = HasHospitalStayEndedForm.hasHospitalStayEndedForm(sampleStartDate).fill(HospitalStayEndInput("no", None))
      val result = radioHelper.conditionalYesNoOptions(form, "healthReason.hasTheHospitalStayEnded")
      result.last.checked shouldBe true
    }
  }
}
