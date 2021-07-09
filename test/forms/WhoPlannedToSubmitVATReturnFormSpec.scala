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

package forms

import base.SpecBase
import play.api.data.{Form, FormError}

class WhoPlannedToSubmitVATReturnFormSpec extends FormBehaviours with SpecBase{

  val form: Form[String] = WhoPlannedToSubmitVATReturnForm.whoPlannedToSubmitVATReturnForm
  behave like mandatoryField(form, "value", FormError("value", "agents.whoPlannedToSubmitVATReturn.error.required"))
  "the value entered does not exist in the possible, valid values" in {
    val result = form.bind(Map("value" -> "random-value")).apply("value")
    result.errors.headOption shouldBe Some(FormError("value", "agents.whoPlannedToSubmitVATReturn.error.required"))
  }

  "WhoPlannedToSubmitVATReturnForm" should {
    "bind" when {
      "a valid option has been selected" in {
        val result = form.bind(
          Map(
            "value" -> "agent"
          )
        )
        result.hasErrors shouldBe false
      }
    }
  }
}
