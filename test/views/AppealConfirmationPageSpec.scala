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
import messages.AppealConfirmationMessages._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealConfirmationPage

class AppealConfirmationPageSpec extends SpecBase with ViewBehaviours {
  "AppealConfirmationPage" should {
    val appealConfirmationPage: AppealConfirmationPage = injector.instanceOf[AppealConfirmationPage]
    object Selectors extends BaseSelectors {
      val penaltyType = ".govuk-panel__body"

      val penaltiesLink = "#penalties-link"

      val vatAccountLink = "#view-vat-account-link"

      val feedbackLink = "#feedback-link"

      val paragraph: Int => String = (index: Int) => s"#main-content > div > div > p:nth-child($index)"

      val obligationExtraParagraph = "#main-content p:nth-child(6)"
    }

    def applyVATTraderView(penaltyTypeMsgKey: String,
                           periodStart: String,
                           periodEnd: String,
                  isObligationAppeal: Boolean = false): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd, isObligationAppeal)(vatTraderLSPUserRequest, messages, appConfig)

    implicit val vatTraderLateSubmissionPenaltyDoc: Document = asDocument(applyVATTraderView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023"))

    def applyLPP1VATTraderView(penaltyTypeMsgKey: String,
                           periodStart: String,
                           periodEnd: String,
                           isObligationAppeal: Boolean = false): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd, isObligationAppeal)(vatTraderLPPUserRequest, messages, appConfig)

    implicit val vatTraderLPP1Doc: Document = asDocument(applyLPP1VATTraderView("penaltyType.latePayment", "1 July 2023", "31 July 2023"))

