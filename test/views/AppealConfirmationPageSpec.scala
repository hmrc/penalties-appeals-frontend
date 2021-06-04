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
    }

    def applyView(penaltyTypeMsgKey: String,
                  periodStart: String,
                  periodEnd: String): HtmlFormat.Appendable = appealConfirmationPage.apply(penaltyTypeMsgKey, periodStart, periodEnd)

    implicit val lateSubmissionPenaltyDoc: Document = asDocument(applyView("penaltyType.lateSubmission", "1 July 2023", "31 July 2023"))

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

    behave like pageWithExpectedMessages(expectedContent)

    "the back link should not be present" in {
      lateSubmissionPenaltyDoc.select("#back-link").text().isEmpty shouldBe true
    }

    "the penalty information should not be visible" in {
      lateSubmissionPenaltyDoc.select("#penalty-information").text().isEmpty shouldBe true
    }

    "the panel body should contain late payment penalty when there is such" in {
      implicit val latePaymentPenaltyDoc: Document = asDocument(applyView("penaltyType.latePayment", "1 July 2023", "31 July 2023"))
      latePaymentPenaltyDoc.select(Selectors.penaltyType).text() shouldBe headingPanelBodyLPP
    }
  }
}
