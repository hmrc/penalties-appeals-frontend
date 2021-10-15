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

package models.upload

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class UpscanInitiateRequestSpec extends SpecBase {

  val modelAsJson: JsValue = Json.parse(
    """
      |{
      | "callbackUrl": "/foo/bar",
      | "successRedirect": "/foo/bar/success",
      | "errorRedirect": "/foo/bar/error",
      | "minimumFileSize": 1,
      | "maximumFileSize": 10000
      |}
      |""".stripMargin)

  val model: UpscanInitiateRequest = UpscanInitiateRequest(
    "/foo/bar",
    Some("/foo/bar/success"),
    Some("/foo/bar/error"),
    Some(1),
    Some(10000)
  )

  "UpscanInitiateRequest" should {
    "be writable to JSON" in {
      val result = Json.toJson(model)
      result shouldBe modelAsJson
    }
  }

}
