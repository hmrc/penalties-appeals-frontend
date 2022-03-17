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
import forms.upscan.UploadDocumentForm
import messages.UploadEvidenceMessages._
import models.pages.{EvidencePage, PageMode, UploadFirstDocumentPage}
import models.{NormalMode, PenaltyTypeEnum}
import models.upload.{UploadFormTemplateRequest, UploadJourney, UpscanInitiateResponseModel}
import org.jsoup.nodes.Document
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import testUtils.UploadData
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.UploadEvidencePage

class UploadEvidencePageSpec extends SpecBase with ViewBehaviours {

  "UploadEvidencePage" should {
    "Javascript is enabled" when {
      val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]
      object Selectors extends BaseSelectors {
        val detailsHeading = ".govuk-details__summary-text"

        val detailsContentP1 = ".govuk-details__text > p:nth-child(1)"

        val detailsContentP2 = ".govuk-details__text > p:nth-child(3)"

        val detailsContentP3 = ".govuk-details__text > p:nth-child(4)"

        val detailsContentLi: Int => String = (index: Int) => s".govuk-details__text > ul > li:nth-child($index)"

        val addAnotherButton = ".multi-file-upload__add-another"

        val continueButton = "#multi-upload-form > p > button"

        val multiUploadForm = "#multi-upload-form"

        val multiUploadFormFilesAKey = "data-multi-file-upload-uploaded-files"

        val multiUploadFormInitiateUploadAKey = "data-multi-file-upload-send-url-tpl"

        val multiUploadFormGetStatusAKey = "data-multi-file-upload-status-url-tpl"

        val multiUploadFormRemoveFileAKey = "data-multi-file-upload-remove-url-tpl"

        val multiUploadFormStillTransferring = "data-multi-file-upload-still-transferring"

        val multiUploadFormGenericError ="data-multi-file-upload-error-generic"

        val multiUploadFormServiceError ="data-multi-file-upload-error-url-tpl"
      }

      def previousUploadsToString(previousUploads: Option[Seq[UploadJourney]]): String = {
      previousUploads match {
        case Some(_) => Json.stringify(Json.toJson(previousUploads.map(_.map(_.copy(downloadUrl = None)))))
        case None => ""
      }
    }

      def applyView(request: FakeRequest[_] = fakeRequest, previousUploads:Option[Seq[UploadJourney]] = None): HtmlFormat.Appendable = {
      uploadEvidencePage.apply(
        Some(controllers.routes.MakingALateAppealController.onPageLoad()),
        Some(controllers.routes.UpscanController.initiateCallToUpscan("1234")),
        Some(controllers.routes.UpscanController.getStatusOfFileUpload("1234", _)),
        Some(controllers.routes.UpscanController.removeFile("1234", _)),
        Some(previousUploadsToString(previousUploads)),
        Some(controllers.routes.UpscanController.getDuplicateFiles("1234")),
        Some(controllers.routes.ProblemWithServiceController.onPageLoad()),
        pageMode = PageMode(EvidencePage, NormalMode))(request, implicitly, implicitly)
    }

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
      Selectors.title -> jsTitle,
      Selectors.h1 -> jsH1,
      Selectors.pElementIndex(1) -> jsP1,
      Selectors.pElementIndex(2) -> jsP2,
      Selectors.pElementIndex(3) -> jsP4,
      Selectors.pElementIndex(4) -> jsP5,
      Selectors.detailsHeading -> jsDetailsHeading,
      Selectors.detailsContentP1 -> jsDetailsP1,
      Selectors.detailsContentLi(1) -> jsDetailsLi1,
      Selectors.detailsContentLi(2) -> jsDetailsLi2,
      Selectors.detailsContentLi(3) -> jsDetailsLi3,
      Selectors.detailsContentLi(4) -> jsDetailsLi4,
      Selectors.detailsContentLi(5) -> jsDetailsLi5,
      Selectors.detailsContentP2 -> jsDetailsP2,
      Selectors.detailsContentP3 -> jsDetailsP3,
      Selectors.addAnotherButton -> jsAddAnotherButton,
      Selectors.continueButton -> jsContinueButton
    )

      behave like pageWithExpectedMessages(expectedContent)

