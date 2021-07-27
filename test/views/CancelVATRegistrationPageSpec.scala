/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.CancelVATRegistrationForm
import models.NormalMode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CancelVATRegistrationPage
import viewtils.RadioOptionHelper
import messages.CancelVATRegistrationMessages._

class CancelVATRegistrationPageSpec extends SpecBase with ViewBehaviours {
  "CancelVATRegistrationPage" should {
    val cancelVATRegistrationPage: CancelVATRegistrationPage = injector.instanceOf[CancelVATRegistrationPage]
    object Selectors extends BaseSelectors
    val formProvider = CancelVATRegistrationForm.cancelVATRegistrationForm
    val radioOptions = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formProvider)
    def applyView(form: Form[_]): HtmlFormat.Appendable = cancelVATRegistrationPage
      .apply(form, radioOptions, controllers.routes.CrimeReasonController.onSubmitForHasCrimeBeenReported(NormalMode))

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
