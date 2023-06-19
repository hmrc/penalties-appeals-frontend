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

package helpers

import base.SpecBase
import models.session.UserAnswers
import models.{PenaltyTypeEnum, UserRequest}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import utils.SessionKeys

class WhenDidYouBecomeUnableHelperSpec extends SpecBase {

  "getMessageKeyForPage" should {
    "return the agent LPP text" when {
      "the agent is submitting an LPP appeal" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment
        ))
        val userRequest = UserRequest("123456789", arn = Some("1234"), answers = userAnswers)(FakeRequest())
        val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
        result shouldBe "agent.key.lpp"
      }

      "the agent is submitting an LPP (additional penalty) appeal" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Additional
        ))
        val userRequest = UserRequest("123456789", arn = Some("1234"), answers = userAnswers)(FakeRequest())
        val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
        result shouldBe "agent.key.lpp"
      }
    }

    "return the trader LPP text" when {
      "the trader is submitting an LPP appeal" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment
        ))
        val userRequest = UserRequest("123456789", arn = None, answers = userAnswers)(FakeRequest())
        val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
        result shouldBe "key.lpp"
      }

      "the agent is submitting an LPP (additional penalty) appeal" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Additional
        ))
        val userRequest = UserRequest("123456789", arn = None, answers = userAnswers)(FakeRequest())
        val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
        result shouldBe "key.lpp"
      }
    }

    "return the correct wording when the client intended to submit and the agent is submitting an LSP appeal" in {
      val userAnswers = UserAnswers("1234", Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.whoPlannedToSubmitVATReturn -> "client"
      ))
      val userRequest = UserRequest("123456789", arn = Some("1234"), answers = userAnswers)(FakeRequest())
      val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
      result shouldBe "agent.key.clientIntendedToSubmit"
    }
  }

  "return the correct wording when the agent planned to submit and client missed deadline" in {
    val userAnswers = UserAnswers("1234", Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
      SessionKeys.whatCausedYouToMissTheDeadline -> "client"
    ))
    val userRequest = UserRequest("123456789", arn = Some("1234"), answers = userAnswers)(FakeRequest())
    val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
    result shouldBe "agent.key.clientMissedDeadline"
  }

  "return the correct wording when the agent planned to submit and they missed the deadline" in {
    val userAnswers = UserAnswers("1234", Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
      SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
    ))
    val userRequest = UserRequest("123456789", arn = Some("1234"), answers = userAnswers)(FakeRequest())
    val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
    result shouldBe "key.lsp"
  }

  "the trader is submitting appeal" in {
    val userAnswers = UserAnswers("1234", Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission
    ))
    val userRequest = UserRequest("123456789", arn = None, answers = userAnswers)(FakeRequest())
    val result = WhenDidYouBecomeUnableHelper.getMessageKeyForPage("key")(userRequest)
    result shouldBe "key.lsp"
  }
}
