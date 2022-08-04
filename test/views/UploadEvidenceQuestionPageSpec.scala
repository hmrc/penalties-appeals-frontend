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
import forms.upscan.UploadEvidenceQuestionForm
import messages.UploadEvidenceQuestionMessages._
import models.pages.{PageMode, UploadEvidenceQuestionPage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.UploadEvidenceQuestionPage
import viewtils.RadioOptionHelper

class UploadEvidenceQuestionPageSpec extends SpecBase with ViewBehaviours {
  "UploadEvidenceQuestionPage" should {
    val uploadEvidenceQuestionPage: UploadEvidenceQuestionPage = injector.instanceOf[UploadEvidenceQuestionPage]
    object Selectors extends BaseSelectors
    val formProvider = UploadEvidenceQuestionForm.uploadEvidenceQuestionForm
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    def applyView(form: Form[_]): HtmlFormat.Appendable = uploadEvidenceQuestionPage.apply(
      form, radioOptions, controllers.routes.OtherReasonController.onSubmitForUploadEvidenceQuestion(NormalMode),
      pageMode = PageMode(UploadEvidenceQuestionPage, NormalMode))

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.hintText -> hintText,
      Selectors.button -> continue
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}

