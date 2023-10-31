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

package views.errors

import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.errors.TechnicalErrorPage
import messages.IncompleteSessionDataMessages._

class TechnicalErrorPageSpec extends SpecBase with ViewBehaviours {
  val technicalErrorPage: TechnicalErrorPage = injector.instanceOf[TechnicalErrorPage]

  object Selectors extends BaseSelectors {
    val link = "#penalties-link"
  }

  def applyView(): Html = technicalErrorPage()(userRequestWithCorrectKeys, implicitly, implicitly)

  implicit val doc: Document = asDocument(applyView())

  object TechnicalErrorMessages {
    val technicalLink = "Return to VAT penalties and appeals"
  }

  "TechnicalErrorPage" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.pElementIndex(2) -> p1,
      Selectors.link -> TechnicalErrorMessages.technicalLink
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
