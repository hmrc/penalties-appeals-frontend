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
import play.api.data.FormError

class WhyReturnWasSubmittedLateAgentFormSpec extends SpecBase {
  val form = WhyReturnWasSubmittedLateAgentForm.whyReturnWasSubmittedLateAgentForm

  "WhyReturnWasSubmittedLateAgentForm" should {
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

    "not bind" when {
      "no option is selected" in {
        val result = form.bind(
          Map(
            "value" -> ""
          )
        )
        result.hasErrors shouldBe true
        result.errors.head shouldBe FormError("value", "agents.whyWasTheReturnSubmittedLate.error.invalid")
      }

      "an invalid option is provided" in {
        val result = form.bind(
          Map(
            "value" -> "person"
          )
        )
        result.hasErrors shouldBe true
        result.errors.head shouldBe FormError("value", "agents.whyWasTheReturnSubmittedLate.error.invalid")
      }
    }
  }
}
