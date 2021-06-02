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
import forms.HasCrimeBeenReportedForm
import messages.HasCrimeBeenReportedToPoliceMessages._
import models.NormalMode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.crime.HasCrimeBeenReportedToPolicePage
import viewtils.RadioOptionHelper

class HasCrimeBeenReportedToPolicePageSpec extends SpecBase with ViewBehaviours {
  "HasCrimeBeenReportedToPolicePage" should {
    val hasCrimeBeenReportedPage: HasCrimeBeenReportedToPolicePage = injector.instanceOf[HasCrimeBeenReportedToPolicePage]
    object Selectors extends BaseSelectors
    val formProvider = HasCrimeBeenReportedForm.hasCrimeBeenReportedForm
    val radioOptions = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formProvider)
    def applyView(form: Form[_]): HtmlFormat.Appendable = hasCrimeBeenReportedPage.apply(form, radioOptions, controllers.routes.CrimeReasonController.onSubmitForHasCrimeBeenReported(NormalMode))

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.labelForRadioButton(3) -> unknownOption,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
