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
import messages.CheckYourAnswersMessages._
import models.NormalMode
import models.appeals.QuestionAnswerRow
import models.pages.{CheckYourAnswersPage, PageMode}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CheckYourAnswersPage

class CheckYourAnswersPageSpec extends SpecBase with ViewBehaviours {
  "CheckYourAnswersPage" should {
    val checkYourAnswers: CheckYourAnswersPage = injector.instanceOf[CheckYourAnswersPage]
    object Selectors extends BaseSelectors {
      val appealDetailsH2 = "#appeal-details"

      val declarationH2 = "#declaration"

      val descriptionListDtKey: Int => String = (nthChild: Int) => s"#main-content .govuk-summary-list__row:nth-child($nthChild) > dt"

      val descriptionListDdValue: Int => String = (nthChild: Int) => s"#main-content .govuk-summary-list__row:nth-child($nthChild) > .govuk-summary-list__value"

      val descriptionListDdActionGovukLink: Int => String = (nthChild: Int) =>
        s"#main-content .govuk-summary-list__row:nth-child($nthChild) > .govuk-summary-list__actions > .govuk-link"

      val descriptionListDdActionVisuallyHidden: Int => String = (nthChild: Int) =>
        s"#main-content .govuk-summary-list__row:nth-child($nthChild) > .govuk-summary-list__actions > .govuk-link > .govuk-visually-hidden"

      val declarationText = "#main-content .govuk-warning-text__text"

      val declarationWarningHiddenText = "#main-content .govuk-warning-text .govuk-visually-hidden"
    }

    def applyView(): HtmlFormat.Appendable = checkYourAnswers.apply(
      Seq(
        QuestionAnswerRow("Key", "Value", "href#"),
        QuestionAnswerRow("Key 2", "Value 2", "href2#"),
        QuestionAnswerRow("Key 3", "Value 3", "href2#")
      ),
      pageMode = PageMode(CheckYourAnswersPage, NormalMode)
    )(userRequestWithCorrectKeys, implicitly, implicitly)

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.appealDetailsH2 -> h2AppealDetails,
      Selectors.descriptionListDtKey(1) -> "Key",
      Selectors.descriptionListDdValue(1) -> "Value",
      Selectors.descriptionListDdActionGovukLink(1) -> "Change",
      Selectors.descriptionListDdActionVisuallyHidden(1) -> "answer for ’Key’",
      Selectors.descriptionListDtKey(2) -> "Key 2",
      Selectors.descriptionListDdValue(2) -> "Value 2",
      Selectors.descriptionListDdActionGovukLink(2) -> "Change",
      Selectors.descriptionListDdActionVisuallyHidden(2) -> "answer for ’Key 2’",
      Selectors.descriptionListDtKey(3) -> "Key 3",
      Selectors.descriptionListDdValue(3) -> "Value 3",
      Selectors.descriptionListDdActionGovukLink(3) -> "Change",
      Selectors.descriptionListDdActionVisuallyHidden(3) -> "answer for ’Key 3’",
      Selectors.declarationH2 -> h2Declaration,
      Selectors.declarationWarningHiddenText -> warningDeclarationHiddenText,
      Selectors.declarationText -> warningDeclaration,
      Selectors.button -> button
    )

    behave like pageWithExpectedMessages(expectedContent, useOwnText = true)

    "the accept and send button should have the 'data-prevent-double-click' attribute" in {
      doc.select(Selectors.button).attr("data-prevent-double-click") shouldBe "true"
    }

    "the accept and send button should be disabled" in {
      doc.forms().get(0).attr("data-prevent-multiple-submit") shouldBe "true"
    }

  }
}
