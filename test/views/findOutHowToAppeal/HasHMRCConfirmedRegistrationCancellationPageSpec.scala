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
import forms.HasHMRCConfirmedRegistrationCancellationForm
import messages.findOutHowToAppeal.HasHMRCConfirmedRegistrationCancellationMessages._
import models.NormalMode
import models.pages.{HasHMRCConfirmedRegistrationCancellationPage, PageMode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.HasHMRCConfirmedRegistrationCancellationPage
import viewtils.RadioOptionHelper

class HasHMRCConfirmedRegistrationCancellationPageSpec extends SpecBase with ViewBehaviours {
  "HasHMRCConfirmedRegistrationCancellationPage" should {

    val page: HasHMRCConfirmedRegistrationCancellationPage = injector.instanceOf[HasHMRCConfirmedRegistrationCancellationPage]

    object Selectors extends BaseSelectors

    val formProvider: Form[String] = HasHMRCConfirmedRegistrationCancellationForm.hasHMRCConfirmedRegistrationCancellationForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)

    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      page.apply(form, radioOptions, controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onSubmit(),
        pageMode = PageMode(HasHMRCConfirmedRegistrationCancellationPage, NormalMode))(implicitly, implicitly, userRequestWithCorrectKeys)
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
