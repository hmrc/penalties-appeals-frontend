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
import forms.WhatCausedYouToMissTheDeadlineForm
import messages.WhatCausedYouToMissTheDeadlineMessages._
import models.pages.{PageMode, WhatCausedYouToMissTheDeadlinePage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.agents.WhatCausedYouToMissTheDeadlinePage
import viewtils.RadioOptionHelper

class WhatCausedYouToMissTheDeadlinePageSpec extends SpecBase with ViewBehaviours {
  "WhatCausedYouToMissTheDeadlinePage" should {
    val whatCausedYouToMissTheDeadlinePage: WhatCausedYouToMissTheDeadlinePage = injector.instanceOf[WhatCausedYouToMissTheDeadlinePage]
    val formProvider = WhatCausedYouToMissTheDeadlineForm.whatCausedYouToMissTheDeadlineForm
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    object Selectors extends BaseSelectors

    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      whatCausedYouToMissTheDeadlinePage.apply(form,
        RadioOptionHelper.radioOptionsForWhatCausedAgentToMissDeadline(formProvider),
          controllers.routes.AgentsController.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode), pageMode = PageMode(WhatCausedYouToMissTheDeadlinePage, NormalMode)
      )
    }

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.legend -> heading,
      Selectors.labelForRadioButton(1) -> clientRadioOption,
      Selectors.labelForRadioButton(2) -> agentRadioOption,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
