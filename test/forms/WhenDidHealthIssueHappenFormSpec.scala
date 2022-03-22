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
import play.api.data.{Form, FormError}

import java.time.LocalDate

class WhenDidHealthIssueHappenFormSpec extends SpecBase {
  
  val formVATTraderLSP: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, vatTraderLSPUserRequest)

  val formVATTraderLPP: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, vatTraderLPPUserRequest)

  val formAgentSubmitClientLate: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserAgentSubmitButClientWasLateSessionKeys)

  val formAgentAgentSubmitLate: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserAgentMissedSessionKeys)

  val formAgentClientSubmit: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserAgentClientPlannedToSubmitSessionKeys)

  val formAgentLPP: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserLPP)

  "WhenDidHealthIssueHappenForm" should {
    "when a VAT trader appealing against an LSP" must {
      "bind when the date is valid" in {
        val result = formVATTraderLSP.bind(
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
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.notInFuture.lsp", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.lsp", Seq("day"))
        }

        "the date has no month" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.lsp", Seq("month"))
        }

        "the date has no year" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "health.whenHealthIssueHappened.error.required.lsp", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formVATTraderLSP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.all.lsp", Seq("day", "month", "year"))
        }
      }
    }

    "when a VAT trader appealing against an LPP" must {
      "bind when the date is valid" in {
        val result = formVATTraderLPP.bind(
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
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.notInFuture.lpp", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lpp", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lpp", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lpp", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.lpp", Seq("day"))
        }

        "the date has no month" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.lpp", Seq("month"))
        }

        "the date has no year" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "health.whenHealthIssueHappened.error.required.lpp", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.two.lpp", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lpp", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lpp", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formVATTraderLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.all.lpp", Seq("day", "month", "year"))
        }
      }
    }

    "when an agent user is appealing a LSP where the agent planned to submit but the client missed the deadline" must {
      "bind when the date is valid" in {
        val result = formAgentSubmitClientLate.bind(
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
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.notInFuture.clientMissedDeadline", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientMissedDeadline", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientMissedDeadline", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientMissedDeadline", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.clientMissedDeadline", Seq("day"))
        }

        "the date has no month" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.clientMissedDeadline", Seq("month"))
        }

        "the date has no year" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "agent.health.whenHealthIssueHappened.error.required.clientMissedDeadline", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.two.clientMissedDeadline", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.clientMissedDeadline", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.clientMissedDeadline", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formAgentSubmitClientLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.all.clientMissedDeadline", Seq("day", "month", "year"))
        }
      }
    }

    "when an agent user is appealing a LSP where the agent planned to submit but missed the deadline" must {
      "bind when the date is valid" in {
        val result = formAgentAgentSubmitLate.bind(
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
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.notInFuture.lsp", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.invalid.lsp", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.lsp", Seq("day"))
        }

        "the date has no month" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.lsp", Seq("month"))
        }

        "the date has no year" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "health.whenHealthIssueHappened.error.required.lsp", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.two.lsp", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formAgentAgentSubmitLate.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "health.whenHealthIssueHappened.error.required.all.lsp", Seq("day", "month", "year"))
        }
      }
    }

    "when an agent user is appealing a LSP where the client planned to submit and missed the deadline" must {
      "bind when the date is valid" in {
        val result = formAgentClientSubmit.bind(
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
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.notInFuture.clientIntendedToSubmit", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientIntendedToSubmit", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientIntendedToSubmit", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.clientIntendedToSubmit", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.clientIntendedToSubmit", Seq("day"))
        }

        "the date has no month" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.clientIntendedToSubmit", Seq("month"))
        }

        "the date has no year" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "agent.health.whenHealthIssueHappened.error.required.clientIntendedToSubmit", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.two.clientIntendedToSubmit", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.clientIntendedToSubmit", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.clientIntendedToSubmit", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formAgentClientSubmit.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.all.clientIntendedToSubmit", Seq("day", "month", "year"))
        }
      }
    }

    "when an agent user is appealing a LPP" must {
      "bind when the date is valid" in {
        val result = formAgentLPP.bind(
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
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> "2050"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.notInFuture.lpp", Seq("day", "month", "year"))
        }

        "the date is not valid" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.lpp", Seq())
        }

        "the date contains strings instead of numbers" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "thirtyFirst",
              "date.month" -> "ofTheSecond",
              "date.year" -> "twentyTwentyOne"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.lpp", Seq("day", "month", "year"))
        }

        "the date contains negative numbers" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "-1",
              "date.month" -> "-2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.invalid.lpp", Seq("day", "month", "year"))
        }

        "the date has no day" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.lpp", Seq("day"))
        }

        "the date has no month" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.lpp", Seq("month"))
        }

        "the date has no year" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "1",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.year", "agent.health.whenHealthIssueHappened.error.required.lpp", Seq("year"))
        }

        "the date has a day but no month and year" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "2",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.month", "agent.health.whenHealthIssueHappened.error.required.two.lpp", Seq("month", "year"))
        }

        "the date has a month but no day and year" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "2",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.lpp", Seq("day", "year"))
        }

        "the date has a year but no day and month" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> "2021"
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.two.lpp", Seq("day", "month"))
        }

        "the date has no values" in {
          val result = formAgentLPP.bind(
            Map(
              "date.day" -> "",
              "date.month" -> "",
              "date.year" -> ""
            )
          )
          result.errors.size shouldBe 1
          result.errors.head shouldBe FormError("date.day", "agent.health.whenHealthIssueHappened.error.required.all.lpp", Seq("day", "month", "year"))
        }
      }
    }
  }

}
