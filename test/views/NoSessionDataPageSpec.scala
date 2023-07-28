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
import messages.IncompleteSessionDataMessages._
import models.AuthRequest
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.errors.NoSessionDataPage

class NoSessionDataPageSpec extends SpecBase with ViewBehaviours {
  val noSessionDataPage: NoSessionDataPage = injector.instanceOf[NoSessionDataPage]

  object Selectors extends BaseSelectors {
    val link = "#penalties-link"
  }

  def applyView(authRequest: AuthRequest[_]): Html = noSessionDataPage()(implicitly, implicitly, authRequest)

  implicit val doc: Document = asDocument(applyView(AuthRequest("123456789")))

  "NoSessionDataPage" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.pElementIndex(2) -> p1,
      Selectors.link -> linkBackToPenaltiesTrader
    )

    behave like pageWithExpectedMessages(expectedContent)

    "have the correct link when the user is an agent" in {
      val doc: Document = asDocument(applyView(AuthRequest("123456789", arn = Some("ARN123"))))
      doc.select(Selectors.link).text() shouldBe linkBackToPenaltiesAgent
    }
  }

}
