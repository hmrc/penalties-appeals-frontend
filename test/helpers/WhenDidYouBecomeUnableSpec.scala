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

package helpers

import base.SpecBase
import models.{PenaltyTypeEnum, UserRequest}
import play.api.test.FakeRequest
import utils.SessionKeys

class WhenDidYouBecomeUnableSpec extends SpecBase {

  "getPageHeadingMessageKey" should {
    "return the agent LPP text" when {
      "the agent is submitting an LPP appeal" in {
        val userRequest = UserRequest("123456789", arn = Some("1234"))(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "agent.key.lpp"
      }

      "the agent is submitting an LPP (additional penalty) appeal" in {
        val userRequest = UserRequest("123456789", arn = Some("1234"))(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "agent.key.lpp"
      }
    }

    "return the trader LPP text" when {
      "the trader is submitting an LPP appeal" in {
        val userRequest = UserRequest("123456789", arn = None)(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "key.lpp"
      }

      "the agent is submitting an LPP (additional penalty) appeal" in {
        val userRequest = UserRequest("123456789", arn = None)(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "key.lpp"
      }
    }

    "return the 'stop your client submitting return' wording when the client intended to submit" when {
      "the agent is submitting an LSP appeal" in {
        val userRequest = UserRequest("123456789", arn = Some("1234"))(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "agent.key.clientIntendedToSubmit"
      }
    }

    "return the 'stop your client getting information to you' wording" when {
      "the agent planned to submit and client missed deadline" in {
        val userRequest = UserRequest("123456789", arn = Some("1234"))(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "agent.key.clientMissedDeadline"
      }
    }

    "return the 'you' wording for LSP" when {
      "the agent planned to submit and they missed the deadline" in {
        val userRequest = UserRequest("123456789", arn = Some("1234"))(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "key.lsp"
      }

      "the trader is submitting appeal" in {
        val userRequest = UserRequest("123456789", arn = None)(FakeRequest().withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString
        ))
        val result = WhenDidYouBecomeUnableHelper.getPageHeadingMessageKey("key")(userRequest)
        result shouldBe "key.lsp"
      }
    }
  }
}
