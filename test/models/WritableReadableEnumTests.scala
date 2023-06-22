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
import play.api.libs.json._

trait WritableReadableEnumTests {
  _: AnyWordSpec with Matchers =>
  def readableTest[A](expectedResult: A, enumAsString: String, testName: String)(implicit formatter: Format[A]): Unit = {
    testName in {
      val result = Json.fromJson(JsString(enumAsString))
      result.isSuccess shouldBe true
      result.get shouldBe expectedResult
    }
  }

  def readableTestError[A](enumAsString: String, testName: String)(implicit formatter: Format[A]): Unit = {
    testName in {
      val result = Json.fromJson(JsString(enumAsString))
      result.isError shouldBe true
    }
  }

  def writableTest[A](`enum`: A, expectedResult: String, testName: String)(implicit formatter: Format[A]): Unit = {
    testName in {
      val result = Json.toJson(`enum`)
      result shouldBe JsString(expectedResult)
    }
  }
}
