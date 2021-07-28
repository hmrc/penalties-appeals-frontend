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
import messages.AppealConfirmationMessages._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealConfirmationPage

class AppealConfirmationPageSpec extends SpecBase with ViewBehaviours {
  "AppealConfirmationPage" should {
    val appealConfirmationPage: AppealConfirmationPage = injector.instanceOf[AppealConfirmationPage]
    object Selectors extends BaseSelectors {
      val whatHappensNextPTag = (index: Int) => s"#main-content > div > div > p:nth-child($index)"

      val penaltyType = ".govuk-panel__body"

      val penaltiesLink = "#penalties-link"

      val vatAccountLink = "#view-vat-account-link"

      val feedbackLink = "#feedback-link"

      val obligationExtraParagraph = "#main-content p:nth-child(4)"
    }

    def applyVATTraderView(penaltyTypeMsgKey: String,
                           periodStart: String,
                           periodEnd: String,
                  isObligationAppeal: Boolean = false): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd, isObligationAppeal)(fakeRequest, messages, appConfig, vatTraderUser)

    implicit val vatTraderLateSubmissionPenaltyDoc: Document = asDocument(applyVATTraderView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023"))

    def applyAgentView(penaltyTypeMsgKey: String,
                       periodStart: String,
                       periodEnd: String): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd)(agentRequest, messages, appConfig, agentUserSessionKeys)

    implicit val agentDoc: Document = asDocument(applyAgentView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023"))


    "when agent is on page " must {
      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLSP,
        Selectors.whatHappensNextPTag(3) -> p1,
        Selectors.whatHappensNextPTag(4) -> p2,
        Selectors.whatHappensNextPTag(5) -> p3,
        Selectors.whatHappensNextPTag(6) -> p4,
        Selectors.penaltiesLink -> returnToPenaltiesAgentText,
        Selectors.vatAccountLink -> goToVatVCAgentText,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(agentDoc)
    }

    "when VAT trader is on page" must {

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> headingPanelH1,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLSP,
        Selectors.whatHappensNextPTag(3) -> p1,
        Selectors.whatHappensNextPTag(4) -> p2,
        Selectors.whatHappensNextPTag(5) -> p3,
        Selectors.whatHappensNextPTag(6) -> p4,
        Selectors.penaltiesLink -> returnToPenalties,
        Selectors.vatAccountLink -> goToVatVC,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(vatTraderLateSubmissionPenaltyDoc)

      "the back link should not be present" in {
        vatTraderLateSubmissionPenaltyDoc.select("#back-link").text().isEmpty shouldBe true
      }

      "the penalty information should not be visible" in {
        vatTraderLateSubmissionPenaltyDoc.select("#penalty-information").text().isEmpty shouldBe true
      }

      "the panel body should contain late payment penalty when there is such" in {
        implicit val latePaymentPenaltyDoc: Document = asDocument(applyVATTraderView("penaltyType.latePayment", "1 July 2023", "31 July 2023"))
        latePaymentPenaltyDoc.select(Selectors.penaltyType).text() shouldBe headingPanelBodyLPP
      }
    }

    "the extra paragraph should be visible when the appeal is against the obligation" in {
      implicit val appealAgainstObligationDoc: Document = asDocument(applyVATTraderView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023", isObligationAppeal = true))
      appealAgainstObligationDoc.select(Selectors.obligationExtraParagraph).text() shouldBe obligationParagraph
    }
  }
}
