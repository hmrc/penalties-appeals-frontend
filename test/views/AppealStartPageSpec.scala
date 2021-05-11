/*
 * Copyright 2021 HM Revenue & Customs
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
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealStartPage
import messages.AppealStartMessages._

class AppealStartPageSpec extends SpecBase with ViewBehaviours {
  "AppealStartPage" should {
    val appealStartPage: AppealStartPage = injector.instanceOf[AppealStartPage]
    object Selectors extends BaseSelectors

    def applyView(): HtmlFormat.Appendable = appealStartPage.apply()

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(2) -> p1,
      Selectors.listIndexWithElementIndex(3, 1) -> li1,
      Selectors.listIndexWithElementIndex(3, 2) -> li2,
      Selectors.pElementIndex(4) -> p2,
      Selectors.pElementIndex(5) -> p3,
      Selectors.h2 -> h2,
      Selectors.pElementIndex(7) -> p4,
      Selectors.listIndexWithElementIndex(8, 1) -> li3,
      Selectors.listIndexWithElementIndex(8, 2) -> li4,
      Selectors.pElementIndex(9) -> p5,
      Selectors.pElementIndex(10) -> p6,
      Selectors.button -> button
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
