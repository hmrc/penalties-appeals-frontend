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

package models.appeals

import base.SpecBase
import play.api.libs.json.Json

class AgentDetailsSpec extends SpecBase {
  "AgentDetails" should {
    "be writable to JSON" in {
      val model = AgentDetails("ARN12345678", true)
      val expectedResult = Json.parse(
        """
          |{
          | "agentReferenceNo": "ARN12345678",
          | "isExcuseRelatedToAgent": true
          |}
          |""".stripMargin)
      val result = Json.toJson(model)(AgentDetails.format)
      result shouldBe expectedResult
    }
  }
}
