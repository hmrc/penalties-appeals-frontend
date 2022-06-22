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

import config.AppConfig

import java.time.LocalDate
import forms.mappings.Mappings
import helpers.WhenDidYouBecomeUnableHelper.getMessageKeyForPage
import models.UserRequest
import play.api.data.Form
import play.api.i18n.Messages

object WhenDidHealthIssueHappenForm extends Mappings {

  def whenHealthIssueHappenedForm()(implicit messages: Messages, appConfig: AppConfig, user: UserRequest[_]): Form[LocalDate] = {
    Form(
      "date" -> localDate(
        invalidKey = getMessageKeyForPage("health.whenHealthIssueHappened.error.invalid"),
        allRequiredKey = getMessageKeyForPage("health.whenHealthIssueHappened.error.required.all"),
        twoRequiredKey = getMessageKeyForPage("health.whenHealthIssueHappened.error.required.two"),
        requiredKey = getMessageKeyForPage("health.whenHealthIssueHappened.error.required"),
        futureKey = Some(getMessageKeyForPage("health.whenHealthIssueHappened.error.notInFuture"))
      )
    )
  }

}
