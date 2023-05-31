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

package helpers

import base.SpecBase
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import utils.SessionKeys

import java.time.LocalDate

class FormProviderHelperSpec extends SpecBase {

  "getSessionKeyAndAttemptToFillAnswerAsString" should {
    val mockForm: Form[String] = Form(single("value" -> nonEmptyText))
    def result(jsObject: JsObject): Form[String] =
      FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(mockForm, SessionKeys.reasonableExcuse, userAnswers(correctUserAnswers ++ jsObject))
    "fill the form with the data stored in the session - if it exists" in {
      val expectedResult = mockForm.fill("this-is-a-value")
      result(Json.obj(SessionKeys.reasonableExcuse -> "this-is-a-value")) shouldBe expectedResult
    }

    "keep the existing (empty) form when the key in the session" in {
      result(Json.obj("this-is-a-key" -> "this-is-a-value")) shouldBe mockForm
    }
  }

  "getSessionKeyAndAttemptToFillAnswerAsDate" should {
    val mockForm: Form[LocalDate] = Form(single("value" -> localDate))
    def result(jsObject: JsObject): Form[LocalDate] =
      FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(mockForm, SessionKeys.dateOfCrime, userAnswers(correctUserAnswers ++ jsObject))
    "fill the form with the data stored in the session - if it exists" in {
      val expectedResult = mockForm.fill(LocalDate.parse("2021-01-01"))
      result(Json.obj(SessionKeys.dateOfCrime -> LocalDate.parse("2021-01-01"))) shouldBe expectedResult
    }

    "keep the existing (empty) form when the key in the session" in {
      result(Json.obj("this-is-a-key" -> "this-is-a-value")) shouldBe mockForm
    }
  }
}
