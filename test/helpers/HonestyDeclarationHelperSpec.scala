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
import models.UserRequest
import play.api.libs.json.Json
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

      def sessionKeys(whoPlanned: String, cause: Option[String], excuse: String): UserRequest[AnyContent] =
        UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers(correctUserAnswers ++ Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> whoPlanned,
          SessionKeys.reasonableExcuse -> excuse
        ) ++ {
          if (cause.isDefined) Json.obj(SessionKeys.whatCausedYouToMissTheDeadline -> cause.get) else Json.obj()
        }))

      "for other reason" should {
        "the agent planned to submit - client missed deadline" should {
          reasonTest("other", "of an issue affecting my client", sessionKeys("agent", Some("client"), "other"))
        }
      }

      "non crime/bereavement reason" should {

        "the agent planned to submit - agent missed the deadline" should {
          reasonTest("technicalIssues", "of technology issues", sessionKeys("agent", Some("agent"), "technicalIssues"))
        }

        "the agent planned to submit - client missed the deadline" should {
          reasonTest("technicalIssues", "my client was affected by technology issues", sessionKeys("agent", Some("client"), "technicalIssues"))
        }

        "the client planned to submit" should {
          reasonTest("technicalIssues", "of technology issues", sessionKeys("client", None, "technicalIssues"))
        }
      }

      "crime/bereavement reason" should {

        "the agent planned to submit - agent missed the deadline" should {
          reasonTest("crime", "I was affected by a crime", sessionKeys("agent", Some("agent"), "crime"))
        }

        "the agent planned to submit - client missed the deadline" should {
          reasonTest("crime", "my client was affected by a crime", sessionKeys("agent", Some("client"), "crime"))
        }

        "the client planned to submit" should {
          reasonTest("crime", "my client was affected by a crime", sessionKeys("client", None, "crime"))
        }
      }
    }

    "return the correct VAT trader message" when {
      reasonTest("crime", "I was affected by a crime", vatTraderLSPUserRequest)
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
