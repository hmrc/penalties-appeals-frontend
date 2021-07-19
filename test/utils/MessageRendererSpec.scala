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

package utils

import base.SpecBase
import models.UserRequest
import play.api.i18n.DefaultMessagesApi
import play.api.test.FakeRequest

class MessageRendererSpec extends SpecBase {

  val testMessages = Map(
    "default"-> Map(
      "test.message" -> "I am a {0} with no ARN",
      "agent.test.message" -> "I’m an {0} with an {1}"
    )
  )

  "getMessage" should {
    "load the relevant agent message " when {
      "the user is an agent" in {
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"))(fakeRequest)
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, fakeAgentRequest)
        result shouldBe "agent.message.key"
      }

      "apply the given arguments" in {
        val fakeAgentRequest = UserRequest(vrn = "123456789", arn = Some("AGENT1"))(fakeRequest)
        val messagesApi = new DefaultMessagesApi(testMessages)
        val messages = messagesApi.preferred(FakeRequest("GET", "/"))
        val fakeMessageKey = "test.message"
        val result = MessageRenderer.getMessage(fakeMessageKey, "agent", "ARN")(messages, fakeAgentRequest)
        result shouldBe "I’m an agent with an ARN"
      }
    }

    "load the normal message" when {
      "the user is a VAT trader" in {
        val fakeVATTraderRequest = UserRequest(vrn = "123456789")(fakeRequest)
        val fakeMessageKey = "message.key"
        val result = MessageRenderer.getMessage(fakeMessageKey)(implicitly, fakeVATTraderRequest)
        result shouldBe "message.key"
      }

      "apply the given arguments" in {
        val fakeVATTraderRequest = UserRequest(vrn = "123456789")(fakeRequest)
        val messagesApi = new DefaultMessagesApi(testMessages)
        val messages = messagesApi.preferred(FakeRequest("GET", "/"))
        val fakeMessageKey = "test.message"
        val result = MessageRenderer.getMessage(fakeMessageKey, "vat trader")(messages, fakeVATTraderRequest)
        result shouldBe "I am a vat trader with no ARN"
      }
    }
  }
}
