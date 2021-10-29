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

package controllers

import java.time.LocalDateTime

import models.NormalMode
import models.upload.{FailureDetails, FailureReasonEnum, UploadDetails, UploadJourney, UploadStatusEnum}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.UpscanStub._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import scala.concurrent.Future

class UpscanControllerISpec extends IntegrationSpecCommonBase {
  val controller: UpscanController = injector.instanceOf[UpscanController]
  val repository: UploadJourneyRepository =
    injector.instanceOf[UploadJourneyRepository]

  def deleteAll(): Future[DeleteResult] =
    repository.collection
      .deleteMany(filter = Document())
      .toFuture

  class Setup {
    await(deleteAll())
  }

  "GET /upscan/upload-status/:journeyId/:fileReference" should {
    "return OK (200)" when {
      "the user has an upload status in the repository" in new Setup {
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file-1", UploadStatusEnum.WAITING)))
        val result: Future[Result] = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.obj("status" -> "WAITING")
      }
    }

    "return NOT_FOUND (404)" when {
      "the user has specified a file and journey id that is not in the repository" in new Setup {
        await(deleteAll())
        val result: Future[Result] = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
        status(result) shouldBe NOT_FOUND
      }
    }
  }

  "POST /upscan/call-to-upscan/:journeyId" should {
    "return 200 (OK) when the user there is an upload state in the database" in new Setup {
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
      val result: WSResponse = await(
        buildClientForRequestToApp(uri = "/upscan/call-to-upscan/12345")
          .post("")
      )
      result.status shouldBe OK
      Json.parse(result.body) shouldBe Json.parse(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
    }

    "return 500 (Internal Server Error) when there is no upload state in the database" in new Setup {
      failedInitiateCall("asdf")
      val result: WSResponse = await(buildClientForRequestToApp(uri = "/upscan/call-to-upscan/12345").post(""))
      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "POST /upscan/remove-file/:journeyId/:fileReference" should {
    "return 204 (No Content) when the user has specified a journey ID and file reference that is in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file2.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result: Future[Result] = controller.removeFile("1234", "ref1")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
    }

    "return 204 (No Content) when the user has specified a journey ID is NOT in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file2.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result: Future[Result] = controller.removeFile("1235", "ref1")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 204 (No Content) when the user has specified a file reference is NOT in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file2.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result: Future[Result] = controller.removeFile("1235", "ref3")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 204 (No Content) and delete the journey record if the user deletes their last file" in new Setup {
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      val result: Future[Result] = controller.removeFile("1234", "ref1")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
    }
  }

  "POST /upscan/upload-failed/:journeyId" should {
    "return NO_CONTENT and add the correct message" when {
      "the failure is because the file is too small" in new Setup {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "key": "file1",
            |   "errorCode": "EntityTooSmall",
            |   "errorMessage": "Some arbitrary message",
            |   "errorRequestId": "request1"
            |}
            |""".stripMargin)
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest().withJsonBody(jsonBody))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileEmpty"
      }

      "the failure is because the file is too large" in new Setup  {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "key": "file1",
            |   "errorCode": "EntityTooLarge",
            |   "errorMessage": "Some arbitrary message",
            |   "errorRequestId": "request1"
            |}
            |""".stripMargin)
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest().withJsonBody(jsonBody))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileTooLarge"
      }

      "the failure is because the file is not specified" in new Setup  {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "key": "file1",
            |   "errorCode": "InvalidArgument",
            |   "errorMessage": "Some arbitrary message",
            |   "errorRequestId": "request1"
            |}
            |""".stripMargin)
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest().withJsonBody(jsonBody))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileNotSpecified"
      }

      "the failure is because there is some unknown client error" in new Setup  {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        val jsonBody: JsValue = Json.parse(
          """
            |{
            |   "key": "file1",
            |   "errorCode": "InternalError",
            |   "errorMessage": "Some arbitrary message",
            |   "errorRequestId": "request1"
            |}
            |""".stripMargin)
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest().withJsonBody(jsonBody))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.unableToUpload"
      }
    }
  }

  "GET /upscan/file-posted/:journeyId" should {

    "return 400 (Bad Request) when the form fails to bind" in new Setup {
      val result: Future[Result] = controller.filePosted("1234")(FakeRequest("GET", "/file-posted"))
      status(result) shouldBe BAD_REQUEST
    }

    "return 204 (No Content) when the form binds and update the record in Mongo (if after 1 second since last update)" in new Setup {
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.FAILED,
          failureDetails = Some(FailureDetails(
            FailureReasonEnum.REJECTED,
            "some.message"
          ))
        )))
      Thread.sleep(2000)
      val result: Future[Result] = controller.filePosted("1234")(FakeRequest("GET", "/file-posted?key=ref2"))
      status(result) shouldBe NO_CONTENT
      await(repository.getStatusOfFileUpload("1234", "ref2")).get.status shouldBe UploadStatusEnum.WAITING.toString
    }

    "return 204 (No Content) when the form binds and update the record in Mongo (if before 1 second since last update)" in new Setup {
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.FAILED,
          failureDetails = Some(FailureDetails(
            FailureReasonEnum.REJECTED,
            "some.message"
          ))
        )))
      val result: Future[Result] = controller.filePosted("1234")(FakeRequest("GET", "/file-posted?key=ref2"))
      status(result) shouldBe NO_CONTENT
      await(repository.getStatusOfFileUpload("1234", "ref2")).get.status shouldBe FailureReasonEnum.REJECTED.toString
    }
  }

  "GET /upscan/file-verification/failed" should {
    "redirect to the non-JS file upload page and add the errorCode to the session" in new Setup {
      val result: Future[Result] = controller.preUpscanCheckFailed(false)(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge")).get(SessionKeys.errorCodeFromUpscan).get shouldBe "EntityTooLarge"
    }

    "redirect to the additional file upload page and add the errorCode to the session" in new Setup {
      val result: Future[Result] = controller.preUpscanCheckFailed(true)(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge"))
      //TODO: change this to redirect and redirect location
      status(result) shouldBe OK
      await(result).session(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge")).get(SessionKeys.errorCodeFromUpscan).get shouldBe "EntityTooLarge"
    }
  }

  "GET /upscan/file-verification/success" should {
    "show an ISE" when {
      "the repository doesn't have a status for this file under this journey" in new Setup {
        val result: Future[Result] = controller.fileVerification(false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the recursive call times out" in new Setup {
        await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.WAITING)))
        val result: Future[Result] = controller.fileVerification(false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the non-JS file upload page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType")))))
      val result: Future[Result] = controller.fileVerification(false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234")).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the successful upload page when there is no error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.READY)))
      val result: Future[Result] = controller.fileVerification(false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete().url
    }

    "redirect to the additional file upload page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType")))))
      val result: Future[Result] = controller.fileVerification(true)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      //TODO: change this to redirect and redirect location
      status(result) shouldBe OK
      await(result).session(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234")).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }
  }
}
