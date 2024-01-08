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
import messages.findOutHowToAppeal.HowToAppealMessages._
import models.NormalMode
import models.pages.{HowToAppealPage, PageMode}
import org.jsoup.nodes.Document
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.HowToAppealPage

class HowToAppealPageSpec extends SpecBase with ViewBehaviours {

  val page: HowToAppealPage = injector.instanceOf[HowToAppealPage]

  object Selectors extends BaseSelectors {
    val p1 = "#main-content > div > div > p:nth-child(4)"
    val heading2_1 = "#main-content > div.govuk-grid-row > div > h2:nth-child(3)"
    val li1 = "#main-content > div.govuk-grid-row > div > ul > li:nth-child(1)"
    val li2 = "#main-content > div.govuk-grid-row > div > ul > li:nth-child(2)"
    val heading2_2 = "#main-content > div.govuk-grid-row > div > h2:nth-child(6)"
    val p2 = "#main-content > div.govuk-grid-row > div > p:nth-child(7)"
    val address = "#main-content > div.govuk-grid-row > div > p:nth-child(8)"
    val p3 = "#main-content > div.govuk-grid-row > div > p:nth-child(9)"
    val link1 = "#tax-tribunal-link"
    val link2 = "#penalties-link"
  }

  implicit val doc: Document = asDocument(page("123.45", PageMode(HowToAppealPage, NormalMode))(implicitly, implicitly, userRequestWithCorrectKeys))

  "HowToAppeal" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.heading2_1 -> heading2,
      Selectors.p1 -> p1,
      Selectors.li1 -> li1,
      Selectors.li2 -> li2,
      Selectors.heading2_2 -> heading3,
      Selectors.p2 -> p2,
      Selectors.address -> address,
      Selectors.p3 -> p3,
      Selectors.link1 -> link1,
      Selectors.link2 -> link2
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
