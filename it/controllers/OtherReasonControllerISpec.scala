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

package controllers

import config.featureSwitches.{FeatureSwitching, NonJSRouting}
import controllers.testHelpers.AuthorisationTest
import models.NormalMode
import models.session.UserAnswers
import models.upload._
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.UpscanStub.successfulInitiateCall
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OtherReasonControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {
  val controller: OtherReasonController = injector.instanceOf[OtherReasonController]
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]

  class Setup(sessionDataToStore: UserAnswers = UserAnswers("1234", Json.obj())) extends UserAnswersSetup(sessionDataToStore) {
    await(repository.collection.deleteMany(Document()).toFuture())
    await(repository.collection.countDocuments().toFuture()) shouldBe 0
  }

  val callbackModel: UploadJourney = fileUploadModel
  val callbackModel2: UploadJourney = callbackModel.copy(
    reference = "ref2",
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2023, 1, 1, 1, 1),
      checksum = "check1235",
      size = 3
    ))
  )

  "GET /when-inability-to-manage-account-happened" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest))
      result.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhenDidBecomeUnable(NormalMode), "GET", "/when-inability-to-manage-account-happened")
  }

  "POST /when-inability-to-manage-account-happened" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val result = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new Setup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val result = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithInvalidBody))
        result.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new Setup(userAnswers()) {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val result = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        result.header.status shouldBe BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in new Setup(userAnswers()) {
        val result = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest))
        result.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in new Setup(userAnswers()) {

        val noDayJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "",
          "date.month" -> "02",
          "date.year" -> "2025"
        )

        val noMonthJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "02",
          "date.month" -> "",
          "date.year" -> "2025"
        )

        val noYearJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "02",
          "date.month" -> "02",
          "date.year" -> ""
        )
        val resultNoDay = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        resultNoDay.header.status shouldBe BAD_REQUEST

        val resultNoMonth = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        resultNoMonth.header.status shouldBe BAD_REQUEST

        val resultNoYear = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        resultNoYear.header.status shouldBe BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWhenDidBecomeUnable(NormalMode), "POST", "/when-inability-to-manage-account-happened")
  }

  "GET /upload-evidence-for-the-appeal" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest))
      result.header.status shouldBe OK
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled" in new Setup(userAnswers()) {
      disableFeatureSwitch(NonJSRouting)
      val result = await(controller.onPageLoadForUploadEvidence(NormalMode, false)(fakeRequest))
      result.header.status shouldBe SEE_OTHER
      enableFeatureSwitch(NonJSRouting)
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled but the feature switch is enabled" in new Setup(userAnswers()) {
      enableFeatureSwitch(NonJSRouting)
      val result = await(controller.onPageLoadForUploadEvidence(NormalMode, false)(fakeRequest))
      result.header.status shouldBe SEE_OTHER
      disableFeatureSwitch(NonJSRouting)
    }

    "return 200 (OK) when the user does have JavaScript enabled and the feature switch is disabled" in new Setup(userAnswers()) {
      disableFeatureSwitch(NonJSRouting)
      val result = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest))
      result.header.status shouldBe OK
      enableFeatureSwitch(NonJSRouting)
    }

    runControllerPredicateTests(controller.onPageLoadForUploadEvidence(NormalMode, true), "GET", "/upload-evidence-for-the-appeal?isJsEnabled=true")
  }

  "GET /why-was-the-vat-late" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
      result.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode), "GET", "/why-was-the-vat-late")
  }

  "POST /why-was-the-vat-late" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("why-return-submitted-late-text" -> "Other Reason")
      val result = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectBody))
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whyReturnSubmittedLate).get shouldBe "Other Reason"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup(userAnswers()) {
        val result = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
        result.header.status shouldBe BAD_REQUEST
      }

      "invalid characters are entered" in new Setup(userAnswers()) {
        val fakeRequestWithInvalidChars: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("why-return-submitted-late-text" -> "コし")

        val result: Result = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithInvalidChars))
        result.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWhyReturnSubmittedLate(NormalMode), "POST", "/why-was-the-vat-late")
  }

  "GET /upload-first-document" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      result.header.status shouldBe OK
    }

    "return 200 (OK) when the users has JS active and the feature switch is enabled" in new Setup(userAnswers()) {
      enableFeatureSwitch(NonJSRouting)
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      result.header.status shouldBe OK
      disableFeatureSwitch(NonJSRouting)
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      result.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest.withSession(
        SessionKeys.failureMessageFromUpscan -> "upscan.invalidMimeType")))
      result.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onPageLoadForFirstFileUpload(NormalMode), "GET", "/upload-first-document")
  }

  "GET /uploaded-documents" should {
    "return OK and correct view" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      val result = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(result) shouldBe OK
    }

    "return 303 (SEE_OTHER) when the user has no uploads" in new Setup(userAnswers()) {
      val result = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) when the user has no successful uploads" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val result = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    runControllerPredicateTests(controller.onPageLoadForUploadComplete(NormalMode), "GET", "/uploaded-documents")
  }

  "GET /upload-another-document" should {

    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest))
      result.header.status shouldBe OK
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      result.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup(userAnswers()) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val result = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.invalidMimeType")))
      result.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onPageLoadForAnotherFileUpload(NormalMode), "GET", "/upload-another-document")

  }

  "POST /remove-file-upload" should {
    "return an ISE when the form fails to bind" in new Setup(userAnswers()) {
      val result = controller.removeFileUpload(NormalMode)(fakeRequest
        .withFormUrlEncodedBody("file" -> "file1"))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to the first document upload page when there is no uploads left" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1235", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val result = await(controller.removeFileUpload(NormalMode)(fakeRequest.withFormUrlEncodedBody("fileReference" -> "file1")))
      eventually {
        result.header.status shouldBe SEE_OTHER
        result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      }
    }

    "reload the upload list when there is more uploads" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file2", UploadStatusEnum.READY), isInitiateCall = true))
      val result = controller.removeFileUpload(NormalMode)(fakeRequest.withFormUrlEncodedBody("fileReference" -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }

  }

  "POST /upload-taking-longer-than-expected" should {
    "show an ISE" when {
      "the repository doesn't have a status for this file under this journey" in new Setup(userAnswers()) {
        val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(SessionKeys.fileReference -> "file1"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect back to the 'upload taking longer than expected' page when the recursive call times out" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "redirect to the non-JS first file upload page when there is an error from Upscan" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(
        SessionKeys.fileReference -> "file1",
        SessionKeys.isAddingAnotherDocument -> "false"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the non-JS upload another document page when there is an error from Upscan" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest
        .withSession(SessionKeys.fileReference -> "file1",
          SessionKeys.isAddingAnotherDocument -> "true"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the successful upload page when there is no error from Upscan" in new Setup(userAnswers()) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest
        .withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }
  }

  "POST /uploaded-documents" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file" +
      " upload page when user specifies 'yes'" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file upload" +
      " page when user specifies 'no'" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup(userAnswers()) {
        val result = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequest))
        result.header.status shouldBe BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForUploadComplete(NormalMode), "POST", "/uploaded-documents")
  }

  "GET /upload-evidence-question" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers()) {
      val result = await(controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest))
      result.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForUploadEvidenceQuestion(NormalMode), "GET", "/upload-evidence-question")
  }

  "POST /upload-evidence-question" should {
    "return 303 (SEE_OTHER), navigate to check your answer page when the user answers no" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val result = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      result.header.status shouldBe Status.SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "no"
    }

    "return 303 (SEE_OTHER), navigate to upload evidence and add the session key to the session - when the user answers yes" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes", "isJsEnabled" -> "true")
      val result = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      result.header.status shouldBe Status.SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER), navigate to first upload page and add the session key to the session - when the user answers yes, JS is disabled" in new Setup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes", "isJsEnabled" -> "false")
      val result = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      result.header.status shouldBe Status.SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, false).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new Setup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val result = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithInvalidBody))
        result.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new Setup(userAnswers()) {
        val result = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest))
        result.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForUploadEvidenceQuestion(NormalMode), "POST", "/upload-evidence-question")
  }
}