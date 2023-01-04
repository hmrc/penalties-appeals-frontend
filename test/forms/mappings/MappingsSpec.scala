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

package forms.mappings

import base.SpecBase
import play.api.data.{Form, FormError}

class MappingsSpec extends SpecBase with Mappings{

  "text" must {
    val testForm: Form[String] = Form(
      "value" -> text()
    )

    "bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foo"))
      result.get shouldBe "foo"
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors should contain(FormError("value", "error.required"))
    }

    "not bind a string with spaces only" in {
      val result = testForm.bind(Map("value" -> "        "))
      result.errors should contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors should contain(FormError("value", "error.required"))
    }

    "return a custom error message" in {
      val form = Form("value" -> text("reasonableExcuses.error.required"))
      val result = form.bind(Map("value" -> ""))
      result.errors should contain(FormError("value", "reasonableExcuses.error.required"))
    }

    "unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.get shouldBe "foobar"
    }
  }
}