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
import forms.upscan.RemoveFileQuestionForm
import messages.RemoveFileMessages._
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.RemoveFilePage
import viewtils.RadioOptionHelper

class RemoveFilePageSpec extends SpecBase with ViewBehaviours {
  "RemoveFilePage" should {
    val removeFilePage: RemoveFilePage = injector.instanceOf[RemoveFilePage]
    object Selectors extends BaseSelectors
    val formProvider = RemoveFileQuestionForm.form
    val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
    implicit val userRequest: UserRequest[AnyContent] = userRequestWithCorrectKeys
    def applyView(form: Form[_]): HtmlFormat.Appendable = removeFilePage.apply(
      form,
      radioOptions,
      controllers.routes.RemoveFileController.onSubmit(fileReference = "fileref1", isJsEnabled = false, mode = NormalMode),
      fileName = "file123.txt",
      backLink = "backLinkUrl"
    )

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title("file123.txt"),
      Selectors.h1 -> heading("file123.txt"),
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
