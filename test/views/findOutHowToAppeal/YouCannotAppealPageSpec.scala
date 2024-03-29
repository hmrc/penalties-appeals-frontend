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
import messages.YouCannotAppealMessages._
import models.pages.{PageMode, YouCannotAppealPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.YouCannotAppealPage
import viewtils.YouCannotAppealHelper

class YouCannotAppealPageSpec extends SpecBase with ViewBehaviours {
  "YouCannotAppealPage" should {
    val youCannotAppealPage: YouCannotAppealPage = injector.instanceOf[YouCannotAppealPage]
    val youCannotHelper = injector.instanceOf[YouCannotAppealHelper]
    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(3)"
      val p2 = "#main-content > div > div > p:nth-child(4)"
      val p3 = "#main-content > div > div > p:nth-child(5)"
      val p4 = "#main-content > div > div > p:nth-child(6)"
      val link = "p > .govuk-link"
    }

    def applyView(implicit userRequest: UserRequest[_]): HtmlFormat.Appendable = {
      youCannotAppealPage.apply(youCannotHelper.getContent, youCannotHelper.getHeaderAndTitle,
        PageMode(YouCannotAppealPage, NormalMode))(userRequest, messages, appConfig)
    }

    "when agent is on the page with LSP Appeal" must {
      implicit val agentDoc: Document = asDocument(applyView(userRequest = agentUserLSP))

      val expectedContent = Seq(
        Selectors.title -> (titleHeaderLSPAgent + agentTitleAppend),
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
  }
}
