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

class WhyReturnSubmittedLateFormSpec extends SpecBase {
  val form = WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm()

  "When no reason is given" should {
    "give required error and not bind" in {
      val result = form.bind(Map("why-return-submitted-late-text" -> "")).apply("why-return-submitted-late-text")
      result.errors.headOption shouldBe Some(FormError("why-return-submitted-late-text", "otherReason.whyReturnSubmittedLate.error.required"))
    }
  }

  "A valid reason is given" should {
    "give required error and not bind" in {
      val result = form.bind(Map("why-return-submitted-late-text" -> "Valid Reason.")).apply("why-return-submitted-late-text")
      result.errors shouldBe empty
    }
  }
}
