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
import forms.WhyReturnSubmittedLateForm
import messages.WhyReturnSubmittedLateMessages._
import models.pages.{PageMode, YouCanAppealThisPenaltyPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.WhyReturnSubmittedLatePage

class WhyReturnSubmittedLatePageSpec extends SpecBase with ViewBehaviours {
  "WhyReturnSubmittedLatePage" should {
    val whyReturnSubmittedLate: WhyReturnSubmittedLatePage = injector.instanceOf[WhyReturnSubmittedLatePage]

    object Selectors extends BaseSelectors {
      val label = "#main-content .govuk-label--l"
    }

    def applyView(form: Form[_], request: UserRequest[_] = userRequestWithCorrectKeys): HtmlFormat.Appendable = whyReturnSubmittedLate.apply(form,
      controllers.routes.OtherReasonController.onSubmitForWhyReturnSubmittedLate(NormalMode),
      pageMode = PageMode(YouCanAppealThisPenaltyPage, NormalMode))(request, implicitly, implicitly)

    val formProvider = WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm()(appConfig, userRequestWithCorrectKeys)

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.label -> heading,
      Selectors.hintText -> hintTextMultiple,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "display the LPP variation when the appeal is for a LPP" must {
      implicit val doc: Document = asDocument(applyView(formProvider, userRequestLPPWithCorrectKeys))

      val expectedContent = Seq(
        Selectors.title -> titleLpp,
        Selectors.label -> headingLpp,
        Selectors.hintText -> hintTextMultiple,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}
