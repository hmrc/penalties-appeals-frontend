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
import forms.WhenDidBecomeUnableForm
import messages.WhenDidBecomeUnableMessages._
import models.{NormalMode, PenaltyTypeEnum}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.WhenDidBecomeUnablePage

class WhenDidBecomeUnablePageSpec extends SpecBase with ViewBehaviours {
  "WhenDidBecomeUnablePage" should {
    val whenDidBecomeUnablePage: WhenDidBecomeUnablePage = injector.instanceOf[WhenDidBecomeUnablePage]
    object Selectors extends BaseSelectors

    def applyView(form: Form[_], request: FakeRequest[_] = fakeRequest): HtmlFormat.Appendable = whenDidBecomeUnablePage.apply(form,
      controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(NormalMode))(request, implicitly, implicitly)

    val formProvider = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.hintText -> hintText,
      Selectors.dateEntry(1) -> dayEntry,
      Selectors.dateEntry(2) -> monthEntry,
      Selectors.dateEntry(3) -> yearEntry,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "display the LPP variation when the appeal is for a LPP" must {
      implicit val doc: Document = asDocument(applyView(formProvider, fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString)))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.hintText -> hintTextLpp,
        Selectors.dateEntry(1) -> dayEntry,
        Selectors.dateEntry(2) -> monthEntry,
        Selectors.dateEntry(3) -> yearEntry,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}
