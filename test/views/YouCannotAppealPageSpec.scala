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
import messages.YouCannotAppealMessages._
import models.{NormalMode, UserRequest}
import models.pages.{PageMode, YouCannotAppealPage}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.obligation.YouCannotAppealPage

class YouCannotAppealPageSpec extends SpecBase with ViewBehaviours {
  "YouCannotAppealPage" should {
    val youCannotAppealPage: YouCannotAppealPage = injector.instanceOf[YouCannotAppealPage]
    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val p2 = "#main-content > div > div > p:nth-child(3)"
      val p3 = "#main-content > div > div > p:nth-child(4)"
      val vatClientLink = "#vat-client-details-link"
      val vatLink = "#vat-details-link"
      val checkWhatYouOweLink = "#check-what-you-owe-link"
    }

    def applyView(userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys): HtmlFormat.Appendable = {
      youCannotAppealPage.apply(PageMode(YouCannotAppealPage, NormalMode))(userRequest, messages, appConfig)
    }

    "when agent is on the page with LPP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLPP))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> agentLPPp1,
        Selectors.p2 -> agentLPPp2,
        Selectors.p3 -> agentp3,
        Selectors.vatClientLink -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
      }
    "when agent is on the page with Additional Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLPPAdditional))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> agentLPPp1,
        Selectors.p2 -> agentLPPp2,
        Selectors.p3 -> agentp3,
        Selectors.vatClientLink -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when agent is on the page with LSP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLSP))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> agentLSPp1,
        Selectors.p2 -> agentLSPp2,
        Selectors.p3 -> agentp3,
        Selectors.vatClientLink -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with LSP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserLSP))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> vatTraderLSPp1,
        Selectors.p2 -> vatTraderLSPp2,
        Selectors.p3 -> p3,
        Selectors.vatLink -> returnToVATAccount
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with LPP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserLPP))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> vatTraderLPPp1,
        Selectors.p2 -> vatTraderLPPp2,
        Selectors.p3 -> p3,
        Selectors.checkWhatYouOweLink -> checkWhatYouOwe
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with Additional Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserAdditional))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.p1 -> vatTraderLPPp1,
        Selectors.p2 -> vatTraderLPPp2,
        Selectors.p3 -> p3,
        Selectors.checkWhatYouOweLink -> checkWhatYouOwe
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    }
  }
