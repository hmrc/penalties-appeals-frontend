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
import forms.upscan.UploadDocumentForm
import messages.UploadAnotherDocumentMessages._
import models.pages.{PageMode, UploadAnotherDocumentPage}
import models.upload.{UploadFormTemplateRequest, UpscanInitiateResponseModel}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.noJs.UploadAnotherDocumentPage

class UploadAnotherDocumentPageSpec extends SpecBase with ViewBehaviours{

  val uploadAnotherDocumentPage: UploadAnotherDocumentPage = injector.instanceOf[UploadAnotherDocumentPage]
  object Selectors extends BaseSelectors {
    val detailsHeading = ".govuk-details__summary-text"

    val detailsContentP1 = ".govuk-details__text > p:nth-child(1)"

    val detailsContentP2 = ".govuk-details__text > p:nth-child(3)"

    val detailsContentP3 = ".govuk-details__text > p:nth-child(4)"

    val detailsContentLi: Int => String = (index: Int) => s".govuk-details__text > ul > li:nth-child($index)"

    val chooseYourNextFile = "#file-upload-form-group > label"

    val uploadButton = "#file-upload-form .govuk-button"

    val skipFileUploadButton = "#skip-file-upload"
  }

  val form: Form[String] = UploadDocumentForm.form
  val mockUpscanInitiateResponseModel: UpscanInitiateResponseModel = UpscanInitiateResponseModel(
    reference = "file1",
    uploadRequest = UploadFormTemplateRequest(
      href = "/link",
      fields = Map.empty
    )
  )

  def applyView(request: UserRequest[_] = userRequestWithCorrectKeys): HtmlFormat.Appendable = {
    uploadAnotherDocumentPage.apply(mockUpscanInitiateResponseModel, form, "/next-page", pageMode = PageMode(UploadAnotherDocumentPage, NormalMode))(request, implicitly, implicitly)
  }

  implicit val doc: Document = asDocument(applyView(userRequestWithCorrectKeys))

  "UploadAnotherDocumentPage" should {

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(3) -> p1,
      Selectors.pElementIndex(4) -> p2,
      Selectors.detailsHeading -> detailsHeading,
      Selectors.detailsContentP1 -> detailsP1,
      Selectors.detailsContentLi(1) -> detailsLi1,
      Selectors.detailsContentLi(2) -> detailsLi2,
      Selectors.detailsContentLi(3) -> detailsLi3,
      Selectors.detailsContentLi(4) -> detailsLi4,
      Selectors.detailsContentLi(5) -> detailsLi5,
      Selectors.chooseYourNextFile -> chooseYourNextFile,
      Selectors.uploadButton -> uploadButton,
      Selectors.skipFileUploadButton -> skipFileUploadButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "link to the next page in the journey when the user skips file upload" in {
      implicit val doc: Document = asDocument(applyView(fakeRequestConverter(correctLPPUserAnswers)))
      doc.select(Selectors.skipFileUploadButton).attr("href") shouldBe "/next-page"
    }
  }
}
