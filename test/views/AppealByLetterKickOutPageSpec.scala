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
import config.featureSwitches.ShowFindOutHowToAppealJourney
import messages.AppealByLetterKickOutMessages._
import models.pages.{AppealByLetterKickOutPage, PageMode}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.obligation.AppealByLetterKickOutPage

class AppealByLetterKickOutPageSpec extends SpecBase with ViewBehaviours {
  "AppealByLetterKickOutPage" should {
    val appealByLetter = injector.instanceOf[AppealByLetterKickOutPage]

    def applyView(implicit userRequest: UserRequest[_]): HtmlFormat.Appendable = {
      appealByLetter.apply(PageMode(AppealByLetterKickOutPage, NormalMode))(userRequest, messages, mockAppConfig)
    }

    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(3)"
      val p2 = "#main-content > div > div > p:nth-child(5)"
      val bullet: (Int, Int) => String = (section: Int, number: Int) => s"#main-content > div > div > ul:nth-child($section) > li:nth-child($number)"
      val inset: Int => String = (number: Int) => s"#main-content > div > div > div > p:nth-child($number)"
      val link = "p > .govuk-link"

      val addressP: Int => String = (number: Int) => s"#address > p:nth-child($number)"
    }

    "when the 'ShowFindOutHowToAppealJourney' feature is enabled" should {
      when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(true)
      "when a VAT trader is on the page" must {
        implicit val doc: Document = asDocument(applyView(userRequest = vatTraderUserLPP))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> heading,
          Selectors.p1 -> p1,
          Selectors.bullet(4, 1) -> bullet1,
          Selectors.bullet(4, 2) -> bullet2LSP,
          Selectors.bullet(4, 3) -> bullet3LSP,
          Selectors.addressP(1) -> address1,
          Selectors.addressP(2) -> address2,
          Selectors.addressP(3) -> address3,
          Selectors.addressP(4) -> address4,
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
          Selectors.bullet(4, 1) -> bullet1,
          Selectors.bullet(4, 2) -> bullet2LSP,
          Selectors.bullet(4, 3) -> bullet3LSP,
          Selectors.addressP(1) -> address1,
          Selectors.addressP(2) -> address2,
          Selectors.addressP(3) -> address3,
          Selectors.addressP(4) -> address4,
          Selectors.link -> agentLink
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }

    "when the 'ShowFindOutHowToAppealJourney' feature is disabled" should {
      when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(false)
      "when a VAT trader is on the page" must {
        implicit val doc: Document = asDocument(applyView(userRequest = vatTraderUserLPP))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> heading,
          Selectors.p1 -> p1,
          Selectors.bullet(4, 1) -> bullet1,
          Selectors.bullet(4, 2) -> bullet2,
          Selectors.bullet(4, 3) -> bullet3,
          Selectors.p2 -> p2,
          Selectors.bullet(6, 1) -> bullet4,
          Selectors.bullet(6, 2) -> bullet5,
          Selectors.inset(1) -> address1,
          Selectors.inset(2) -> address2,
          Selectors.inset(3) -> address3,
          Selectors.inset(4) -> address4,
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
          Selectors.bullet(4, 1) -> bullet1,
          Selectors.bullet(4, 2) -> bullet2,
          Selectors.bullet(4, 3) -> bullet3,
          Selectors.p2 -> p2,
          Selectors.bullet(6, 1) -> bullet4,
          Selectors.bullet(6, 2) -> bullet5,
          Selectors.inset(1) -> address1,
          Selectors.inset(2) -> address2,
          Selectors.inset(3) -> address3,
          Selectors.inset(4) -> address4,
          Selectors.link -> agentLink
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }
  }
}
