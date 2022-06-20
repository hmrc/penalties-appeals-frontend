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

package forms

import base.SpecBase
import config.AppConfig
import org.mockito.Mockito.mock
import play.api.Configuration
import play.api.data.{Form, FormError}

class MakingALateAppealFormSpec extends SpecBase with FormBehaviours {
  val form: Form[String] = MakingALateAppealForm.makingALateAppealForm()
  "MakingALateAppealForm" should {
    "bind" when {
      behave like mandatoryField(form, "late-appeal-text",
        FormError("late-appeal-text", "makingALateAppeal.error.required"))
      "a text value less than 5000 entered for bereavement" in {
        val result = form.bind(
          Map(
            "late-appeal-text" -> "Some reason."
          )
        )
        result.hasErrors shouldBe false
      }
    }

    "More than 5000 characters give required error and not bind" in {
      val result = form.bind(Map("late-appeal-text" -> moreThanFiveThousandChars)).apply("late-appeal-text")
      result.errors.headOption shouldBe Some(FormError("late-appeal-text", "explainReason.charsInTextArea.error"))
    }
  }
}
