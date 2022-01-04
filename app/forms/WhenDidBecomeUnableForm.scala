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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages
import utils.MessageRenderer.getMessageKey
import java.time.LocalDate

import models.UserRequest

object WhenDidBecomeUnableForm extends Mappings {
  def whenDidBecomeUnableForm()(implicit messages: Messages, user: UserRequest[_]): Form[LocalDate] = {
    Form(
      "date" -> localDate(
      invalidKey = getMessageKey("otherReason.whenDidBecomeUnable.error.invalid"),
      allRequiredKey = getMessageKey("otherReason.whenDidBecomeUnable.error.required.all"),
      twoRequiredKey = getMessageKey("otherReason.whenDidBecomeUnable.error.required.two"),
      requiredKey = getMessageKey("otherReason.whenDidBecomeUnable.error.required"),
      fieldLengthKey = getMessageKey("otherReason.whenDidBecomeUnable.error.invalid"),
      futureKey = Some(getMessageKey("otherReason.whenDidBecomeUnable.error.notInFuture"))
      )
    )
  }
}
