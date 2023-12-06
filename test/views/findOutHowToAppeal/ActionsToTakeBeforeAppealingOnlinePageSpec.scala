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
import messages.findOutHowToAppeal.ActionsToTakeBeforeAppealingOnlineMessages._
import models.NormalMode
import models.pages.{ActionsToTakeBeforeAppealingOnlinePage, PageMode}
import org.jsoup.nodes.Document
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.ActionsToTakeBeforeAppealingOnlinePage

class ActionsToTakeBeforeAppealingOnlinePageSpec extends SpecBase with ViewBehaviours {
  "ActionsToTakeBeforeAppealingOnlinePage" should {

    val page: ActionsToTakeBeforeAppealingOnlinePage = injector.instanceOf[ActionsToTakeBeforeAppealingOnlinePage]

    object Selectors extends BaseSelectors {
      val detailsSummary = ".govuk-details__summary"

      def detailsP(index: Int) = s".govuk-details p:nth-child($index)"

      val returnToPenaltiesLink = "#penalties-link"

      val appealByTribunalLink = "#appeal-by-tribunal"
    }

    def applyView(isAgent: Boolean, isCA: Boolean = false): HtmlFormat.Appendable = {
      val answers = correctUserAnswers ++ Json.obj(
        SessionKeys.startDateOfPeriod -> "2024-01-01",
        SessionKeys.endDateOfPeriod -> "2024-01-31")
      val request = if(isAgent) agentFakeRequestConverter(answers) else fakeRequestConverter(answers)
      page.apply(pageMode = PageMode(ActionsToTakeBeforeAppealingOnlinePage, NormalMode), isCA)(implicitly, implicitly, request)
    }

    "when a VAT trader is on the page (LSP)" must {

      implicit val doc: Document = asDocument(applyView(isAgent = false))

      val expectedContent = Seq(
        Selectors.title -> TraderMessages.title,
        Selectors.h1 -> h1,
        Selectors.orderedListIndexWithElementIndex(1, 1) -> TraderMessages.li1("1 January 2024", "31 January 2024"),
        Selectors.orderedListIndexWithElementIndex(1, 2) -> TraderMessages.li2,
        Selectors.orderedListIndexWithElementIndex(1, 3) -> TraderMessages.li3,
        Selectors.detailsSummary -> details,
        Selectors.detailsP(1) -> TraderMessages.detailsP1,
        Selectors.detailsP(2) -> TraderMessages.detailsP2,
        Selectors.detailsP(3) -> address1,
        Selectors.detailsP(4) -> address2,
        Selectors.detailsP(5) -> address3,
        Selectors.detailsP(6) -> address4,
        Selectors.detailsP(7) -> detailsP3,
        Selectors.appealByTribunalLink -> taxTribunalLink,
        Selectors.returnToPenaltiesLink -> TraderMessages.returnLink
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "when a VAT trade is on the page (LPP)" must {

      implicit val doc = asDocument(applyView(isAgent = false, isCA = true))

      val expectedContent = Seq(
        Selectors.title -> TraderMessages.title,
        Selectors.h1 -> h1,
        Selectors.orderedListIndexWithElementIndex(1, 1) -> TraderMessages.li1("1 January 2024", "31 January 2024"),
        Selectors.orderedListIndexWithElementIndex(1, 2) -> TraderMessages.li2LPP,
        Selectors.orderedListIndexWithElementIndex(1, 3) -> TraderMessages.li3LPP,
        Selectors.orderedListIndexWithElementIndex(1, 4) -> TraderMessages.li3,
        Selectors.detailsSummary -> details,
        Selectors.detailsP(1) -> TraderMessages.detailsP1,
        Selectors.detailsP(2) -> TraderMessages.detailsP2,
        Selectors.detailsP(3) -> address1,
        Selectors.detailsP(4) -> address2,
        Selectors.detailsP(5) -> address3,
        Selectors.detailsP(6) -> address4,
        Selectors.detailsP(7) -> detailsP3,
        Selectors.appealByTribunalLink -> taxTribunalLink,
        Selectors.returnToPenaltiesLink -> TraderMessages.returnLink
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "when an agent is on the page (LSP)" must {

      implicit val doc: Document = asDocument(applyView(isAgent = true))

      val expectedContent = Seq(
        Selectors.title -> AgentMessages.title,
        Selectors.h1 -> h1,
        Selectors.orderedListIndexWithElementIndex(1, 1) -> AgentMessages.li1("1 January 2024", "31 January 2024"),
        Selectors.orderedListIndexWithElementIndex(1, 2) -> AgentMessages.li2,
        Selectors.orderedListIndexWithElementIndex(1, 3) -> AgentMessages.li3,
        Selectors.detailsSummary -> details,
        Selectors.detailsP(1) -> AgentMessages.detailsP1,
        Selectors.detailsP(2) -> AgentMessages.detailsP2,
        Selectors.detailsP(3) -> address1,
        Selectors.detailsP(4) -> address2,
        Selectors.detailsP(5) -> address3,
        Selectors.detailsP(6) -> address4,
        Selectors.detailsP(7) -> detailsP3,
        Selectors.appealByTribunalLink -> taxTribunalLink,
        Selectors.returnToPenaltiesLink -> AgentMessages.returnLink
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "when an agent is on the page (LPP)" must {

      implicit val doc: Document = asDocument(applyView(isAgent = true, isCA = true))

      val expectedContent = Seq(
        Selectors.title -> AgentMessages.title,
        Selectors.h1 -> h1,
        Selectors.orderedListIndexWithElementIndex(1, 1) -> AgentMessages.li1("1 January 2024", "31 January 2024"),
        Selectors.orderedListIndexWithElementIndex(1, 2) -> AgentMessages.li2LPP,
        Selectors.orderedListIndexWithElementIndex(1, 3) -> AgentMessages.li3LPP,
        Selectors.orderedListIndexWithElementIndex(1, 4) -> AgentMessages.li3,
        Selectors.detailsSummary -> details,
        Selectors.detailsP(1) -> AgentMessages.detailsP1,
        Selectors.detailsP(2) -> AgentMessages.detailsP2,
        Selectors.detailsP(3) -> address1,
        Selectors.detailsP(4) -> address2,
        Selectors.detailsP(5) -> address3,
        Selectors.detailsP(6) -> address4,
        Selectors.detailsP(7) -> detailsP3,
        Selectors.appealByTribunalLink -> taxTribunalLink,
        Selectors.returnToPenaltiesLink -> AgentMessages.returnLink
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}
