/*
 * Copyright 2022 HM Revenue & Customs
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
import config.AppConfig
import models.appeals.HospitalStayEndInput
import org.mockito.Mockito.mock
import play.api.Configuration
import play.api.data.{Form, FormError}

import java.time.LocalDate

class HasHospitalStayEndedFormSpec extends FormBehaviours with SpecBase {
  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  override implicit val config: Configuration = mock(classOf[Configuration])
  val sampleStartDate: LocalDate = LocalDate.parse("2020-01-02")
  val form: Form[HospitalStayEndInput] = HasHospitalStayEndedForm.hasHospitalStayEndedForm(sampleStartDate)

  "radio field option not present" should {
    "not apply any validation on the date - fail" in {
      val result = form.bind(Map(
        "hasStayEnded" -> ""
      ))
      result.errors.size shouldBe 1
      result.errors.head shouldBe FormError("hasStayEnded", "healthReason.hasTheHospitalStayEnded.error.required", Seq())
    }

    "invalid answer given - fail" in {
      val result = form.bind(Map(
        "hasStayEnded" -> "what-is-this"
      ))
      result.errors.size shouldBe 1
      result.errors.head shouldBe FormError("hasStayEnded", "healthReason.hasTheHospitalStayEnded.error.required", Seq())
    }
  }

  "the 'no' option is selected" should {
    "not apply any validation on the date - success" in {
      val result = form.bind(Map(
        "hasStayEnded" -> "no",
        "stayEndDate.day" -> "",
        "stayEndDate.month" -> "",
        "stayEndDate.year" -> ""
      ))
      result.errors.isEmpty shouldBe true
    }
  }

  "the 'yes' option is selected" should {
    "not bind" when {
      "the date is in the future" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "1",
            "stayEndDate.month" -> "2",
            "stayEndDate.year" -> "2050"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.notInFuture", Seq("day", "month", "year"))
      }

      "the date entered is before the start date" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "1",
            "stayEndDate.month" -> "1",
            "stayEndDate.year" -> "2020"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError(
          "stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.endDateLessThanStartDate", Seq("day", "month", "year"))
      }

      "the date is not valid" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "31",
            "stayEndDate.month" -> "2",
            "stayEndDate.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.invalid", Seq())
      }

      "the date contains strings instead of numbers" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "thirtyFirst",
            "stayEndDate.month" -> "ofTheSecond",
            "stayEndDate.year" -> "twentyTwentyOne"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.invalid", Seq("day", "month", "year"))
      }

      "the date contains negative numbers" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "-1",
            "stayEndDate.month" -> "-2",
            "stayEndDate.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.invalid", Seq("day", "month", "year"))
      }

      "the date has no day" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "",
            "stayEndDate.month" -> "2",
            "stayEndDate.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.required", Seq("day"))
      }

      "the date has no month" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "1",
            "stayEndDate.month" -> "",
            "stayEndDate.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.month", "healthReason.hasTheHospitalStayEnded.date.error.required", Seq("month"))
      }

      "the date has no year" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "1",
            "stayEndDate.month" -> "2",
            "stayEndDate.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.year", "healthReason.hasTheHospitalStayEnded.date.error.required", Seq("year"))
      }

      "the date has a day but no month and year" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "2",
            "stayEndDate.month" -> "",
            "stayEndDate.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.month", "healthReason.hasTheHospitalStayEnded.date.error.required.two", Seq("month", "year"))
      }

      "the date has a month but no day and year" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "",
            "stayEndDate.month" -> "2",
            "stayEndDate.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.required.two", Seq("day", "year"))
      }

      "the date has a year but no day and month" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "",
            "stayEndDate.month" -> "",
            "stayEndDate.year" -> "2021"
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.required.two", Seq("day", "month"))
      }

      "the date has no values" in {
        val result = form.bind(
          Map(
            "hasStayEnded" -> "yes",
            "stayEndDate.day" -> "",
            "stayEndDate.month" -> "",
            "stayEndDate.year" -> ""
          )
        )
        result.errors.size shouldBe 1
        result.errors.head shouldBe FormError("stayEndDate.day", "healthReason.hasTheHospitalStayEnded.date.error.required.all", Seq("day", "month", "year"))
      }
    }
  }
}
