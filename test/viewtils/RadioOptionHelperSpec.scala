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
import forms.{HasCrimeBeenReportedForm, WhoPlannedToSubmitVATReturnForm}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class RadioOptionHelperSpec extends SpecBase {
  "radioOptionsForHasCrimeBeenReportedPage" should {
    "return the correct radio options for the HasCrimeBeenReportedPage - no option has been selected" in {
      val form = HasCrimeBeenReportedForm.hasCrimeBeenReportedForm
      val expectedResult = Seq(
        RadioItem(
          value = Some("yes"),
          content = Text("Yes"),
          checked = false
        ),
        RadioItem(
          value = Some("no"),
          content = Text("No"),
          checked = false
        ),
        RadioItem(
          value = Some("unknown"),
          content = Text("I do not know"),
          checked = false
        ),
      )

      val result = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(form)
      result shouldBe expectedResult
    }

    "return the correct radio options for the HasCrimeBeenReportedPage - option has been selected" in {
      val form = HasCrimeBeenReportedForm.hasCrimeBeenReportedForm.fill("yes")
      val expectedResult = Seq(
        RadioItem(
          value = Some("yes"),
          content = Text("Yes"),
          checked = true
        ),
        RadioItem(
          value = Some("no"),
          content = Text("No"),
          checked = false
        ),
        RadioItem(
          value = Some("unknown"),
          content = Text("I do not know"),
          checked = false
        ),
      )

      val result = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(form)
      result shouldBe expectedResult
    }
  }

  "radioOptionsForSubmitVATReturnPage" should {
    "return the correct radio options for the WhoPlannedToSubmitVATReturnPage - no option has been selected" in {
      val form = WhoPlannedToSubmitVATReturnForm.whoPlannedToSubmitVATReturnForm
      val expectedResult = Seq(
        RadioItem(
          value = Some("agent"),
          content = Text("I did"),
          checked = false
        ),
        RadioItem(
          value = Some("client"),
          content = Text("My Client did"),
          checked = false
        )
      )

      val result = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(form)
      result shouldBe expectedResult
    }

    "return the correct radio options for the WhoPlannedToSubmitVATReturnPage - option has been selected" in {
      val form = WhoPlannedToSubmitVATReturnForm.whoPlannedToSubmitVATReturnForm.fill("agent")
      val expectedResult = Seq(
        RadioItem(
          value = Some("agent"),
          content = Text("I did"),
          checked = true
        ),
        RadioItem(
          value = Some("client"),
          content = Text("My Client did"),
          checked = false
        )
      )

      val result = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(form)
      result shouldBe expectedResult
    }
  }
}
