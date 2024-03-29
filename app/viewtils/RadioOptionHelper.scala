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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object RadioOptionHelper {
  def radioOptionsForHasCrimeBeenReportedPage(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("yes"),
        content = Text(messages("common.radioOption.yes")),
        checked = form("value").value.contains("yes")
      ),
      RadioItem(
        value = Some("no"),
        content = Text(messages("common.radioOption.no")),
        checked = form("value").value.contains("no")
      ),
      RadioItem(
        value = Some("unknown"),
        content = Text(messages("crimeReason.hasBeenReported.unknown")),
        checked = form("value").value.contains("unknown")
      )
    )
  }

  def radioOptionsForWhatCausedAgentToMissDeadline(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("client"),
        content = Text(messages("agents.whatCausedYouToMissTheDeadline.option.client")),
        checked = form("value").value.contains("client")
      ),
      RadioItem(
        value = Some("agent"),
        content = Text(messages("agents.whatCausedYouToMissTheDeadline.option.agent")),
        checked = form("value").value.contains("agent")
      )
    )
  }

  def yesNoRadioOptions(form: Form[_], formKey: String = "value", noContent: String = "common.radioOption.no", noHint: Option[String] = None)(implicit messages: Messages): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("yes"),
        content = Text(messages("common.radioOption.yes")),
        checked = form(formKey).value.contains("yes")
      ),
      RadioItem(
        value = Some("no"),
        content = Text(messages(noContent)),
        checked = form(formKey).value.contains("no"),
        hint = noHint.map {
          hint => Hint(content = HtmlContent(messages(hint)))
        }
      )
    )
  }

  def radioOptionsForCanYouPayPage(form: Form[_], vatAmount:String)(implicit messages: Messages): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("yes"),
        content = Text(messages("common.radioOption.yes.2", vatAmount)),
        checked = form("value").value.contains("yes")
      ),
      RadioItem(
        value = Some("no"),
        content = Text(messages("common.radioOption.no.3")),
        checked = form("value").value.contains("no")
      ),
      RadioItem(
        value = Some("paid"),
        content = Text(messages("common.radioOption.paid")),
        checked = form("value").value.contains("paid")
      )
    )
  }

  def radioOptionsForSubmitVATReturnPage(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = {
    Seq(
      RadioItem(
        value = Some("agent"),
        content = Text(messages("agents.whoPlannedToSubmitVATReturn.agent")),
        checked = form("value").value.contains("agent")
      ),
      RadioItem(
        value = Some("client"),
        content = Text(messages("agents.whoPlannedToSubmitVATReturn.client")),
        checked = form("value").value.contains("client")
      )
    )
  }
}
