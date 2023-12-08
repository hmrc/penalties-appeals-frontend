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
import messages.findOutHowToAppeal.WaitForPaymentToClearMessages._
import models.pages.{PageMode, WaitForPaymentToClearPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.WaitForPaymentToClearPage

class WaitForPaymentToClearPageSpec extends SpecBase with ViewBehaviours {
  "WaitForPaymentToClearPage" should {
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    val page: WaitForPaymentToClearPage = injector.instanceOf[WaitForPaymentToClearPage]
    object Selectors extends BaseSelectors

    def applyView(): HtmlFormat.Appendable = page.apply(pageMode = PageMode(WaitForPaymentToClearPage, NormalMode))

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(3) -> p1,
      Selectors.pElementIndex(4) -> p2,
      Selectors.pElementIndex(5) -> link,
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
