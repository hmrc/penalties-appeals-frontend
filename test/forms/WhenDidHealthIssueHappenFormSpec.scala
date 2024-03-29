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

import base.SpecBase
import models.UserRequest
import play.api.data.Form

import java.time.LocalDate

class WhenDidHealthIssueHappenFormSpec extends SpecBase with FormBehaviours {

  def form(user: UserRequest[_]): Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, appConfig, user)

  "WhenDidHealthIssueHappenForm" should {

    "when a VAT trader appealing against an LSP" must {
      behave like dateForm(form(vatTraderLSPUserRequest), "date", errorType => s"health.whenHealthIssueHappened.error.$errorType.lsp")
    }

    "when a VAT trader appealing against an LPP" must {
      behave like dateForm(form(vatTraderLPPUserRequest), "date", errorType => s"health.whenHealthIssueHappened.error.$errorType.lpp")
    }

    "when an agent user is appealing a LSP where the agent planned to submit but the client missed the deadline" must {
      behave like dateForm(form(agentUserAgentSubmitButClientWasLateSessionKeys),
        "date", errorType => s"agent.health.whenHealthIssueHappened.error.$errorType.clientMissedDeadline")
    }

    "when an agent user is appealing a LSP where the agent planned to submit but missed the deadline" must {
      behave like dateForm(form(agentUserAgentMissedSessionKeys), "date", errorType => s"health.whenHealthIssueHappened.error.$errorType.lsp")
    }

    "when an agent user is appealing a LSP where the client planned to submit and missed the deadline" must {
      behave like dateForm(form(agentUserAgentClientPlannedToSubmitSessionKeys),
        "date", errorType => s"agent.health.whenHealthIssueHappened.error.$errorType.clientIntendedToSubmit")
    }

    "when an agent user is appealing a LPP" must {
      behave like dateForm(form(agentUserLPP), "date", errorType => s"agent.health.whenHealthIssueHappened.error.$errorType.lpp")
    }
  }
}