      "display the LPP variation when the appeal is for a LPP" must {
      implicit val lppDoc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString)))

      val expectedContent = Seq(
        Selectors.title -> jsTitle,
        Selectors.h1 -> jsH1,
        Selectors.pElementIndex(1) -> jsP1,
        Selectors.pElementIndex(2) -> jsP2Lpp,
        Selectors.pElementIndex(3) -> jsP4,
        Selectors.pElementIndex(4) -> jsP5,
        Selectors.detailsHeading -> jsDetailsHeading,
        Selectors.detailsContentP1 -> jsDetailsP1,
        Selectors.detailsContentLi(1) -> jsDetailsLi1,
        Selectors.detailsContentLi(2) -> jsDetailsLi2,
        Selectors.detailsContentLi(3) ->  jsDetailsLi3,
        Selectors.detailsContentLi(4) -> jsDetailsLi4,
        Selectors.detailsContentLi(5) -> jsDetailsLi5,
        Selectors.detailsContentP2 -> jsDetailsP2,
        Selectors.detailsContentP3 -> jsDetailsP3,
        Selectors.addAnotherButton -> jsAddAnotherButton,
        Selectors.continueButton -> jsContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(lppDoc)
    }

      "display appeal against the obligation page when the appeal is for LSP" must {
      implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> jsTitle,
        Selectors.h1 -> jsH1,
        Selectors.pElementIndex(1) -> jsP1,
        Selectors.pElementIndex(2) -> jsP2AppealAgainstObligation,
        Selectors.pElementIndex(3) -> jsP4,
        Selectors.pElementIndex(4) -> jsP5,
        Selectors.detailsHeading -> jsDetailsHeading,
        Selectors.detailsContentP1 -> jsDetailsP1,
        Selectors.detailsContentLi(1) -> jsDetailsLi1,
        Selectors.detailsContentLi(2) -> jsDetailsLi2,
        Selectors.detailsContentLi(3) -> jsDetailsLi3,
        Selectors.detailsContentLi(4) -> jsDetailsLi4,
        Selectors.detailsContentLi(5) -> jsDetailsLi5,
        Selectors.detailsContentP2 -> jsDetailsP2,
        Selectors.detailsContentP3 -> jsDetailsP3,
        Selectors.addAnotherButton -> jsAddAnotherButton,
        Selectors.continueButton -> jsContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

      "display the LPP variation when the appeal is for a LPP - Additional" must {
      implicit val lppAdditionalDoc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString)))

      val expectedContent = Seq(
        Selectors.title -> jsTitle,
        Selectors.h1 -> jsH1,
        Selectors.pElementIndex(1) -> jsP1,
        Selectors.pElementIndex(2) -> jsP2Lpp,
        Selectors.pElementIndex(3) -> jsP4,
        Selectors.pElementIndex(4) -> jsP5,
        Selectors.detailsHeading -> jsDetailsHeading,
        Selectors.detailsContentP1 -> jsDetailsP1,
        Selectors.detailsContentLi(1) -> jsDetailsLi1,
        Selectors.detailsContentLi(2) -> jsDetailsLi2,
        Selectors.detailsContentLi(3) -> jsDetailsLi3,
        Selectors.detailsContentLi(4) -> jsDetailsLi4,
        Selectors.detailsContentLi(5) -> jsDetailsLi5,
        Selectors.detailsContentP2 -> jsDetailsP2,
        Selectors.detailsContentP3 -> jsDetailsP3,
        Selectors.addAnotherButton -> jsAddAnotherButton,
        Selectors.continueButton -> jsContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(lppAdditionalDoc)
    }

      "display appeal against the obligation page when the appeal is for LPP" must {
      implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> jsTitle,
        Selectors.h1 -> jsH1,
        Selectors.pElementIndex(1) -> jsP1,
        Selectors.pElementIndex(2) -> jsP2LPPAppealAgainstObligation,
        Selectors.pElementIndex(3) -> jsP4,
        Selectors.pElementIndex(4) -> jsP5,
        Selectors.detailsHeading -> jsDetailsHeading,
        Selectors.detailsContentP1 -> jsDetailsP1,
        Selectors.detailsContentLi(1) -> jsDetailsLi1,
        Selectors.detailsContentLi(2) -> jsDetailsLi2,
        Selectors.detailsContentLi(3) -> jsDetailsLi3,
        Selectors.detailsContentLi(4) -> jsDetailsLi4,
        Selectors.detailsContentLi(5) -> jsDetailsLi5,
        Selectors.detailsContentP2 -> jsDetailsP2,
        Selectors.detailsContentP3 -> jsDetailsP3,
        Selectors.addAnotherButton -> jsAddAnotherButton,
        Selectors.continueButton -> jsContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

      "load the correct parameters for previous uploads" in {
      implicit val doc: Document = asDocument(
        applyView(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString), Some(UploadData.maxWaitingUploads))
      )
      val expectedString = Json.stringify(Json.toJson(Some(UploadData.maxWaitingUploads)))

      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormFilesAKey) shouldBe expectedString
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormStillTransferring) shouldBe jsUploadEvidenceStillTransferring
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormGenericError) shouldBe jsFileUploadTryAgain

    }

      "load the correct parameters for initiating uploads and getting status of uploads" in {
      implicit val doc: Document = asDocument(
        applyView(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString), Some(UploadData.maxWaitingUploads))
      )
      val expectedInitiateString = "/penalties-appeals/upscan/call-to-upscan/1234"
      val expectedStatusString = "/penalties-appeals/upscan/upload-status/1234/%7BfileRef%7D"
      val expectedRemovalString = "/penalties-appeals/upscan/remove-file/1234/%7BfileRef%7D"
      val expectedErrorServiceString = "/penalties-appeals/problem-with-service"

      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormInitiateUploadAKey) shouldBe expectedInitiateString
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormGetStatusAKey) shouldBe expectedStatusString
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormRemoveFileAKey) shouldBe expectedRemovalString
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormServiceError) shouldBe expectedErrorServiceString
    }
    }

    "Javascript is disabled" when {
      val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]

      object Selectors extends BaseSelectors {
        val detailsHeading = ".govuk-details__summary-text"

        val detailsContentP1 = ".govuk-details__text > p:nth-child(1)"

        val detailsContentP2 = ".govuk-details__text > p:nth-child(3)"

        val detailsContentP3 = ".govuk-details__text > p:nth-child(4)"

        val detailsContentLi: Int => String = (index: Int) => s".govuk-details__text > ul > li:nth-child($index)"

        val continueButton = "#file-upload-form .govuk-button--secondary"

        val uploadButton = "#file-upload-form .govuk-button"

        val chooseYourFirstFile = "#file-upload-form-group > label"
      }

      val form = UploadDocumentForm.form
      val mockUpscanInitiateResponseModel: UpscanInitiateResponseModel = UpscanInitiateResponseModel(
        reference = "file1",
        uploadRequest = UploadFormTemplateRequest(
          href = "/link",
          fields = Map.empty
        )
      )

      val noUploadAction = controllers.routes.MakingALateAppealController.onPageLoad().url
      def applyView(request: FakeRequest[_] = fakeRequest, previousUploads:Option[Seq[UploadJourney]] = None): HtmlFormat.Appendable = {
        uploadEvidencePage.apply(
          upscanResponse = Some(mockUpscanInitiateResponseModel),
          form = Some(form),
          nextPageIfNoUpload = Some(noUploadAction), pageMode = PageMode(UploadFirstDocumentPage, NormalMode), jsEnabled = false)(request, implicitly, implicitly)
      }

      implicit val doc: Document = asDocument(applyView())

      val expectedContent = Seq(
        Selectors.title -> noJsTitle,
        Selectors.h1 -> noJsH1,
        Selectors.pElementIndex(2) -> noJsP1,
        Selectors.pElementIndex(3) -> noJsP2,
        Selectors.pElementIndex(4) -> noJsP4,
        Selectors.pElementIndex(5) -> noJsP5,
        Selectors.detailsHeading -> noJsDetailsHeading,
        Selectors.detailsContentP1 -> noJsDetailsP1,
        Selectors.detailsContentLi(1) -> noJsDetailsLi1,
        Selectors.detailsContentLi(2) -> noJsDetailsLi2,
        Selectors.detailsContentLi(3) -> noJsDetailsLi3,
        Selectors.detailsContentLi(4) -> noJsDetailsLi4,
        Selectors.detailsContentLi(5) -> noJsDetailsLi5,
        Selectors.detailsContentP2 -> noJsDetailsP2,
        Selectors.detailsContentP3 -> noJsDetailsP3,
        Selectors.chooseYourFirstFile -> noJsChooseYourFirstFile,
        Selectors.continueButton -> noJsContinueButton,
        Selectors.uploadButton -> noJsUploadButton
      )

      behave like pageWithExpectedMessages(expectedContent)

      "display the LPP variation when the appeal is for a LPP" must {
        implicit val lppDoc: Document = asDocument(applyView(fakeRequest.withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString)))

        val expectedContent = Seq(
          Selectors.title -> noJsTitle,
          Selectors.h1 -> noJsH1,
          Selectors.pElementIndex(2) -> noJsP1,
          Selectors.pElementIndex(3) -> noJsP2Lpp,
          Selectors.pElementIndex(4) -> noJsP4,
          Selectors.pElementIndex(5) -> noJsP5,
          Selectors.detailsHeading -> noJsDetailsHeading,
          Selectors.detailsContentP1 -> noJsDetailsP1,
          Selectors.detailsContentLi(1) -> noJsDetailsLi1,
          Selectors.detailsContentLi(2) -> noJsDetailsLi2,
          Selectors.detailsContentLi(3) -> noJsDetailsLi3,
          Selectors.detailsContentLi(4) -> noJsDetailsLi4,
          Selectors.detailsContentLi(5) -> noJsDetailsLi5,
          Selectors.detailsContentP2 -> noJsDetailsP2,
          Selectors.detailsContentP3 -> noJsDetailsP3,
          Selectors.chooseYourFirstFile -> noJsChooseYourFirstFile,
          Selectors.continueButton -> noJsContinueButton,
          Selectors.uploadButton -> noJsUploadButton
        )

        behave like pageWithExpectedMessages(expectedContent)(lppDoc)
      }

      "display appeal against the obligation page when the appeal is for LSP" must {
        implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
          SessionKeys.isObligationAppeal -> "true")))

        val expectedContent = Seq(
          Selectors.title -> noJsTitle,
          Selectors.h1 -> noJsH1,
          Selectors.pElementIndex(2) -> noJsP1,
          Selectors.pElementIndex(3) -> noJsP2AppealAgainstObligation,
          Selectors.pElementIndex(4) -> noJsP4,
          Selectors.pElementIndex(5) -> noJsP5,
          Selectors.detailsHeading -> noJsDetailsHeading,
          Selectors.detailsContentP1 -> noJsDetailsP1,
          Selectors.detailsContentLi(1) -> noJsDetailsLi1,
          Selectors.detailsContentLi(2) -> noJsDetailsLi2,
          Selectors.detailsContentLi(3) -> noJsDetailsLi3,
          Selectors.detailsContentLi(4) -> noJsDetailsLi4,
          Selectors.detailsContentLi(5) -> noJsDetailsLi5,
          Selectors.detailsContentP2 -> noJsDetailsP2,
          Selectors.detailsContentP3 -> noJsDetailsP3,
          Selectors.chooseYourFirstFile -> noJsChooseYourFirstFile,
          Selectors.continueButton -> noJsContinueButton,
          Selectors.uploadButton -> noJsUploadButton
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "display the LPP variation when the appeal is for a LPP - Additional" must {
        implicit val lppAdditionalDoc: Document = asDocument(applyView(fakeRequest.withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString)))

        val expectedContent = Seq(
          Selectors.title -> noJsTitle,
          Selectors.h1 -> noJsH1,
          Selectors.pElementIndex(2) -> noJsP1,
          Selectors.pElementIndex(3) -> noJsP2Lpp,
          Selectors.pElementIndex(4) -> noJsP4,
          Selectors.pElementIndex(5) -> noJsP5,
          Selectors.detailsHeading -> noJsDetailsHeading,
          Selectors.detailsContentP1 -> noJsDetailsP1,
          Selectors.detailsContentLi(1) -> noJsDetailsLi1,
          Selectors.detailsContentLi(2) -> noJsDetailsLi2,
          Selectors.detailsContentLi(3) -> noJsDetailsLi3,
          Selectors.detailsContentLi(4) -> noJsDetailsLi4,
          Selectors.detailsContentLi(5) -> noJsDetailsLi5,
          Selectors.detailsContentP2 -> noJsDetailsP2,
          Selectors.detailsContentP3 -> noJsDetailsP3,
          Selectors.chooseYourFirstFile -> noJsChooseYourFirstFile,
          Selectors.continueButton -> noJsContinueButton,
          Selectors.uploadButton -> noJsUploadButton
        )

        behave like pageWithExpectedMessages(expectedContent)(lppAdditionalDoc)
      }

      "display appeal against the obligation page when the appeal is for LPP" must {
        implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
          SessionKeys.isObligationAppeal -> "true")))

        val expectedContent = Seq(
          Selectors.title -> noJsTitle,
          Selectors.h1 -> noJsH1,
          Selectors.pElementIndex(2) -> noJsP1,
          Selectors.pElementIndex(3) -> noJsP2LPPAppealAgainstObligation,
          Selectors.pElementIndex(4) -> noJsP4,
          Selectors.pElementIndex(5) -> noJsP5,
          Selectors.detailsHeading -> noJsDetailsHeading,
          Selectors.detailsContentP1 -> noJsDetailsP1,
          Selectors.detailsContentLi(1) -> noJsDetailsLi1,
          Selectors.detailsContentLi(2) -> noJsDetailsLi2,
          Selectors.detailsContentLi(3) -> noJsDetailsLi3,
          Selectors.detailsContentLi(4) -> noJsDetailsLi4,
          Selectors.detailsContentLi(5) -> noJsDetailsLi5,
          Selectors.detailsContentP2 -> noJsDetailsP2,
          Selectors.detailsContentP3 -> noJsDetailsP3,
          Selectors.chooseYourFirstFile -> noJsChooseYourFirstFile,
          Selectors.continueButton -> noJsContinueButton,
          Selectors.uploadButton -> noJsUploadButton
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }
}