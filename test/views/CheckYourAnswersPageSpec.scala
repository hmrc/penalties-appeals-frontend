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
import messages.CheckYourAnswersMessages._
import models.NormalMode
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

      val descriptionListDdAction: Int => String = (nthChild: Int) =>
        s"#main-content .govuk-summary-list__row:nth-child($nthChild) > .govuk-summary-list__actions"

      val declarationText = s"#main-content > div > div > p"
    }

    def applyView(): HtmlFormat.Appendable = checkYourAnswers.apply(
      Seq(
        ("Key", "Value", "href#"),
        ("Key 2", "Value 2", "href2#"),
        ("Key 3", "Value 3", "href2#")
      ),
      pageMode = PageMode(CheckYourAnswersPage, NormalMode)
    )

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.appealDetailsH2 -> h2AppealDetails,
      Selectors.descriptionListDtKey(1) -> "Key",
      Selectors.descriptionListDdValue(1) -> "Value",
      Selectors.descriptionListDdAction(1) -> "Change",
      Selectors.descriptionListDtKey(2) -> "Key 2",
      Selectors.descriptionListDdValue(2) -> "Value 2",
      Selectors.descriptionListDdAction(2) -> "Change",
      Selectors.descriptionListDtKey(3) -> "Key 3",
      Selectors.descriptionListDdValue(3) -> "Value 3",
      Selectors.descriptionListDdAction(3) -> "Change",
      Selectors.declarationH2 -> h2Declaration,
      Selectors.declarationText -> pDeclaration,
      Selectors.button -> button
    )

    behave like pageWithExpectedMessages(expectedContent)

  }
}
