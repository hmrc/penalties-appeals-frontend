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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}

class ModeSpec extends AnyWordSpec with Matchers {
  "Mode" should {
    "be writable to JSON for NormalMode" in {
      val expectedResult = JsString("NormalMode")
      Json.toJson[Mode](NormalMode)(Mode.writes) shouldBe expectedResult
    }

    "be writable to JSON for CheckMode" in {
      val expectedResult = JsString("CheckMode")
      Json.toJson[Mode](CheckMode)(Mode.writes) shouldBe expectedResult
    }

    "be writable to a JavascriptLiteral for NormalMode" in {
      Mode.jsLiteral.to(NormalMode) shouldBe "\"NormalMode\""
    }

    "be writable to a JavascriptLiteral for CheckMode" in {
      Mode.jsLiteral.to(CheckMode) shouldBe "\"CheckMode\""
    }
  }
}
