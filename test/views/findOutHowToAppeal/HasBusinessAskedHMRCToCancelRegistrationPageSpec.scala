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

package views.findOutHowToAppeal

import base.{BaseSelectors, SpecBase}
import forms.HasBusinessAskedHMRCToCancelRegistrationForm
import messages.findOutHowToAppeal.HasBusinessAskedHMRCToCancelRegistrationMessages._
import models.NormalMode
import models.pages.{HasBusinessAskedHMRCToCancelRegistrationPage, PageMode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.HasBusinessAskedHMRCToCancelRegistrationPage
import viewtils.RadioOptionHelper

class HasBusinessAskedHMRCToCancelRegistrationPageSpec extends SpecBase with ViewBehaviours {
  "HasBusinessAskedHMRCToCancelRegistrationPage" should {

    val page: HasBusinessAskedHMRCToCancelRegistrationPage = injector.instanceOf[HasBusinessAskedHMRCToCancelRegistrationPage]

    object Selectors extends BaseSelectors

    val formProvider: Form[String] = HasBusinessAskedHMRCToCancelRegistrationForm.hasBusinessAskedHMRCToCancelRegistrationForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)

    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      page.apply(form, radioOptions, controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onSubmit(),
        pageMode = PageMode(HasBusinessAskedHMRCToCancelRegistrationPage, NormalMode))(implicitly, implicitly, userRequestWithCorrectKeys)
    }

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.hintText -> hintText,
      Selectors.labelForRadioButton(1) -> yesText,
      Selectors.labelForRadioButton(2) -> noText,
      Selectors.button -> submitButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
