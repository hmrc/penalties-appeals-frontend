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

package models

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime

class AppealDataSpec extends WordSpec with Matchers {
  val expectedModelAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00"
      |}
      |""".stripMargin)

  val expectedModel = AppealData(
    `type` = PenaltyTypeEnum.Late_Submission,
    startDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
    endDate = LocalDateTime.of(2020, 1, 1, 13, 0, 0)
  )

  "AppealData" should {
    "be readable to JSON" in {
      val result = Json.toJson(expectedModel)
      result shouldBe expectedModelAsJson
    }

    "be writable from JSON" in {
      val result = Json.fromJson(expectedModelAsJson)(AppealData.format)
      result.isSuccess shouldBe true
      result.get shouldBe expectedModel
    }
  }
}
