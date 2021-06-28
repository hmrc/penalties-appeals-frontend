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

package models.appeals

import base.SpecBase
import play.api.libs.json.Json

class EvidenceSpec extends SpecBase {
  "Evidence" should {
    "be writable to JSON" in {
      val model = Evidence(1, "reference-1")
      val expectedResult = Json.parse(
        """
          |{
          | "noOfUploadedFiles": 1,
          | "referenceId": "reference-1"
          |}
          |""".stripMargin)
      val result = Json.toJson(model)(Evidence.format)
      result shouldBe expectedResult
    }
  }
}
