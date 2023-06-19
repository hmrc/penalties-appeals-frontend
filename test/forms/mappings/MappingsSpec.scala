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

import java.time.LocalDate

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

  "localDate" must {
    val testForm: Form[LocalDate] = Form(
      "date" -> localDate(
        invalidKey = "invalid",
        allRequiredKey = "allRequired",
        twoRequiredKey = "twoRequired",
        requiredKey = "required")
    )

    "bind a valid date" in {
      val result = testForm.bind(Map(
        "date.day" -> "3",
        "date.month" -> "4",
        "date.year" -> "2021"))
      result.value.get shouldBe LocalDate.of( 2021, 4, 3)
    }

    "format string with spaces into a valid localDate" in {
      val result = testForm.bind(Map(
        "date.day" -> "1 ",
        "date.month" -> " 2",
        "date.year" -> "2 0 2 0"))
      result.value.get shouldBe LocalDate.of( 2020, 2, 1)
    }

    "format string with punctuation into a valid localDate" in {
      val result = testForm.bind(Map(
        "date.day" -> " , 1 . 1",
        "date.month" -> " (1) 2",
        "date.year" -> "2  0 -2- 0"))
      result.value.get shouldBe LocalDate.of(2020, 12, 11)
    }

    "not bind a date that is missing one of it's values and return the correct invalid value" in {
      val result = testForm.bind(Map(
        "date.day" -> "",
        "date.month" -> "2",
        "date.year" -> "2020"))
      result.errors should contain(FormError("date.day", "required", List("day")))
    }

    "not bind a date that is missing two of it's values and return the correct invalid values" in {
      val result = testForm.bind(Map(
        "date.day" -> "2",
        "date.month" -> "",
        "date.year" -> ""))
      result.errors should contain(FormError("date.month", "twoRequired", List("month", "year")))
    }

    "not bind a date that is missing all of it's values and return the correct invalid values" in {
      val result = testForm.bind(Map(
        "date.day" -> "",
        "date.month" -> "",
        "date.year" -> ""))
      result.errors should contain(FormError("date.day", "allRequired", List("day", "month", "year")))
    }

    "treat values with only spaces as if the string was empty" in {
      val result = testForm.bind(Map(
        "date.day" -> "1",
        "date.month" -> " ",
        "date.year" -> "    "))
      result.errors should contain(FormError("date.month", "twoRequired", List("month", "year")))
    }

    "not bind a string that is not a real date" in {
      val result = testForm.bind(Map(
        "date.day" -> "x",
        "date.month" -> "y",
        "date.year" -> "z"))
      result.errors should contain(FormError("date.day", "invalid", List("day", "month", "year")))
    }
  }
}
