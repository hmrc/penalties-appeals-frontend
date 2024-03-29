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
import messages.ViewAppealDetailsMessages._
import models.appeals.QuestionAnswerRow
import org.jsoup.nodes.Document
import views.behaviours.ViewBehaviours
import views.html.ViewAppealDetailsPage

class ViewAppealDetailsPageSpec extends SpecBase with ViewBehaviours {

  "ViewAppealDetailsPage" should {
    val viewAppealDetailsPage = injector.instanceOf[ViewAppealDetailsPage]
    object Selectors extends BaseSelectors {
      val descriptionListDtKey: Int => String = (nthChild: Int) => s"#main-content .govuk-summary-list__row:nth-child($nthChild) > dt"

      val descriptionListDdValue: Int => String = (nthChild: Int) => s"#main-content .govuk-summary-list__row:nth-child($nthChild) > .govuk-summary-list__value"

      val printButton: String = "#print-button"
    }

    def applyView() = viewAppealDetailsPage.apply(
      Seq(
        QuestionAnswerRow("Key 1", "Value 1", ""),
        QuestionAnswerRow("Key 2", "Value 2", ""),
        QuestionAnswerRow("Key 3", "Value 3", "")
      )
    )(implicitly, implicitly, userRequestWithCorrectKeys)

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.descriptionListDtKey(1) -> "Key 1",
      Selectors.descriptionListDdValue(1) -> "Value 1",
      Selectors.descriptionListDtKey(2) -> "Key 2",
      Selectors.descriptionListDdValue(2) -> "Value 2",
      Selectors.descriptionListDtKey(3) -> "Key 3",
      Selectors.descriptionListDdValue(3) -> "Value 3",
      Selectors.insetText -> printInsetText,
      Selectors.printButton -> printButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }

}
