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

package views.components.upload

import base.{BaseSelectors, SpecBase}
import forms.upscan.YouHaveUploadedFilesForm
import messages.YouHaveUploadedFilesMessages._
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.behaviours.ViewBehaviours
import views.html.components.upload.YouHaveUploadedFilesPage
import viewtils.RadioOptionHelper

class YouHaveUploadedFilesPageSpec extends SpecBase with ViewBehaviours {
  val youHaveUploadedFilesPage: YouHaveUploadedFilesPage = injector.instanceOf[YouHaveUploadedFilesPage]

  val uploadedFiles: Seq[(String, Int)] = Seq(("file1", 1), ("file2", 2), ("file3", 3))

  val expected = List(uploadListRow(1),uploadListRow(2),uploadListRow(3))

  val uploadSingleRow: Seq[SummaryListRow] = Seq(uploadListRow(1))

  val uploadMultipleRows: Seq[SummaryListRow] = Seq(uploadListRow(1),uploadListRow(2))

  val uploadMaxRow: Seq[SummaryListRow] = Seq(uploadListRow(1),uploadListRow(2),uploadListRow(3), uploadListRow(4),uploadListRow(5))

  object Selectors extends BaseSelectors {
    val label = ".govuk-fieldset__legend--m"
  }
  val formProvider = YouHaveUploadedFilesForm.youHaveUploadedForm
  val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
  "YouHaveUploadedFilesPage" should {
    "return multiple files header and title" when {
      def applyView(form: Form[_]): HtmlFormat.Appendable = youHaveUploadedFilesPage.apply(
        form, radioOptions, controllers.routes.OtherReasonController.onPageLoadForUploadComplete(),uploadMultipleRows)(userRequestWithCorrectKeys,messages,appConfig)
      implicit val doc: Document = asDocument(applyView(formProvider))

      val expectedContent = Seq(
        Selectors.title -> titleMultipleFiles,
        Selectors.h1 -> h1MultipleFiles,
        Selectors.labelForRadioButton(1) -> yesOption,
        Selectors.labelForRadioButton(2) -> noOption,
        Selectors.label -> uploadAnotherFile,
        Selectors.button -> continueButton
      )
      behave like pageWithExpectedMessages(expectedContent)
    }
  }

  "return single file header and title added" when {
    def applyView(form: Form[_]): HtmlFormat.Appendable = youHaveUploadedFilesPage.apply(
      form, radioOptions, controllers.routes.OtherReasonController.onPageLoadForUploadComplete(),uploadSingleRow)(userRequestWithCorrectKeys,messages,appConfig)
    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> titleSingleFile,
      Selectors.h1 -> h1SingleFile,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.label -> uploadAnotherFile,
      Selectors.button -> continueButton
    )
    behave like pageWithExpectedMessages(expectedContent)
  }

  "return max of 5 files added" when {
    def applyView(form: Form[_]): HtmlFormat.Appendable = youHaveUploadedFilesPage.apply(
      form, radioOptions, controllers.routes.OtherReasonController.onPageLoadForUploadComplete(),uploadMaxRow)(userRequestWithCorrectKeys,messages,appConfig)
    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> titleMaxFiles,
      Selectors.h1 -> h1MaxFiles,
      Selectors.button -> continueButton
    )
    behave like pageWithExpectedMessages(expectedContent)
  }
}
