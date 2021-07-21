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

package helpers

import base.SpecBase

class HonestyDeclarationHelperSpec extends SpecBase {
  "getReasonText" should {

    def reasonTest(reasonableExcuse: String, expectedText: String): Unit = {
      s"the reason is '$reasonableExcuse' - text should be $expectedText" in {
        val result = HonestyDeclarationHelper.getReasonText(reasonableExcuse)
        result shouldBe expectedText
      }
    }

    "return the correct message" when {
      reasonTest("crime", "I was affected by a crime")
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
