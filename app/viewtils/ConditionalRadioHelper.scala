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

package viewtils

import config.AppConfig
import models.UserRequest
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.ViewUtils

import javax.inject.Inject

class ConditionalRadioHelper @Inject()(dateInput: views.html.components.inputDate) extends ViewUtils {

  def conditionalYesNoOptions(form: Form[_], messagePrefix: String)(implicit messages: Messages, userRequest: UserRequest[_], appConfig: AppConfig): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("yes"),
        content = Text(messages("common.radioOption.yes")),
        checked = form("hasStayEnded").value.contains("yes"),
        conditionalHtml = Some(html(dateInput(
          form = form,
          legendContent = messages(s"$messagePrefix.yes.heading"),
          legendClasses = Some("govuk-fieldset__legend govuk-label"),
          id = "stayEndDate",
          hintText = Some(messages("whenDidEventHappen.hint")),
          showPenaltyInformation = false
        )))
      ),
      RadioItem(
        value = Some("no"),
        content = Text(messages("common.radioOption.no")),
        checked = form("hasStayEnded").value.contains("no")
      )
    )
  }

}
