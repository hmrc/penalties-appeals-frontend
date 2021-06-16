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
import forms.{WhenDidTechnologyIssuesBeginForm, WhenDidTechnologyIssuesEndForm}
import messages.{WhenDidTechnologyIssuesBeginMessages, WhenDidTechnologyIssuesEndMessages}
import models.NormalMode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.technicalIssues.TechnologyIssuesDatePage

import java.time.LocalDate

class TechnologyIssuesDatePageSpec extends SpecBase with ViewBehaviours {
  "TechnologyIssuesDatePage" should {
    "have a begin variation" when {
      val whenDidTechnologyIssuesBeginPage: TechnologyIssuesDatePage = injector.instanceOf[TechnologyIssuesDatePage]
      object Selectors extends BaseSelectors

      def applyView(form: Form[_]): HtmlFormat.Appendable = whenDidTechnologyIssuesBeginPage.apply(form,
        controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(NormalMode), "technicalIssues.begin")

      val formProvider = WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm()

      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> WhenDidTechnologyIssuesBeginMessages.title,
        Selectors.h1 -> WhenDidTechnologyIssuesBeginMessages.heading,
        Selectors.hintText -> WhenDidTechnologyIssuesBeginMessages.hintText,
        Selectors.dateEntry(1) -> WhenDidTechnologyIssuesBeginMessages.dayEntry,
        Selectors.dateEntry(2) -> WhenDidTechnologyIssuesBeginMessages.monthEntry,
        Selectors.dateEntry(3) -> WhenDidTechnologyIssuesBeginMessages.yearEntry,
        Selectors.button -> WhenDidTechnologyIssuesBeginMessages.continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "have an end variation" when {
      val whenDidTechnologyIssuesBeginPage: TechnologyIssuesDatePage = injector.instanceOf[TechnologyIssuesDatePage]
      object Selectors extends BaseSelectors
      val sampleStartDateForFormValidation: LocalDate = LocalDate.of(2021, 12, 31)

      def applyView(form: Form[_]): HtmlFormat.Appendable = whenDidTechnologyIssuesBeginPage.apply(form,
        controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(NormalMode), "technicalIssues.end")

      val formProvider = WhenDidTechnologyIssuesEndForm.whenDidTechnologyIssuesEndForm(sampleStartDateForFormValidation)

      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> WhenDidTechnologyIssuesEndMessages.title,
        Selectors.h1 -> WhenDidTechnologyIssuesEndMessages.heading,
        Selectors.hintText -> WhenDidTechnologyIssuesEndMessages.hintText,
        Selectors.dateEntry(1) -> WhenDidTechnologyIssuesEndMessages.dayEntry,
        Selectors.dateEntry(2) -> WhenDidTechnologyIssuesEndMessages.monthEntry,
        Selectors.dateEntry(3) -> WhenDidTechnologyIssuesEndMessages.yearEntry,
        Selectors.button -> WhenDidTechnologyIssuesEndMessages.continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}
