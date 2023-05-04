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
import forms.WhenDidPersonLeaveTheBusinessForm
import messages.WhenDidThePersonLeaveBusinessMessages._
import models.pages.{PageMode, WhenDidPersonLeaveTheBusinessPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.lossOfStaff.WhenDidThePersonLeaveBusinessPage

import java.time.LocalDate

class WhenDidThePersonLeaveBusinessPageSpec extends SpecBase with ViewBehaviours {
  "WhenDidThePersonLeaveBusinessPage" should {
    val whenDidThePersonLeaveBusinessPage: WhenDidThePersonLeaveBusinessPage = injector.instanceOf[WhenDidThePersonLeaveBusinessPage]
    val whenDidThePersonLeaveBusinessForm: Form[LocalDate] = WhenDidPersonLeaveTheBusinessForm.whenDidPersonLeaveTheBusinessForm()
    val postAction: Call = controllers.routes.LossOfStaffReasonController.onPageLoad(NormalMode)
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    object Selectors extends BaseSelectors {
      val p = "#main-content p"

      val visuallyHiddenHeading = ".govuk-fieldset__legend.govuk-visually-hidden"
    }

    def applyView(form: Form[_]): HtmlFormat.Appendable = whenDidThePersonLeaveBusinessPage.apply(form, postAction, pageMode = PageMode(WhenDidPersonLeaveTheBusinessPage, NormalMode))

    implicit val doc: Document = asDocument(applyView(whenDidThePersonLeaveBusinessForm))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> heading,
      Selectors.p -> descriptiveText,
      Selectors.hintText -> hintText,
      Selectors.visuallyHiddenHeading -> heading,
      Selectors.dateEntry(1) -> dayEntry,
      Selectors.dateEntry(2) -> monthEntry,
      Selectors.dateEntry(3) -> yearEntry,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
