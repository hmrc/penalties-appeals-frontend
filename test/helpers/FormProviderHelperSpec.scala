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

import java.time.LocalDate

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

  "getSessionKeyAndAttemptToFillAnswerAsOptionString" should {
    "fill the form with the data stored in the session - if it exists" in {
      val mockForm: Form[Option[String]] = Form(optional(single("value" -> text)))
      val expectedResult = mockForm.fill(Some("Lorem ipsum"))
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsOptionString(mockForm, SessionKeys.lateAppealReason)(fakeRequest.withSession(
        SessionKeys.lateAppealReason -> "Lorem ipsum"
      ))
      result shouldBe expectedResult
    }

    "keep the existing (empty) form when the key in the session" in {
      val mockForm: Form[Option[String]] = Form(optional(single("value" -> text)))
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsOptionString(mockForm, SessionKeys.lateAppealReason)(fakeRequest.withSession(
        "this-is-a-key" -> "this-is-a-value"
      ))
      result shouldBe mockForm
    }
  }

  "getSessionKeyAndAttemptToFillAnswerAsDate" should {
    "fill the form with the data stored in the session - if it exists" in {
      val mockForm: Form[LocalDate] = Form(single("value" -> localDate))
      val expectedResult = mockForm.fill(LocalDate.parse("2021-01-01"))
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(mockForm, SessionKeys.dateOfCrime)(fakeRequest.withSession(
        SessionKeys.dateOfCrime -> "2021-01-01"
      ))
      result shouldBe expectedResult
    }

    "keep the existing (empty) form when the key in the session" in {
      val mockForm: Form[LocalDate] = Form(single("value" -> localDate))
      val result = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(mockForm, SessionKeys.reasonableExcuse)(fakeRequest.withSession(
        "this-is-a-key" -> "this-is-a-value"
      ))
      result shouldBe mockForm
    }
  }
}
