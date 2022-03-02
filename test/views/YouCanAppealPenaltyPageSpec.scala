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

package views

import base.{BaseSelectors, SpecBase}
import forms.YouCanAppealPenaltyForm
import messages.YouCanAppealThisPenaltyMessages._
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.YouCanAppealPenaltyPage
import viewtils.RadioOptionHelper

class YouCanAppealPenaltyPageSpec extends SpecBase with ViewBehaviours {
  "YouCanAppealPenaltyPage" should {
    val youCanAppealPenaltyPage: YouCanAppealPenaltyPage = injector.instanceOf[YouCanAppealPenaltyPage]
    object Selectors extends BaseSelectors {
      val legend = ".govuk-fieldset__legend"

      val p = "p.govuk-body"

      val li1 = ".govuk-list > li:first-child"

      val li2 = ".govuk-list > li:nth-of-type(2)"
    }
    val formProvider = YouCanAppealPenaltyForm.youCanAppealPenaltyForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)

    def applyView(form: Form[_]): HtmlFormat.Appendable = youCanAppealPenaltyPage
      .apply(form, radioOptions, controllers.routes.YouCanAppealPenaltyController.onSubmit())

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.p -> p1,
      Selectors.li1 -> li1,
      Selectors.li2 -> li2,
      Selectors.legend -> questionText,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.button -> continueButton
    )
    behave like pageWithExpectedMessages(expectedContent)
  }
}
