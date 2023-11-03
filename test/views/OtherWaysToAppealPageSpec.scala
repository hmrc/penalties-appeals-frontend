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
import views.behaviours.ViewBehaviours
import views.html.OtherWaysToAppealPage
import messages.OtherWaysToAppealMessages._

class OtherWaysToAppealPageSpec extends SpecBase with ViewBehaviours {

  val page: OtherWaysToAppealPage = injector.instanceOf[OtherWaysToAppealPage]

  object Selectors extends BaseSelectors {
    val p1 = "#main-content > div > div > p:nth-child(3)"
    val p2 = "#main-content > div > div > p:nth-child(4)"
    val p3 = "#main-content > div > div > p:nth-child(5)"
    val inset: Int => String = (number: Int) => s"#main-content > div > div > div > p:nth-child($number)"
    val link1 = "#tax-tribunal-link"
    val link2 = "#penalties-link"
  }

  implicit val doc = asDocument(page()(implicitly, implicitly, userRequestWithCorrectKeys))

  "OtherWaysToAppealPage" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.p1 -> p1,
      Selectors.p2 -> p2,
      Selectors.p3 -> p3,
      Selectors.link1 -> link1,
      Selectors.link2 -> link2
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
