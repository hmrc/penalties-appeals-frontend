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

package utils

import base.SpecBase
import models.UserRequest
import models.session.UserAnswers
import play.api.i18n.DefaultMessagesApi
import play.api.libs.json.Json
import play.api.test.FakeRequest

class MessageRendererSpec extends SpecBase {

  val testMessages = Map(
    "default" -> Map(
      "test.message" -> "I am a {0} with no ARN",
      "agent.test.message" -> "I’m an {0} with an {1}"
    )
  )

  "getMessage" should {
    "load the relevant agent message" when {
      "the user is an agent" in {
        val answer = UserAnswers("123456789", Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"))
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest.
          withSession())
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, fakeAgentRequest)
        result shouldBe "agent.message.key"
      }

      "apply the given arguments" in {
        val answer = UserAnswers("123456789", Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"))
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
        val messagesApi = new DefaultMessagesApi(testMessages)
        val messages = messagesApi.preferred(FakeRequest("GET", "/"))
        val fakeMessageKey = "test.message"
        val result = MessageRenderer.getMessage(fakeMessageKey, "agent", "ARN")(messages, fakeAgentRequest)
        result shouldBe "I’m an agent with an ARN"
      }
    }

    "load the normal message" when {
      "the user is a VAT trader" in {
        val answer = UserAnswers("123456789")
        val fakeVATTraderRequest = UserRequest(vrn = "123456789", answers = answer)(fakeRequest)
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, fakeVATTraderRequest)
        result shouldBe "message.key"
      }

      "apply the given arguments" in {
        val answer = UserAnswers("123456789")
        val fakeVATTraderRequest = UserRequest(vrn = "123456789", answers = answer)(fakeRequest)
        val messagesApi = new DefaultMessagesApi(testMessages)
        val messages = messagesApi.preferred(FakeRequest("GET", "/"))
        val fakeMessageKey = "test.message"
        val result = MessageRenderer.getMessage(fakeMessageKey, "vat trader")(messages, fakeVATTraderRequest)
        result shouldBe "I am a vat trader with no ARN"
      }
    }
  }

  "getMessageKey" should {
    "load the relevant message key" when {
      "the user is an agent" in {
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, agentUserAgentSubmitButClientWasLateSessionKeys)
        result shouldBe "agent.message.key"
      }

      "the user is a VAT trader" in {
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, vatTraderLSPUserRequest)
        result shouldBe "message.key"
      }
    }
  }

  "didClientCauseLateSubmission" should {
    "return true" when {
      "the client planned to submit" in {
        val answer = UserAnswers("123456789", Json.obj(SessionKeys.whoPlannedToSubmitVATReturn -> "client"))
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers =  answer)(fakeRequest)
        val result = MessageRenderer.didClientCauseLateSubmission()(fakeAgentRequest)
        result shouldBe true
      }

      "the agent planned to submit but the client missed the deadline" in {
        val answer = UserAnswers("123456789", Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"))
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
        val result = MessageRenderer.didClientCauseLateSubmission()(fakeAgentRequest)
        result shouldBe true
      }
    }

    "return false" when {
      "when the agent planned to submit and missed the deadline" in {
        val answer = UserAnswers("123456789", Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "agent"))
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)

        val result = MessageRenderer.didClientCauseLateSubmission()(fakeAgentRequest)
        result shouldBe false
      }
    }
  }

  "didClientPlanToSubmit" should {
    "return true when client planned to submit" in {
      val answer = UserAnswers("123456789", Json.obj(
        SessionKeys.whoPlannedToSubmitVATReturn -> "client"))
      val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
      val result = MessageRenderer.didClientPlanToSubmit()(fakeAgentRequest)
      result shouldBe true
    }

    "return false when agent planned to submit" in {
      val answer = UserAnswers("123456789", Json.obj(
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent"))
      val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
      val result = MessageRenderer.didClientPlanToSubmit()(fakeAgentRequest)
      result shouldBe false
    }
  }

  "didAgentPlanToSubmitAndClientMissedDeadline" should {
    "return true when agent planned to submit and client missed deadline" in {
      val answer = UserAnswers("123456789", Json.obj(
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.whatCausedYouToMissTheDeadline -> "client"))
      val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
      val result = MessageRenderer.didAgentPlanToSubmitAndClientMissedDeadline()(fakeAgentRequest)
      result shouldBe true
    }

    "return false for any other scenario" in {
      val answer = UserAnswers("123456789", Json.obj(
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.whatCausedYouToMissTheDeadline -> "agent"))
      val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answer)(fakeRequest)
      val result = MessageRenderer.didAgentPlanToSubmitAndClientMissedDeadline()(fakeAgentRequest)
      result shouldBe false
    }
  }

  "getPageTitle" should {
    "return the correct title when the user is an agent" when {

      "the user is an agent" in {
        val answers = UserAnswers("123456789")
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = answers)(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))
        val result = MessageRenderer.getPageTitle("Page name", "Service name")(implicitly, fakeAgentRequest)
        result shouldBe "Page name - Agent - Service name - GOV.UK"
      }

      "the user is a trader" in {
        val answers = UserAnswers("123456789")
        val request = UserRequest(vrn = "123456789", arn = None, answers = answers)(fakeRequest)
        val result = MessageRenderer.getPageTitle("Page name", "Service name")(implicitly, request)
        result shouldBe "Page name - Service name - GOV.UK"
      }
    }
  }
}
