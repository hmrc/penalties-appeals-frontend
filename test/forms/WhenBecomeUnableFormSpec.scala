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
import play.api.data.Form

import java.time.LocalDate

class WhenBecomeUnableFormSpec extends SpecBase with FormBehaviours {

  val formVATTraderLSP: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, vatTraderLSPUserRequest)

  val formVATTraderLPP: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, vatTraderLPPUserRequest)

  val formAgentAgentSubmitClientLate: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, agentUserAgentSubmitButClientWasLateSessionKeys)

  val formAgentAgentSubmitLate: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, agentUserAgentMissedSessionKeys)

  val formAgentAgentClientSubmit: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, agentUserAgentClientPlannedToSubmitSessionKeys)

  val formAgentLPP: Form[LocalDate] = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, appConfig, agentUserLPP)

  "whenBecomeUnableForm" should {
    "when a VAT trader appealing against an LSP" must {
      behave like dateForm(formVATTraderLSP, "date", errorType => s"whenDidBecomeUnable.error.$errorType.lsp")
    }

    "when a VAT trader appealing against an LPP" must {
      behave like dateForm(formVATTraderLPP, "date", errorType => s"whenDidBecomeUnable.error.$errorType.lpp")
    }

    "when an agent user is appealing a LSP where the agent planned to submit but the client missed the deadline" must {
      behave like dateForm(formAgentAgentSubmitClientLate, "date", errorType => s"agent.whenDidBecomeUnable.error.$errorType.clientMissedDeadline")
    }

    "when an agent user is appealing a LSP where the agent planned to submit but missed the deadline" must {
      behave like dateForm(formAgentAgentSubmitLate, "date", errorType => s"whenDidBecomeUnable.error.$errorType.lsp")
    }

    "when an agent user is appealing a LSP where the client planned to submit and missed the deadline" must {
      behave like dateForm(formAgentAgentClientSubmit, "date", errorType => s"agent.whenDidBecomeUnable.error.$errorType.clientIntendedToSubmit")
    }

    "when an agent user is appealing a LPP" must {
      behave like dateForm(formAgentLPP, "date", errorType => s"agent.whenDidBecomeUnable.error.$errorType.lpp")
    }
  }
}
