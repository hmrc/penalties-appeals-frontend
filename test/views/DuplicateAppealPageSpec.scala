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
import messages.DuplicateAppealMessages._
import models.UserRequest
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.errors.DuplicateAppealPage

class DuplicateAppealPageSpec extends SpecBase with ViewBehaviours {
  "DuplicateAppealPage" should {
    val duplicateAppealPage: DuplicateAppealPage = injector.instanceOf[DuplicateAppealPage]
    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val link = "#main-content > div > div > p:nth-child(3) > a"
    }

    def applyVATTraderView(userRequest: UserRequest[_] = vatTraderLSPUserRequest): HtmlFormat.Appendable =
      duplicateAppealPage.apply()(userRequest, implicitly, implicitly)

    implicit val doc: Document = asDocument(applyVATTraderView())

    def applyAgentView(userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys): HtmlFormat.Appendable =
      duplicateAppealPage.apply()(userRequest, implicitly, implicitly)

    implicit val agentDoc: Document = asDocument(applyAgentView())

    "when a VAT trader is on the page" must {
      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.p1 -> p1,
        Selectors.link -> linkTrader
      )

      behave like pageWithExpectedMessages(expectedContent)(doc)
    }

    "when an agent is on the page" must {
      val expectedContent = Seq(
        Selectors.title -> agentTitle,
        Selectors.h1 -> heading,
        Selectors.p1 -> p1,
        Selectors.link -> linkAgent
      )

      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }
  }
}
