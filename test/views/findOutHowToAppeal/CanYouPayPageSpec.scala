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
import forms.CanYouPayForm
import messages.findOutHowToAppeal.CanYouPayMessages._
import models.pages.{CanYouPayPage, PageMode}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.CanYouPayPage
import viewtils.RadioOptionHelper

class CanYouPayPageSpec extends SpecBase with ViewBehaviours {
  "CanYouPayPage" should {
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    val canYouPayPage: CanYouPayPage = injector.instanceOf[CanYouPayPage]
    object Selectors extends BaseSelectors {
      val formHint = "#value-hint"
    }

    val formProvider = CanYouPayForm.canYouPayForm
    val radioOptions = RadioOptionHelper.radioOptionsForCanYouPayPage(formProvider, "123.45")

    def applyView(form: Form[_]): HtmlFormat.Appendable = canYouPayPage
      .apply(form, radioOptions,
        pageMode = PageMode(CanYouPayPage, NormalMode))

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.formHint -> radioHintText,
      Selectors.labelForRadioButton(1) -> radioOption1,
      Selectors.labelForRadioButton(2) -> radioOption2,
      Selectors.labelForRadioButton(3) -> radioOption3,
      Selectors.button -> submitButton
    )
    behave like pageWithExpectedMessages(expectedContent)
  }
}
