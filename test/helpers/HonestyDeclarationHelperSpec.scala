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
import play.api.mvc.AnyContent
import utils.SessionKeys

class HonestyDeclarationHelperSpec extends SpecBase {
  "getReasonText" should {

    def reasonTest(reasonableExcuse: String, expectedText: String, user: UserRequest[_]): Unit = {
      s"the reason is '$reasonableExcuse' - text should be $expectedText" in {
        val result = HonestyDeclarationHelper.getReasonText(reasonableExcuse)(messages, user)
        result shouldBe expectedText
      }
    }

    "return the correct agent message" when {
      "the user is an agent and the client planned to submit" should {
        reasonTest("crime", "my client was affected by a crime", agentUserSessionKeys)
      }

      "the agent planned to submit and missed the deadline (LSP)" should {
        val agentUserSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(agentRequest.withSession(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "agent",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString)
        )
        reasonTest("crime", "my client was affected by a crime", agentUserSessionKeys)
      }
    }

    "return the correct VAT trader message" when {
      reasonTest("crime", "I was affected by a crime", vatTraderUser)
    }
  }

  "getExtraText" should {
    "return the correct message key(s) for 'loss of staff'" in {
      val expectedMsgKey: String = "honestyDeclaration.li.extra.lossOfStaff"
      val actualMsgKey: Seq[String] = HonestyDeclarationHelper.getExtraText("lossOfStaff")
      actualMsgKey.size shouldBe 1
      actualMsgKey.head shouldBe expectedMsgKey
    }

    "return the correct message key(s) for 'health'" in {
      val expectedMsgKey: String = "honestyDeclaration.li.extra.health"
      val actualMsgKey: Seq[String] = HonestyDeclarationHelper.getExtraText("health")
      actualMsgKey.size shouldBe 1
      actualMsgKey.head shouldBe expectedMsgKey
    }

    "return an empty Seq when the reasonable excuse is not included in the match statements" in {
      val actualMsgKey: Seq[String] = HonestyDeclarationHelper.getExtraText("crime")
      actualMsgKey.isEmpty shouldBe true
    }
  }
}
