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
import forms.WhenDidHealthIssueHappenForm
import messages.WhenDidHealthReasonHappenMessages._
import models.NormalMode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.health.WhenDidHealthReasonHappenPage

class WhenDidHealthReasonHappenPageSpec extends SpecBase with ViewBehaviours {

  val whenHealthReasonHappenedPage: WhenDidHealthReasonHappenPage = injector.instanceOf[WhenDidHealthReasonHappenPage]

  object Selectors extends BaseSelectors

  def applyView(form: Form[_]): HtmlFormat.Appendable = whenHealthReasonHappenedPage.apply(form,
    controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(NormalMode))

  val formProvider = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()

  "WhenDidHealthReasonHappenPage" should {
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
