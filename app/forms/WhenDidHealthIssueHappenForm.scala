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

import java.time.LocalDate

import forms.mappings.Mappings
import models.UserRequest
import play.api.data.Form
import play.api.i18n.Messages
import utils.MessageRenderer.getMessageKey

object WhenDidHealthIssueHappenForm extends Mappings {

  def whenHealthIssueHappenedForm()(implicit messages: Messages, user: UserRequest[_]): Form[LocalDate] = {
    Form(
      "date" -> localDate(
        invalidKey = getMessageKey("healthReason.whenHealthIssueHappened.error.invalid"),
        allRequiredKey = getMessageKey("healthReason.whenHealthIssueHappened.error.required.all"),
        twoRequiredKey = getMessageKey("healthReason.whenHealthIssueHappened.error.required.two"),
        requiredKey = getMessageKey("healthReason.whenHealthIssueHappened.error.required"),
        fieldLengthKey = getMessageKey("healthReason.whenHealthIssueHappened.error.invalid"),
        futureKey = Some(getMessageKey("healthReason.whenHealthIssueHappened.error.notInFuture"))
      )
    )
  }

}
