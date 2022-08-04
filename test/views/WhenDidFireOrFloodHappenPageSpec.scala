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
import forms.WhenDidFireOrFloodHappenForm
import messages.WhenDidFireOrFloodHappenMessages._
import models.pages.{PageMode, WhenDidFireOrFloodHappenPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.fireOrFlood.WhenDidFireOrFloodHappenPage

import java.time.LocalDate

class WhenDidFireOrFloodHappenPageSpec extends SpecBase with ViewBehaviours{

  val fireOrFloodPage: WhenDidFireOrFloodHappenPage = injector.instanceOf[WhenDidFireOrFloodHappenPage]
  implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

  object Selectors extends BaseSelectors

  def applyView(form: Form[_]): HtmlFormat.Appendable = fireOrFloodPage.apply(form,
  controllers.routes.FireOrFloodReasonController.onSubmit(NormalMode), pageMode = PageMode(WhenDidFireOrFloodHappenPage, NormalMode))

  val formProvider: Form[LocalDate] = WhenDidFireOrFloodHappenForm.whenFireOrFloodHappenedForm()

  "WhenDidFireOrFloodHappenPage" should {

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
