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

package forms

import config.AppConfig
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.single
import utils.RegexHelper.textAreaRegex

object OtherRelevantInformationForm extends Mappings {
  def otherRelevantInformationForm()(implicit appConfig: AppConfig): Form[String] = Form[String](
    single(
      "other-relevant-information-text" -> text(errorKey = "otherRelevantInformation.error.required")
        .verifying("explainReason.charsInTextArea.error",
          value => value.length <= appConfig.numberOfCharsInTextArea)
        .verifying("otherReason.whyReturnSubmittedLate.error.regex",
          value => value.matches(textAreaRegex))
    )
  )
}
