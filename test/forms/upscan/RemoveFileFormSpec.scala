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

package forms.upscan

import base.SpecBase
import forms.FormBehaviours
import play.api.data.Form

class RemoveFileFormSpec extends FormBehaviours with SpecBase {
  val form: Form[String] = RemoveFileForm.form
  "RemoveFileForm" should {
    s"not bind when key is not present at all" in {
      val result = form.bind(emptyForm).apply("fileReference")
      result.hasErrors shouldBe true
    }

    s"not bind blank values" in {
      val result = form.bind(Map("fileReference" -> "")).apply("fileReference")
      result.hasErrors shouldBe true
    }
  }
}
