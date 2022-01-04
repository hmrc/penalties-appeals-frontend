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

package forms.upscan

import forms.FormBehaviours

class S3UploadSuccessFormSpec extends FormBehaviours {
  "S3UploadSuccessForm" should {
    "bind when all fields are present" in {
      val formFields = Map(
        "key" -> "file1",
        "bucket" -> "bucket1",
      )
      val result = S3UploadSuccessForm.upscanUploadSuccessForm.bind(formFields)
      result.hasErrors shouldBe false
    }

    "bind when all mandatory fields are present" in {
      val formFields = Map(
        "key" -> "file1"
      )
      val result = S3UploadSuccessForm.upscanUploadSuccessForm.bind(formFields)
      result.hasErrors shouldBe false
    }

    "not bind when some fields are missing" in {
      val formFields = Map(
        "bucket" -> "bucket1"
      )
      val result = S3UploadSuccessForm.upscanUploadSuccessForm.bind(formFields)
      result.hasErrors shouldBe true
    }
  }
}
