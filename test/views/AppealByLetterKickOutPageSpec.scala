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
import models.pages.{AppealByLetterKickOutPage, PageMode}
import models.{NormalMode, UserRequest}
import views.behaviours.ViewBehaviours
import views.html.obligation.AppealByLetterKickOutPage
import messages.AppealByLetterKickOutMessages._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat

class AppealByLetterKickOutPageSpec extends SpecBase with ViewBehaviours {
  "AppealByLetterKickOutPage" should {
    val appealByLetter = injector.instanceOf[AppealByLetterKickOutPage]
    def applyView(implicit userRequest: UserRequest[_] = userRequestWithCorrectKeys): HtmlFormat.Appendable = {
      appealByLetter.apply(PageMode(AppealByLetterKickOutPage, NormalMode))(userRequest, messages, appConfig)
    }

    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val p2 = "#main-content > div > div > p:nth-child(4)"
      val bullet: (Int, Int) => String = (section: Int, number: Int) => s"#main-content > div > div > ul:nth-child($section) > li:nth-child($number)"
      val inset: Int => String = (number: Int) => s"#main-content > div > div > div > p:nth-child($number)"
      val link = "p > .govuk-link"
    }

    "when a VAT trader is on the page" must {
      implicit val doc: Document = asDocument(applyView(userRequest = vatTraderUserLPP))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.p1 -> p1,
        Selectors.bullet(3, 1) -> bullet1,
        Selectors.bullet(3, 2) -> bullet2,
        Selectors.bullet(3, 3) -> bullet3,
        Selectors.p2 -> p2,
        Selectors.bullet(5, 1) -> bullet4,
        Selectors.bullet(5, 2) -> bullet5,
        Selectors.inset(1) -> inset1,
        Selectors.inset(2) -> inset2,
        Selectors.inset(3) -> inset3,
        Selectors.inset(4) -> inset4,
        Selectors.link -> link
      )

      behave like pageWithExpectedMessages(expectedContent)(doc)
    }

    "when a agent is on the page" must {
      implicit val doc: Document = asDocument(applyView(userRequest = agentUserLPP))

      val expectedContent = Seq(
        Selectors.title -> titleAgent,
        Selectors.h1 -> heading,
        Selectors.p1 -> p1,
        Selectors.bullet(3, 1) -> bullet1,
        Selectors.bullet(3, 2) -> bullet2,
        Selectors.bullet(3, 3) -> bullet3,
        Selectors.p2 -> p2,
        Selectors.bullet(5, 1) -> bullet4,
        Selectors.bullet(5, 2) -> bullet5,
        Selectors.inset(1) -> inset1,
        Selectors.inset(2) -> inset2,
        Selectors.inset(3) -> inset3,
        Selectors.inset(4) -> inset4,
        Selectors.link -> agentLink
      )

      behave like pageWithExpectedMessages(expectedContent)(doc)
    }
  }
}
