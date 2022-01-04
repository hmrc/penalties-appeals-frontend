/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.OtherPenaltiesForPeriodForm.otherPenaltiesForPeriodForm
import messages.OtherPenaltiesForPeriodMessages._
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.OtherPenaltiesForPeriodPage

class OtherPenaltiesForPeriodPageSpec extends SpecBase with ViewBehaviours {

  "OtherPenaltiesForPeriodPage" should {
    val otherPenaltiesForPeriodPage: OtherPenaltiesForPeriodPage = injector.instanceOf[OtherPenaltiesForPeriodPage]

    object Selectors extends BaseSelectors{
      val p1 = "#main-content > div > div > p:nth-child(2)"
      val p2 = "#main-content > div > div > p:nth-child(3)"
    }

    def applyView(form: Form[_])(implicit request: FakeRequest[_]): HtmlFormat.Appendable = {
      otherPenaltiesForPeriodPage(form)
    }

    implicit  val doc: Document = asDocument(applyView(otherPenaltiesForPeriodForm))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.p1 -> p1,
      Selectors.p2 -> p2,
      Selectors.button -> button
    )

    behave like pageWithExpectedMessages(expectedContent)
  }

}
