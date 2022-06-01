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
import messages.UploadEvidenceMessages._
import models.pages.{EvidencePage, PageMode}
import models.upload.UploadJourney
import models.{NormalMode, PenaltyTypeEnum}
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
    val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]
    object Selectors extends BaseSelectors {
      val detailsHeading = ".govuk-details__summary-text"

      val detailsContentP1 = ".govuk-details__text > p:nth-child(1)"

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
        controllers.routes.MakingALateAppealController.onPageLoad(),
        controllers.routes.UpscanController.initiateCallToUpscan("1234"),
        controllers.routes.UpscanController.getStatusOfFileUpload("1234", _),
        controllers.routes.UpscanController.removeFile("1234", _),
        previousUploadsToString(previousUploads),
        controllers.routes.UpscanController.getDuplicateFiles("1234"),
        controllers.routes.ProblemWithServiceController.onPageLoad(),
        pageMode = PageMode(EvidencePage, NormalMode))(request, implicitly, implicitly)
    }

    implicit val doc: Document = asDocument(applyView())

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(1) -> p1,
      Selectors.pElementIndex(2) -> p2,
      Selectors.pElementIndex(3) -> p4,
      Selectors.pElementIndex(4) -> p5,
      Selectors.detailsHeading -> detailsHeading,
      Selectors.detailsContentP1 -> detailsP1,
      Selectors.detailsContentLi(1) -> detailsLi1,
      Selectors.detailsContentLi(2) -> detailsLi2,
      Selectors.detailsContentLi(3) -> detailsLi3,
      Selectors.detailsContentLi(4) -> detailsLi4,
      Selectors.detailsContentLi(5) -> detailsLi5,
      Selectors.addAnotherButton -> addAnotherButton,
      Selectors.continueButton -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "display the LPP variation when the appeal is for a LPP" must {
      implicit val lppDoc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString)))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(1) -> p1,
        Selectors.pElementIndex(2) -> p2Lpp,
        Selectors.pElementIndex(3) -> p4,
        Selectors.pElementIndex(4) -> p5,
        Selectors.detailsHeading -> detailsHeading,
        Selectors.detailsContentP1 -> detailsP1,
        Selectors.detailsContentLi(1) -> detailsLi1,
        Selectors.detailsContentLi(2) -> detailsLi2,
        Selectors.detailsContentLi(3) -> detailsLi3,
        Selectors.detailsContentLi(4) -> detailsLi4,
        Selectors.detailsContentLi(5) -> detailsLi5,
        Selectors.addAnotherButton -> addAnotherButton,
        Selectors.continueButton -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(lppDoc)
    }

    "display appeal against the obligation page when the appeal is for LSP" must {
      implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(1) -> p1,
        Selectors.pElementIndex(2) -> p2AppealAgainstObligation,
        Selectors.pElementIndex(3) -> p4,
        Selectors.pElementIndex(4) -> p5,
        Selectors.detailsHeading -> detailsHeading,
        Selectors.detailsContentP1 -> detailsP1,
        Selectors.detailsContentLi(1) -> detailsLi1,
        Selectors.detailsContentLi(2) -> detailsLi2,
        Selectors.detailsContentLi(3) -> detailsLi3,
        Selectors.detailsContentLi(4) -> detailsLi4,
        Selectors.detailsContentLi(5) -> detailsLi5,
        Selectors.addAnotherButton -> addAnotherButton,
        Selectors.continueButton -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "display the LPP variation when the appeal is for a LPP - Additional" must {
      implicit val lppAdditionalDoc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString)))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(1) -> p1,
        Selectors.pElementIndex(2) -> p2Lpp,
        Selectors.pElementIndex(3) -> p4,
        Selectors.pElementIndex(4) -> p5,
        Selectors.detailsHeading -> detailsHeading,
        Selectors.detailsContentP1 -> detailsP1,
        Selectors.detailsContentLi(1) -> detailsLi1,
        Selectors.detailsContentLi(2) -> detailsLi2,
        Selectors.detailsContentLi(3) -> detailsLi3,
        Selectors.detailsContentLi(4) -> detailsLi4,
        Selectors.detailsContentLi(5) -> detailsLi5,
        Selectors.addAnotherButton -> addAnotherButton,
        Selectors.continueButton -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(lppAdditionalDoc)
    }

    "display appeal against the obligation page when the appeal is for LPP" must {
      implicit val doc: Document = asDocument(applyView(fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(1) -> p1,
        Selectors.pElementIndex(2) -> p2LPPAppealAgainstObligation,
        Selectors.pElementIndex(3) -> p4,
        Selectors.pElementIndex(4) -> p5,
        Selectors.detailsHeading -> detailsHeading,
        Selectors.detailsContentP1 -> detailsP1,
        Selectors.detailsContentLi(1) -> detailsLi1,
        Selectors.detailsContentLi(2) -> detailsLi2,
        Selectors.detailsContentLi(3) -> detailsLi3,
        Selectors.detailsContentLi(4) -> detailsLi4,
        Selectors.detailsContentLi(5) -> detailsLi5,
        Selectors.addAnotherButton -> addAnotherButton,
        Selectors.continueButton -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "load the correct parameters for previous uploads" in {
      implicit val doc: Document = asDocument(
        applyView(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString), Some(UploadData.maxWaitingUploads))
      )
      val expectedString = Json.stringify(Json.toJson(Some(UploadData.maxWaitingUploads)))

      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormFilesAKey) shouldBe expectedString
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormStillTransferring) shouldBe uploadEvidenceStillTransferring
      doc.select(Selectors.multiUploadForm).attr(Selectors.multiUploadFormGenericError) shouldBe fileUploadTryAgain

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
}