    def applyLPP2VATTraderViewBothPenalties(penaltyTypeMsgKey: String,
                          periodStart: String,
                          periodEnd: String,
                          isObligationAppeal: Boolean = false): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd, isObligationAppeal)(vatTraderLPP2UserRequest, messages, appConfig)

    implicit val vatTraderLPP2Doc: Document = asDocument(applyLPP2VATTraderViewBothPenalties("penaltyType.latePayment", "1 July 2023", "31 July 2023"))

    def applyLPP2VATAgentViewBothPenalties(penaltyTypeMsgKey: String,
                             periodStart: String,
                             periodEnd: String,
                             isObligationAppeal: Boolean = false): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd, isObligationAppeal)(vatAgentLPP2UserRequest, messages, appConfig)

    implicit val vatAgentLPP2Doc: Document = asDocument(applyLPP2VATAgentViewBothPenalties("penaltyType.latePayment", "1 July 2023", "31 July 2023"))

    def applyAgentView(penaltyTypeMsgKey: String,
                       periodStart: String,
                       periodEnd: String): HtmlFormat.Appendable = appealConfirmationPage.apply(
      penaltyTypeMsgKey, periodStart, periodEnd)(agentUserAgentSubmitButClientWasLateSessionKeys, messages, appConfig)

    implicit val agentDoc: Document = asDocument(applyAgentView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023"))


    "when agent is on the page " must {
      val expectedContent = Seq(
        Selectors.title -> titleSinglePenaltyAgent,
        Selectors.h1 -> headingPanelSinglePenaltyAgent,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLSP,
        Selectors.paragraph(2) -> p1,
        Selectors.paragraph(3) -> p2,
        Selectors.paragraph(5) -> whatHappensNextP1,
        Selectors.paragraph(6) -> whatHappensNextP2,
        Selectors.paragraph(7) -> whatHappensNextP3,
        Selectors.penaltiesLink -> returnToPenaltiesAgentText,
        Selectors.vatAccountLink -> goToVatVCAgentText,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(agentDoc)

      "not show the secure/email message" in {
        agentDoc.select(Selectors.paragraph(8)).text.isEmpty shouldBe true
      }
    }

    "when agent is on the page with both penalties " must {
      val expectedContent = Seq(
        Selectors.title -> titleBothPenaltiesAgent,
        Selectors.h1 -> headingPanelBothPenaltiesAgent,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLPPenalties,
        Selectors.paragraph(2) -> p1,
        Selectors.paragraph(3) -> p2,
        Selectors.paragraph(5) -> whatHappensNextP1,
        Selectors.paragraph(6) -> whatHappensNextP2,
        Selectors.paragraph(7) -> whatHappensNextP3,
        Selectors.penaltiesLink -> returnToPenaltiesAgentText,
        Selectors.vatAccountLink -> goToVatVCAgentText,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(vatAgentLPP2Doc)

      "not show the secure/email message" in {
        vatAgentLPP2Doc.select(Selectors.paragraph(8)).text.isEmpty shouldBe true
      }
    }

    "when VAT trader is on page in LSP appeal" must {

      val expectedContent = Seq(
        Selectors.title -> titleSinglePenalty,
        Selectors.h1 -> headingPanelSinglePenalty,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLSP,
        Selectors.paragraph(2) -> p1,
        Selectors.paragraph(3) -> p2,
        Selectors.paragraph(5) -> whatHappensNextP1,
        Selectors.paragraph(6) -> whatHappensNextP2,
        Selectors.paragraph(7) -> whatHappensNextP3,
        Selectors.paragraph(8) -> whatHappensNextP4,
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
    }

    "when VAT trader is on page in LPP1 appeal" must {

      val expectedContent = Seq(
        Selectors.title -> titleSinglePenalty,
        Selectors.h1 -> headingPanelSinglePenalty,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLPP,
        Selectors.paragraph(2) -> p1,
        Selectors.paragraph(3) -> p2,
        Selectors.paragraph(5) -> whatHappensNextP1,
        Selectors.paragraph(6) -> whatHappensNextP2,
        Selectors.paragraph(7) -> whatHappensNextP3,
        Selectors.paragraph(8) -> whatHappensNextP4,
        Selectors.penaltiesLink -> returnToPenalties,
        Selectors.vatAccountLink -> goToVatVC,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(vatTraderLPP1Doc)

      "the back link should not be present" in {
        vatTraderLPP1Doc.select("#back-link").text().isEmpty shouldBe true
      }

      "the penalty information should not be visible" in {
        vatTraderLPP1Doc.select("#penalty-information").text().isEmpty shouldBe true
      }
    }

    "when VAT trader is on page in LPP2 appeal" must {

      val expectedContent = Seq(
        Selectors.title -> titleBothPenalties,
        Selectors.h1 -> headingPanelBothPenalties,
        Selectors.h2 -> h2WhatHappensNext,
        Selectors.penaltyType -> headingPanelBodyLPPenalties,
        Selectors.paragraph(2) -> p1,
        Selectors.paragraph(3) -> p2,
        Selectors.paragraph(5) -> whatHappensNextP1,
        Selectors.paragraph(6) -> whatHappensNextP2,
        Selectors.paragraph(7) -> whatHappensNextP3,
        Selectors.paragraph(8) -> whatHappensNextP4,
        Selectors.penaltiesLink -> returnToPenalties,
        Selectors.vatAccountLink -> goToVatVC,
        Selectors.feedbackLink -> goToFeedback
      )

      behave like pageWithExpectedMessages(expectedContent)(vatTraderLPP2Doc)

      "the back link should not be present" in {
        vatTraderLPP2Doc.select("#back-link").text().isEmpty shouldBe true
      }

      "the penalty information should not be visible" in {
        vatTraderLPP2Doc.select("#penalty-information").text().isEmpty shouldBe true
      }
    }

    "the extra paragraph should be visible when the appeal is against the obligation" in {
      implicit val appealAgainstObligationDoc: Document = asDocument(
        applyVATTraderView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023", isObligationAppeal = true))
      appealAgainstObligationDoc.select(Selectors.obligationExtraParagraph).text() shouldBe obligationParagraph
    }
  }
}
