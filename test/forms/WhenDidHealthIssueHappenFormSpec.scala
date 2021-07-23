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

class WhenDidHealthIssueHappenFormSpec extends SpecBase {
  val formVATTrader = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, vatTraderUser)

  val formAgent = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserSessionKeys)

  "WhenDidHealthIssueHappenForm" should {

    "when a VAT trader" must {
      "bind when the date is valid" in {
        val result = formVATTrader.bind(
          Map(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )
        )
        result.errors shouldBe List.empty
      }

      "not bind" when {
        "the date is in the future" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.notInFuture", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.invalid", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.invalid", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.invalid", Seq())
        }

        "the date has no day" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.required", Seq("day"))
        }

        "the date has no month" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "healthReason.whenHealthIssueHappened.error.required", Seq("month"))
        }

        "the date has no year" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "healthReason.whenHealthIssueHappened.error.required", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "healthReason.whenHealthIssueHappened.error.required.two", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.required.two", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.required.two", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formVATTrader.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "healthReason.whenHealthIssueHappened.error.required.all", Seq("day", "month", "year"))
        }
      }
    }

    "when an agent user" must {
      "bind when the date is valid" in {
        val result = formAgent.bind(
          Map(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )
        )
        result.errors shouldBe List.empty
      }

      "not bind" when {
        "the date is in the future" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.notInFuture", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.invalid", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.invalid", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.invalid", Seq())
        }

        "the date has no day" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.required", Seq("day"))
        }

        "the date has no month" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.healthReason.whenHealthIssueHappened.error.required", Seq("month"))
        }

        "the date has no year" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "agent.healthReason.whenHealthIssueHappened.error.required", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.healthReason.whenHealthIssueHappened.error.required.two", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.required.two", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.required.two", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formAgent.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.healthReason.whenHealthIssueHappened.error.required.all", Seq("day", "month", "year"))
        }
      }
    }
  }

}
