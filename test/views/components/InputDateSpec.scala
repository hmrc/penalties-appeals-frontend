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

package views.components

import base.SpecBase
import forms.mappings.Mappings
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.components.inputDate

import java.time.LocalDate

class InputDateSpec extends SpecBase with Mappings with ViewBehaviours {
  val inputDateComponent: inputDate = injector.instanceOf[inputDate]

  def form(): Form[_] = Form(
    "date" -> localDate(
      invalidKey = "invalidKey",
      allRequiredKey = "allRequiredKey",
      twoRequiredKey = "twoRequiredKey",
      requiredKey = "requiredKey",
      futureKey = Some("futureKey"),
      dateNotEqualOrAfterKeyAndCompareDate = Some(("dateNotEqualOrAfterKey", LocalDate.of(2023, 1, 1)))
    )
  )

  def runTest(values: Map[String, String])(isDayError: Boolean, isMonthError: Boolean, isYearError: Boolean): Unit = {
    val result = asDocument(inputDateComponent.apply(form().bind(values), "Sample date form")(implicitly, userRequestWithCorrectKeys, implicitly))
    result.getElementById("date.day").hasClass("govuk-input--error") shouldBe isDayError
    result.getElementById("date.month").hasClass("govuk-input--error") shouldBe isMonthError
    result.getElementById("date.year").hasClass("govuk-input--error") shouldBe isYearError
  }

  "The input date component" should {
    "highlight the day, month and year fields" when {
      "the date is in the future" in {
        val nextYear = LocalDate.now().plusYears(1).getYear
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "1", "date.year" -> s"$nextYear")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = true)
      }

      "the date is invalid" in {
        val values: Map[String, String] = Map("date.day" -> "x", "date.month" -> "x", "date.year" -> "x")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = true)
      }

      "two of the fields are invalid" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "13", "date.year" -> "222")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = true)
      }

      "the date is before another date" in {
        val values: Map[String, String] = Map("date.day" -> "31", "date.month" -> "12", "date.year" -> "2022")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = true)
      }

      "no values have been provided" in {
        val values: Map[String, String] = Map("date.day" -> "", "date.month" -> "", "date.year" -> "")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = true)
      }
    }

    "highlight the day field only" when {
      "the day has not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "", "date.month" -> "12", "date.year" -> "2022")
        runTest(values)(isDayError = true, isMonthError = false, isYearError = false)
      }

      "the day is invalid (non-digits)" in {
        val values: Map[String, String] = Map("date.day" -> "x", "date.month" -> "12", "date.year" -> "2022")
        runTest(values)(isDayError = true, isMonthError = false, isYearError = false)
      }

      "the day is invalid (exceeds padding for day)" in {
        val values: Map[String, String] = Map("date.day" -> "32", "date.month" -> "12", "date.year" -> "2022")
        runTest(values)(isDayError = true, isMonthError = false, isYearError = false)
      }
    }

    "highlight the month field only" when {
      "the month has not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "", "date.year" -> "2022")
        runTest(values)(isDayError = false, isMonthError = true, isYearError = false)
      }

      "the month is invalid (non-digits)" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "x", "date.year" -> "2022")
        runTest(values)(isDayError = false, isMonthError = true, isYearError = false)
      }

      "the month is invalid (exceeds padding for month)" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "13", "date.year" -> "2022")
        runTest(values)(isDayError = false, isMonthError = true, isYearError = false)
      }
    }

    "highlight the year field only" when {
      "the year has not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "12", "date.year" -> "")
        runTest(values)(isDayError = false, isMonthError = false, isYearError = true)
      }

      "the year is invalid (non-digits)" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "12", "date.year" -> "x")
        runTest(values)(isDayError = false, isMonthError = false, isYearError = true)
      }

      "the year is invalid" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "12", "date.year" -> "222")
        runTest(values)(isDayError = false, isMonthError = false, isYearError = true)
      }
    }

    "highlight two fields" when {
      "the day and month have not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "", "date.month" -> "", "date.year" -> "222")
        runTest(values)(isDayError = true, isMonthError = true, isYearError = false)
      }

      "the month and year have not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "1", "date.month" -> "", "date.year" -> "")
        runTest(values)(isDayError = false, isMonthError = true, isYearError = true)
      }

      "the day and year have not been provided" in {
        val values: Map[String, String] = Map("date.day" -> "", "date.month" -> "1", "date.year" -> "")
        runTest(values)(isDayError = true, isMonthError = false, isYearError = true)
      }
    }
  }
}
