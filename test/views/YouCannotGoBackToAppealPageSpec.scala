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
import messages.YouCannotGoBackToAppealMessages._
import models.pages.PageMode
import models.{AuthRequest, NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.errors.YouCannotGoBackToAppealPage

class YouCannotGoBackToAppealPageSpec extends SpecBase with ViewBehaviours {
  "YouCannotGoBackToAppealPage" should {
    val youCannotGoBackToAppealPage: YouCannotGoBackToAppealPage = injector.instanceOf[YouCannotGoBackToAppealPage]

    object Selectors extends BaseSelectors {
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val p2 = "#main-content > div > div > p:nth-child(3)"
      val bullet1 = "#return-to-vat-penalties"
      val bullet2 = "#return-to-vat-account"
      val feedbackLink = "#page-feedback-link"
    }

    def applyView(implicit request: AuthRequest[_]): HtmlFormat.Appendable = {
      youCannotGoBackToAppealPage.apply()(request, messages, appConfig)
    }

    "when trader is on the page" must {
      val traderRequest: AuthRequest[AnyContent] = new AuthRequest[AnyContent]("12346789")
      implicit val doc: Document = asDocument(applyView(request = traderRequest))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.p1 -> p1,
        Selectors.p2 -> p2,
        Selectors.bullet1 -> bullet1,
        Selectors.bullet2 -> bullet2,
        Selectors.feedbackLink -> feedbackLink
      )
      behave like pageWithExpectedMessages(expectedContent)

      "have the correct links" in {
        doc.select(Selectors.bullet1).attr("href") shouldBe appConfig.penaltiesFrontendUrl
        doc.select(Selectors.bullet2).attr("href") shouldBe appConfig.vatOverviewUrl
        doc.select(Selectors.feedbackLink).attr("href") shouldBe appConfig.betaFeedbackUrl(controllers.routes.YouCannotGoBackToAppealController.onPageLoad().url)
      }
    }

    "when agent is on the page" must {
      val agentRequest: AuthRequest[AnyContent] = new AuthRequest[AnyContent]("12346789", arn = Some("BARN123456789"))
      implicit val doc: Document = asDocument(applyView(request = agentRequest))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.p1 -> p1,
        Selectors.p2 -> p2,
        Selectors.bullet1 -> bullet1Agent,
        Selectors.bullet2 -> bullet2Agent,
        Selectors.feedbackLink -> feedbackLink
      )
      behave like pageWithExpectedMessages(expectedContent)

      "have the correct links" in {
        doc.select(Selectors.bullet1).attr("href") shouldBe appConfig.penaltiesFrontendUrl
        doc.select(Selectors.bullet2).attr("href") shouldBe appConfig.vatOverviewUrl
        doc.select(Selectors.feedbackLink).attr("href") shouldBe appConfig.betaFeedbackUrl(controllers.routes.YouCannotGoBackToAppealController.onPageLoad().url)
      }
    }
  }
}
