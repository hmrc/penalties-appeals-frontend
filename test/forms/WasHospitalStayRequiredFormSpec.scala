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

import models.UserRequest
import play.api.data.FormError
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.SessionKeys

class WasHospitalStayRequiredFormSpec extends FormBehaviours {

  "when a VAT trader is in session" must {
    val vatTraderUserRequest = UserRequest("123456789")(FakeRequest("GET", "/"))
    val vatTraderForm = WasHospitalStayRequiredForm.wasHospitalStayRequiredForm()(vatTraderUserRequest)
    behave like mandatoryField(vatTraderForm, "value", FormError("value", "healthReason.wasHospitalStayRequired.error.required"))

    "the value entered does not exist in the possible, valid values" in {
      val result = vatTraderForm.bind(Map("value" -> "random-value")).apply("value")
      result.errors.headOption shouldBe Some(FormError("value", "healthReason.wasHospitalStayRequired.error.required"))
    }
  }

  "when an agent is in session" must {
    val agentUserRequest: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(FakeRequest("GET", "/")
      .withSession(
        SessionKeys.agentSessionVrn -> "VRN1234",
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.causeOfLateSubmissionAgent -> "client")
    )

    val agentForm = WasHospitalStayRequiredForm.wasHospitalStayRequiredForm()(agentUserRequest)
    behave like mandatoryField(agentForm, "value", FormError("value", "agent.healthReason.wasHospitalStayRequired.error.required"))

    "the value entered does not exist in the possible, valid values" in {
      val result = agentForm.bind(Map("value" -> "random-value")).apply("value")
      result.errors.headOption shouldBe Some(FormError("value", "agent.healthReason.wasHospitalStayRequired.error.required"))
    }
  }


}
