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
import forms.WhoPlannedToSubmitVATReturnAgentForm
import messages.WhoPlannedToSubmitVATReturnAgentMessages._
import models.pages.{PageMode, WhoPlannedToSubmitVATReturnAgentPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.agents.WhoPlannedToSubmitVATReturnAgentPage
import viewtils.RadioOptionHelper

class WhoPlannedToSubmitVATReturnAgentPageSpec extends SpecBase with ViewBehaviours {
  val whoPlannedToSubmitVATReturnPage: WhoPlannedToSubmitVATReturnAgentPage = injector.instanceOf[WhoPlannedToSubmitVATReturnAgentPage]
  object Selectors extends BaseSelectors
  val formProvider: Form[String] = WhoPlannedToSubmitVATReturnAgentForm.whoPlannedToSubmitVATReturnForm
  implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

  def applyView(form: Form[_]): HtmlFormat.Appendable = whoPlannedToSubmitVATReturnPage.apply(form,
    RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formProvider),
    controllers.routes.AgentsController.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode), pageMode = PageMode(WhoPlannedToSubmitVATReturnAgentPage, NormalMode))

  "WhoPlannedToSubmitVATReturnPage" should {

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.labelForRadioButton(1) -> agentDidOption,
      Selectors.labelForRadioButton(2) -> clientDidOption,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }

}
