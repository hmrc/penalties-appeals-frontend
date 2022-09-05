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
import messages.IncompleteSessionDataMessages._
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.errors.IncompleteSessionDataPage

class IncompleteSessionDataPageSpec extends SpecBase with ViewBehaviours {
  val incompleteSessionDataPage: IncompleteSessionDataPage = injector.instanceOf[IncompleteSessionDataPage]

  object Selectors extends BaseSelectors {
    val link = "#main-content > div > div > p:nth-child(3) > a"
  }

  def applyView(): Html = incompleteSessionDataPage("123456789", isLPP = false, isAdditional = false)(implicitly, implicitly, userRequestWithCorrectKeys)

  implicit val doc: Document = asDocument(applyView())

  "IncompleteSessionDataPage" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.pElementIndex(2) -> p1,
      Selectors.link -> link
    )

    behave like pageWithExpectedMessages(expectedContent)
  }

}
