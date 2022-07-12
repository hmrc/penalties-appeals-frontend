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
import models.pages.{PageMode, YouCannotAppealPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.obligation.YouCannotAppealPage
import viewtils.YouCannotAppealHelper

class YouCannotAppealPageSpec extends SpecBase with ViewBehaviours {
  "YouCannotAppealPage" should {
    val youCannotAppealPage: YouCannotAppealPage = injector.instanceOf[YouCannotAppealPage]
    val youCannotHelper = injector.instanceOf[YouCannotAppealHelper]
    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val p2 = "#main-content > div > div > p:nth-child(3)"
      val p3 = "#main-content > div > div > p:nth-child(4)"
      val p4 = "#main-content > div > div > p:nth-child(5)"
      val link = "p > .govuk-link"
    }

    def applyView(implicit userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys): HtmlFormat.Appendable = {
      youCannotAppealPage.apply(youCannotHelper.getContent, youCannotHelper.getHeaderAndTitle,
        PageMode(YouCannotAppealPage, NormalMode))(userRequest, messages, appConfig)
    }

    "when agent is on the page with LPP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLPP))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLPPAgent + titleAppend),
        Selectors.h1 -> titleHeaderLPPAgent,
        Selectors.p1 -> p1,
        Selectors.p2 -> agentLPPp2,
        Selectors.p3 -> agentLPPp3,
        Selectors.p4 -> agentP4,
        Selectors.link -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
      }
    "when agent is on the page with Additional Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLPPAdditional))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLPPAgent + titleAppend),
        Selectors.h1 -> titleHeaderLPPAgent,
        Selectors.p1 -> p1,
        Selectors.p2 -> agentLPPp2,
        Selectors.p3 -> agentLPPp3,
        Selectors.link -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when agent is on the page with LSP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLSP))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLSPAgent + titleAppend),
        Selectors.h1 -> titleHeaderLSPAgent,
        Selectors.p1 -> p1,
        Selectors.p2 -> agentLSPp2,
        Selectors.p3 -> agentLSPp3,
        Selectors.p4 -> agentP4,
        Selectors.link -> returnToClientVATDetails
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with LSP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserLSP))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLSP + titleAppend),
        Selectors.h1 -> titleHeaderLSP,
        Selectors.p1 -> p1,
        Selectors.p2 -> traderLSPp2,
        Selectors.p3 -> traderLSPp3,
        Selectors.p4 -> traderP4,
        Selectors.link -> returnToVATAccount
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with LPP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserLPP))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLPP + titleAppend),
        Selectors.h1 -> titleHeaderLPP,
        Selectors.p1 -> p1,
        Selectors.p2 -> traderLPPp2,
        Selectors.p3 -> traderLPPp3,
        Selectors.p4 -> traderP4,
        Selectors.link -> checkWhatYouOwe
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    "when VAT trader is on the page with Additional Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = vatTraderUserAdditional))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLPP + titleAppend),
        Selectors.h1 -> titleHeaderLPP,
        Selectors.p1 -> p1,
        Selectors.p2 -> traderLPPp2,
        Selectors.p3 -> traderLPPp3,
        Selectors.p4 -> traderP4,
        Selectors.link -> checkWhatYouOwe
      )
      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
    }
  }
