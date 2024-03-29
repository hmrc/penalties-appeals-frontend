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

package views

import base.{BaseSelectors, SpecBase}
import forms.HasHospitalStayEndedForm
import messages.HasTheHospitalStayEndedMessages._
import models.NormalMode
import models.pages.{DidHospitalStayEndPage, PageMode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.health.HasTheHospitalStayEndedPage
import viewtils.RadioOptionHelper

class HasTheHospitalStayEndedPageSpec extends SpecBase with ViewBehaviours {
  "HasTheHospitalStayEndedPage" should {

    val hasTheHospitalStayEndedPage: HasTheHospitalStayEndedPage = injector.instanceOf[HasTheHospitalStayEndedPage]
    object Selectors extends BaseSelectors {
      override val labelForRadioButton: Int => String = (index: Int) => if(index == 1) "label[for=hasStayEnded]" else s"label[for=hasStayEnded-$index]"
    }
    val formProvider: Form[String] = HasHospitalStayEndedForm.hasHospitalStayEndedForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider, "hasStayEnded")
    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      hasTheHospitalStayEndedPage.apply(form, radioOptions, controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(NormalMode),
        pageMode = PageMode(DidHospitalStayEndPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly)
    }

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.labelForRadioButton(1) -> yesText,
      Selectors.labelForRadioButton(2) -> noText,
      Selectors.button -> continue
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
