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
import messages.AppealStartMessages._
import models.PenaltyTypeEnum
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.AppealStartPage

class AppealStartPageSpec extends SpecBase with ViewBehaviours {
  "AppealStartPage" should {
    val appealStartPage: AppealStartPage = injector.instanceOf[AppealStartPage]
    object Selectors extends BaseSelectors

    s"it has been less than ${appConfig.daysRequiredForLateAppeal} days since the due date" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, isObligationAppeal = false)

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

    s"it has been more than ${appConfig.daysRequiredForLateAppeal} days since the due date" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, isObligationAppeal = false)

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
        Selectors.listIndexWithElementIndex(8, 3) -> li5,
        Selectors.pElementIndex(9) -> p5,
        Selectors.pElementIndex(10) -> p6,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "the appeal is for a LPP" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = true, isObligationAppeal = false)(
        fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString), implicitly, implicitly)

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
        Selectors.listIndexWithElementIndex(8, 1) -> li3Lpp,
        Selectors.listIndexWithElementIndex(8, 2) -> li4,
        Selectors.listIndexWithElementIndex(8, 3) -> li5,
        Selectors.pElementIndex(9) -> p5,
        Selectors.pElementIndex(10) -> p6,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
    "the appeal is against an obligation" when {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, isObligationAppeal = true)

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
        Selectors.listIndexWithElementIndex(8, 1) -> li3Obl,
        Selectors.listIndexWithElementIndex(8, 2) -> li4Obl,
        Selectors.button -> button
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "the appeal is triggered by an agent and is not a LPP - redirect to the correct page" in {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, isObligationAppeal = false)(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        SessionKeys.agentSessionVrn -> "VRN1234"), implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      doc.select("#main-content a").attr("href") shouldBe "/penalties-appeals/who-planned-to-submit-vat-return"
    }

    "the appeal is an obligation appeal - redirect to the correct page" in {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, isObligationAppeal = true)

      implicit val doc: Document = asDocument(applyView())

      doc.select("#main-content a").attr("href") shouldBe "/penalties-appeals/honesty-declaration"
    }

    "the appeal does not match the special cases - redirect to the correct page" in {
      def applyView(): HtmlFormat.Appendable = appealStartPage.apply(isLate = false, isObligationAppeal = false)(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString), implicitly, implicitly)

      implicit val doc: Document = asDocument(applyView())

      doc.select("#main-content a").attr("href") shouldBe "/penalties-appeals/reason-for-missing-deadline"
    }

  }
}
