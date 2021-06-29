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

package forms

import base.SpecBase

class UploadEvidenceFormSpec extends SpecBase {
  val form = UploadEvidenceForm.uploadEvidenceForm

  "UploadEvidenceForm" should {
    "bind when a file is uploaded" in {
      val result = form.bind(
        Map(
          "upload-evidence" -> "test.png"
        )
      )
      result.errors shouldBe empty
    }

    "bind when no file is uploaded" in {
      val result = form.bind(
        Map(
          "upload-evidence" -> ""
        )
      )
      result.errors shouldBe empty
    }
  }
}
