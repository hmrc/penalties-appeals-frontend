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

package viewtils

import base.SpecBase

import java.time.LocalDate

class ImplicitDateFormatterSpec extends SpecBase with ImplicitDateFormatter {

  "ImplicitDateFormatter" must {
    "return a formatted LocalDate" when {
      "dateToString is called" in {
        dateToString(LocalDate.of(2021, 1, 1)) shouldBe "1 January 2021"
      }
    }

    "return the formatted Welsh LocalDate" when {
      "dateToString is called" in {
        dateToString(LocalDate.of(2021, 1, 1))(cyMessages) shouldBe "1 Ionawr 2021"
      }
    }
  }
}
