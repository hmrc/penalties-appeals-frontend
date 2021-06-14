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
import forms.HonestyDeclarationForm.honestyDeclarationForm
import messages.HonestyDeclarationMessages._
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.HonestyDeclarationPage

class HonestyDeclarationPageSpec extends SpecBase with ViewBehaviours {
  "HonestyDeclarationPage" should {
    val appealStartPage: HonestyDeclarationPage = injector.instanceOf[HonestyDeclarationPage]
    object Selectors extends BaseSelectors

    def applyView(form: Form[_], reasonText: String, date: String, extraBullets: Seq[String] = Seq.empty): HtmlFormat.Appendable = appealStartPage.apply(form, reasonText, date, extraBullets)

    implicit val doc: Document = asDocument(applyView(honestyDeclarationForm, "of reason", "1 January 2022"))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(2) -> p1,
      Selectors.listIndexWithElementIndex(3, 1) -> li1("of reason", "1 January 2022"),
      Selectors.listIndexWithElementIndex(3, 2) -> li2,
      Selectors.listIndexWithElementIndex(3, 3) -> li3,
      Selectors.button -> acceptAndContinueButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "display the correct variation" when {
      "the option selected is 'crime'" must {
        implicit val doc: Document = asDocument(applyView(honestyDeclarationForm, messages("honestyDeclaration.crime"), "1 January 2022"))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.crime"), "1 January 2022")
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "the option selected is 'loss of staff'" must {
        implicit val doc: Document = asDocument(applyView(honestyDeclarationForm, messages("honestyDeclaration.lossOfStaff"),
          "1 January 2022",
          Seq("honestyDeclaration.li.extra.lossOfStaff")))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.lossOfStaff"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> extraLiForLossOfStaff
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }
}
