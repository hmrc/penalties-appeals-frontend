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
import forms.WhenDidThePersonDieForm
import messages.WhenDidThePersonDiePageMessages._
import models.NormalMode
import models.pages.{PageMode, WhenDidThePersonDiePage}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.bereavement.WhenDidThePersonDiePage

import java.time.LocalDate

class WhenDidThePersonDiePageSpec extends SpecBase with ViewBehaviours {
  val whenDidThePersonDiePage: WhenDidThePersonDiePage = injector.instanceOf[WhenDidThePersonDiePage]

  object Selectors extends BaseSelectors
  def applyView(form: Form[_]): HtmlFormat.Appendable = whenDidThePersonDiePage.apply(form,
    controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(NormalMode), pageMode = PageMode(WhenDidThePersonDiePage, NormalMode))

  val formProvider: Form[LocalDate] = WhenDidThePersonDieForm.whenDidThePersonDieForm()

  "WhenDidThePersonDiePage" should {
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
  }
}