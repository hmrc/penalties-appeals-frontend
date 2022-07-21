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
import forms.MakingALateAppealForm
import messages.MakingALateAppealMessages._
import models.NormalMode
import models.pages.{MakingALateAppealPage, PageMode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.MakingALateAppealPage

class MakingALateAppealPageSpec extends SpecBase with ViewBehaviours {
  "MakingALateAppealPage" should {
    val makingALateAppealPage: MakingALateAppealPage = injector.instanceOf[MakingALateAppealPage]
    object Selectors extends BaseSelectors
    "when a single penalty is appealed" when {
      def applyView(form: Form[_]): HtmlFormat.Appendable = makingALateAppealPage.apply(form, pageMode = PageMode(MakingALateAppealPage, NormalMode))

      val formProvider = MakingALateAppealForm.makingALateAppealForm()
      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.hintText -> hintText,
        Selectors.button -> continueBtn
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "when multiple penalties are being appealed" when {
      def applyView(form: Form[_]): HtmlFormat.Appendable = makingALateAppealPage.apply(form,
        pageMode = PageMode(MakingALateAppealPage, NormalMode))(fakeRequestWithCorrectKeys.withSession(SessionKeys.doYouWantToAppealBothPenalties -> "yes"), messages, appConfig)

      val formProvider = MakingALateAppealForm.makingALateAppealForm()
      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> titleMulti,
        Selectors.h1 -> headingMulti,
        Selectors.hintText -> hintText,
        Selectors.button -> continueBtn
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "when only one penalty of the period is being appealed" when {
      def applyView(form: Form[_]): HtmlFormat.Appendable = makingALateAppealPage.apply(form,
        pageMode = PageMode(MakingALateAppealPage, NormalMode))(fakeRequestWithCorrectKeys.withSession(SessionKeys.doYouWantToAppealBothPenalties -> "no"), messages, appConfig)

      val formProvider = MakingALateAppealForm.makingALateAppealForm()
      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> titleFirst,
        Selectors.h1 -> headingFirst,
        Selectors.hintText -> hintText,
        Selectors.button -> continueBtn
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}
