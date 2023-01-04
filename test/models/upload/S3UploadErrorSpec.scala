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

package models.upload

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class S3UploadErrorSpec extends AnyWordSpec with Matchers {
  "S3UploadError" should {
    "be readable from JSON (not including optional fields)" in {
      val jsonToParse = Json.parse(
        """
          |{
          |   "key": "file1",
          |   "errorCode": "InternalError",
          |   "errorMessage": "Some arbitrary message"
          |}
          |""".stripMargin)
      val modelRepresentingJson = S3UploadError(
        key = "file1",
        errorCode = "InternalError",
        errorMessage = "Some arbitrary message"
      )
      val result = Json.fromJson(jsonToParse)(S3UploadError.reads)
      result.isSuccess shouldBe true
      result.get shouldBe modelRepresentingJson
    }

    "be readable from JSON (including optional fields)" in {
      val jsonToParse = Json.parse(
        """
          |{
          |   "key": "file1",
          |   "errorCode": "InternalError",
          |   "errorMessage": "Some arbitrary message",
          |   "errorRequestId": "request1",
          |   "errorResource": "resource1"
          |}
          |""".stripMargin)
      val modelRepresentingJson = S3UploadError(
        key = "file1",
        errorCode = "InternalError",
        errorMessage = "Some arbitrary message",
        errorRequestId = Some("request1"),
        errorResource = Some("resource1")
      )
      val result = Json.fromJson(jsonToParse)(S3UploadError.reads)
      result.isSuccess shouldBe true
      result.get shouldBe modelRepresentingJson
    }
  }
}
