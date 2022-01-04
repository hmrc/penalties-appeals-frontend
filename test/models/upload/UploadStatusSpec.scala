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

package models.upload

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class UploadStatusSpec extends AnyWordSpec with Matchers {
  "UploadStatus" should {
    "be writable to JSON (including optional fields)" in {
      val expectedJson = Json.parse(
        """
          |{
          | "status": "REJECTED",
          | "errorMessage": "error.message"
          |}
          |""".stripMargin)
      val modelToConvertToJson: UploadStatus = UploadStatus(
        status = "REJECTED",
        errorMessage = Some("error.message")
      )
      val result = Json.toJson(modelToConvertToJson)(UploadStatus.writes)
      result shouldBe expectedJson
    }

    "be writable to JSON (excluding optional fields)" in {
      val expectedJson = Json.parse(
        """
          |{
          | "status": "READY"
          |}
          |""".stripMargin)
      val modelToConvertToJson: UploadStatus = UploadStatus(
        status = "READY",
        errorMessage = None
      )
      val result = Json.toJson(modelToConvertToJson)(UploadStatus.writes)
      result shouldBe expectedJson
    }
  }
}
