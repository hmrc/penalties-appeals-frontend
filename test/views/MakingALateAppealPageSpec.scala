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
import forms.MakingALateAppealForm
import messages.MakingALateAppealMessages._
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.MakingALateAppealPage

class MakingALateAppealPageSpec extends SpecBase with ViewBehaviours {
  "MakingALateAppealPage" should {
    val makingALateAppealPage: MakingALateAppealPage = injector.instanceOf[MakingALateAppealPage]
    object Selectors extends BaseSelectors {
       val label = ".govuk-label--m"
    }

    def applyView(form: Form[_]): HtmlFormat.Appendable = makingALateAppealPage.apply(form)

    val formProvider = MakingALateAppealForm.makingALateAppealForm
    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.label -> label,
      Selectors.hintText -> hintText,
      Selectors.button -> continueBtn
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
