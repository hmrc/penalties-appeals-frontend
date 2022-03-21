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
import forms.HasHospitalStayEndedForm
import messages.HasTheHospitalStayEndedMessages._
import models.NormalMode
import models.appeals.HospitalStayEndInput
import models.pages.{DidHospitalStayEndPage, PageMode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.health.HasTheHospitalStayEndedPage
import viewtils.ConditionalRadioHelper

import java.time.LocalDate

class HasTheHospitalStayEndedPageSpec extends SpecBase with ViewBehaviours {
  "HasTheHospitalStayEndedPage" should {
    val conditionalRadioHelper = injector.instanceOf[ConditionalRadioHelper]
    val sampleStartDate = LocalDate.parse("2020-01-01")

    val hasTheHospitalStayEndedPage: HasTheHospitalStayEndedPage = injector.instanceOf[HasTheHospitalStayEndedPage]
    object Selectors extends BaseSelectors {
      val collapsableYesText = "#conditional-hasStayEnded div > fieldset > legend"

      val collapsableYesHintText = "#stayEndDate-hint"

      override val dateEntry: Int => String = (index: Int) => s"#stayEndDate > div:nth-child($index) > div > label"

      override val labelForRadioButton: Int => String = (index: Int) => if(index == 1) "label[for=hasStayEnded]" else s"label[for=hasStayEnded-$index]"
    }
    val formProvider: Form[HospitalStayEndInput] = HasHospitalStayEndedForm.hasHospitalStayEndedForm(sampleStartDate)
    val radioOptions = conditionalRadioHelper.conditionalYesNoOptions(formProvider, "healthReason.hasTheHospitalStayEnded")
    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      hasTheHospitalStayEndedPage.apply(form, radioOptions, controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(NormalMode),
        pageMode = PageMode(DidHospitalStayEndPage, NormalMode))
    }

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.labelForRadioButton(1) -> yesText,
      Selectors.labelForRadioButton(2) -> noText,
      Selectors.collapsableYesText -> whenDidItEnd,
      Selectors.collapsableYesHintText -> hintText,
      Selectors.dateEntry(1) -> dayEntry,
      Selectors.dateEntry(2) -> monthEntry,
      Selectors.dateEntry(3) -> yearEntry,
      Selectors.button -> continue
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
