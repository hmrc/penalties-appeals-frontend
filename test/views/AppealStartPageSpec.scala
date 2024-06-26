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
import messages.AppealStartMessages._
import models.NormalMode
import models.pages.{AppealStartPage, PageMode}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealStartPage

class AppealStartPageSpec extends SpecBase with ViewBehaviours {
  "AppealStartPage" when {
    val appealStartPage: AppealStartPage = injector.instanceOf[AppealStartPage]
    object Selectors extends BaseSelectors {
      override val h2 = "h2:nth-of-type(2)"
    }

    s"it has been less than ${appConfig.daysRequiredForLateAppeal} days since the due date" must {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, showWebChatLink = false,
      pageMode = PageMode(AppealStartPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    s"it has been less than ${appConfig.daysRequiredForLateAppeal} days since the due date and show web chat link" must {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, showWebChatLink = true,
        pageMode = PageMode(AppealStartPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button,
        Selectors.webChatLink -> webChatLink

      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    s"it has been more than ${appConfig.daysRequiredForLateAppeal} days since the due date" must {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, showWebChatLink = false, pageMode = PageMode(AppealStartPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.listIndexWithElementIndex(9, 3) -> li5,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    s"it has been more than ${appConfig.daysRequiredForLateAppeal} days since the due date and show web chat link" must {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, showWebChatLink = true, pageMode = PageMode(AppealStartPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.listIndexWithElementIndex(9, 3) -> li5,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button,
        Selectors.webChatLink -> webChatLink
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    "the appeal is for a LPP" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, showWebChatLink = false, pageMode = PageMode(AppealStartPage, NormalMode))(
        userRequestLPPWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3Lpp,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.listIndexWithElementIndex(9, 3) -> li5,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    "the appeal is for a LPP and show web chat link" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, showWebChatLink = true, pageMode = PageMode(AppealStartPage, NormalMode))(
        userRequestLPPWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(3) -> p1,
        Selectors.listIndexWithElementIndex(4, 1) -> li1,
        Selectors.listIndexWithElementIndex(4, 2) -> li2,
        Selectors.pElementIndex(5) -> p2,
        Selectors.pElementIndex(6) -> p3,
        Selectors.h2 -> h2,
        Selectors.pElementIndex(8) -> p4,
        Selectors.listIndexWithElementIndex(9, 1) -> li3Lpp,
        Selectors.listIndexWithElementIndex(9, 2) -> li4,
        Selectors.listIndexWithElementIndex(9, 3) -> li5,
        Selectors.externalGuidanceLink -> externalGuidanceLink,
        Selectors.pElementIndex(11) -> p5,
        Selectors.pElementIndex(12) -> p6,
        Selectors.button -> button,
        Selectors.webChatLink -> webChatLink
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "https://www.gov.uk/tax-appeals/reasonable-excuses"
      }
    }

    "the appeal is triggered by an agent and is not a LPP - have a link to the correct page" in {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, showWebChatLink = false, pageMode = PageMode(AppealStartPage, NormalMode))(
        agentUserLSP, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      doc.select("#main-content a").get(1).attr("href") shouldBe "/penalties-appeals/who-planned-to-submit-vat-return"
    }

    "the appeal does not match the special cases - have a link to the correct page" in {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, showWebChatLink = false, pageMode = PageMode(AppealStartPage, NormalMode))(
       userRequestLPPWithCorrectKeys, implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      doc.select("#main-content a").get(1).attr("href") shouldBe "/penalties-appeals/reason-for-missing-deadline"
    }
  }
}
