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

package forms

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.{Form, FormError}

import java.time.LocalDate

trait FormBehaviours extends AnyWordSpec with Matchers with FeatureSwitching {
  implicit val appConfig: AppConfig
  val emptyForm: Map[String, String] = Map.empty

  def mandatoryField(form: Form[_],
                     fieldName: String,
                     requiredError: FormError): Unit = {

    s"not bind when key is not present at all for field $fieldName" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors.headOption shouldBe Some(requiredError)
    }

    s"not bind blank values for field $fieldName" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors.headOption shouldBe Some(requiredError)
    }
  }

  //scalastyle:off
  def dateForm(form: Form[_],
               fieldName: String,
               errorMessage: String => String): Unit = {
    "bind when the date is valid" in {
      setFeatureDate(None)
      val result = form.bind(
        Map(
          s"$fieldName.day" -> "1",
          s"$fieldName.month" -> "2",
          s"$fieldName.year" -> "2021"
        )
      )
      result.errors shouldBe List.empty
    }

    "not bind" when {
      "the date is in the future" in {
        setFeatureDate(None)
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2050"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("notInFuture"), Seq("day", "month", "year"))
      }

      "the date is in the future relative to the time machine" in {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "2",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2022"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("notInFuture"), Seq("day", "month", "year"))
      }

      "the date is not valid" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "31",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("invalid"), Seq())
      }

      "the date contains strings instead of numbers" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "thirtyFirst",
            s"$fieldName.month" -> "ofTheSecond",
            s"$fieldName.year" -> "twentyTwentyOne"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("invalid"), Seq("day", "month", "year"))
      }

      "the date contains negative numbers" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "-1",
            s"$fieldName.month" -> "-2",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("invalid"), Seq("day", "month", "year"))
      }

      "the date has no day" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("required"), Seq("day"))
      }

      "the date has no month" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.month", errorMessage("required"), Seq("month"))
      }

      "the date has no year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "1",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.year", errorMessage("required"), Seq("year"))
      }

      "the date has a day but no month and year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "2",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.month", errorMessage("required.two"), Seq("month", "year"))
      }

      "the date has a month but no day and year" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "2",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("required.two"), Seq("day", "year"))
      }

      "the date has a year but no day and month" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("required.two"), Seq("day", "month"))
      }

      "the date has no values" in {
        val result = form.bind(
          Map(
            s"$fieldName.day" -> "",
            s"$fieldName.month" -> "",
            s"$fieldName.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(s"$fieldName.day", errorMessage("required.all"), Seq("day", "month", "year"))
      }
    }
  }
}
