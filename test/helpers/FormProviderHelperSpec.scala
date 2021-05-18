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

package helpers

import base.SpecBase
import play.api.data.Form
import play.api.data.Forms._
import utils.SessionKeys

class FormProviderHelperSpec extends SpecBase {

  "getSessionKeyAndAttemptToFillAnswerAsString" should {
    "fill the form with the data stored in the session - if it exists" in {
      val mockForm: Form[String] = Form(single("value" -> nonEmptyText))
      val expectedResult = mockForm.fill("this-is-a-value")
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(mockForm, SessionKeys.reasonableExcuse)(fakeRequest.withSession(
        SessionKeys.reasonableExcuse -> "this-is-a-value"
      ))
      result shouldBe expectedResult
    }

    "keep the existing (empty) form when the key in the session" in {
      val mockForm: Form[String] = Form(single("value" -> nonEmptyText))
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(mockForm, SessionKeys.reasonableExcuse)(fakeRequest.withSession(
        "this-is-a-key" -> "this-is-a-value"
      ))
      result shouldBe mockForm
    }
  }
}
