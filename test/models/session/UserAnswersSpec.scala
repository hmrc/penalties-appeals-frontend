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

package models.session

import base.SpecBase
import models.session.UserAnswers.SensitiveJsObject
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.Crypted
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

class UserAnswersSpec extends SpecBase {
  lazy val answersAsJson: SensitiveJsObject = SensitiveJsObject(Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))
  lazy val now: Instant = Instant.now

  "be writable to JSON" in {
    val model = UserAnswers("journey123", answersAsJson, now)
    val expectedResult = Json.obj(
      "journeyId" -> "journey123",
      "data" -> answersAsJson,
      "lastUpdated" -> now
    )
    val result = Json.toJson(model)(UserAnswers.format)
    (result \ "journeyId").validate[String].get shouldBe (expectedResult \ "journeyId").validate[String].get
    val actualResultData = (result \ "data").validate[String].get
    val expectedResultData = (expectedResult \ "data").validate[String].get
    crypto.decrypt(Crypted(actualResultData)).value shouldBe crypto.decrypt(Crypted(expectedResultData)).value
  }

  "be readable from JSON" in {
    val json = Json.obj(
      "journeyId" -> "journey123",
      "data" -> answersAsJson,
      "lastUpdated" -> Json.toJson(now)(MongoJavatimeFormats.instantWrites)
    )
    val expectedModel = UserAnswers("journey123", answersAsJson, now)
    val result = Json.fromJson(json)(UserAnswers.format)
    result.isSuccess shouldBe true
    result.get.journeyId shouldBe expectedModel.journeyId
    result.get.data.decryptedValue shouldBe expectedModel.data.decryptedValue
  }

  "setAnswer" should {
    "store the answer in the data field" in {
      val answers = UserAnswers("journey123", answersAsJson)
      val newAnswers = answers.setAnswer("key3", "value3")
      newAnswers.getAnswer[String]("key3").get shouldBe "value3"
    }

    "overwrite the answer if already present in the data field" in {
      val answers = UserAnswers("journey123", answersAsJson)
      val newAnswers = answers.setAnswer("key2", "value3")
      newAnswers.getAnswer[String]("key2").get shouldBe "value3"
    }
  }

  "getAnswer" should {
    "return Some" when {
      "the answer is in the data field" in {
        val answers = UserAnswers("journey123", answersAsJson)
        val result = answers.getAnswer[String]("key2")
        result.isDefined shouldBe true
        result.get shouldBe "value2"
      }
    }

    "return None" when {
      "the answer is not in the data field" in {
        val answers = UserAnswers("journey123", answersAsJson)
        val result = answers.getAnswer[String]("key3")
        result.isEmpty shouldBe true
      }
    }
  }
}
