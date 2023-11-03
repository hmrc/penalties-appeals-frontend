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
import forms.DoYouWantToPayNowForm
import org.jsoup.nodes.Document
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.YouCanAppealOnlinePage
import viewtils.RadioOptionHelper
import messages.YouCanAppealOnlineAfterYouPayMessages._
import models.pages.{PageMode, YouCanAppealOnlinePage}
import models.NormalMode

class YouCanAppealOnlinePageSpec extends SpecBase with ViewBehaviours {
  "YouCanAppealOnline" should {

    val youCanAppealOnlinePage = injector.instanceOf[YouCanAppealOnlinePage]
    object Selectors extends BaseSelectors {
      val radioHeader = "#radio-heading"
      val noHint = "#value-2-item-hint"
    }
    val formProvider = DoYouWantToPayNowForm.doYouWantToPayNowForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider,
      noContent = "common.radioOption.no.2", noHint = Some("common.radioOption.no.hint"))
    def applyView(form: Form[_]) = youCanAppealOnlinePage(form,
      radioOptions, controllers.routes.YouCanAppealOnlineController.onSubmit(),
      pageMode = PageMode(YouCanAppealOnlinePage, NormalMode))(implicitly, implicitly, userRequestWithCorrectKeys)

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.pElementIndex(3) -> p1,
      Selectors.pElementIndex(4) -> p2,
      Selectors.radioHeader -> radioHeading,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.noHint -> noHint
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}