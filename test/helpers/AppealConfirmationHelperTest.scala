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

package helpers

import base.SpecBase

class AppealConfirmationHelperTest extends SpecBase {

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is single penalty" in {
        val result = AppealConfirmationHelper.getPluralOrSingular(Some("no"))("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.singular"
      }
    }
    "show the plural wording" when {
      "there are both penalties" in {
        val result = AppealConfirmationHelper.getPluralOrSingular(Some("yes"))("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.plural"
      }
    }
  }
}
