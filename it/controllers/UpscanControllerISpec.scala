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

import config.featureSwitches.{FeatureSwitching, WarnForDuplicateFiles}
import models.upload._
import models.{CheckMode, NormalMode}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.UpscanStub._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDateTime
import scala.concurrent.Future

class UpscanControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: UpscanController = injector.instanceOf[UpscanController]
  val repository: UploadJourneyRepository =
    injector.instanceOf[UploadJourneyRepository]

  def deleteAll(): Future[DeleteResult] =
    repository.collection
      .deleteMany(filter = Document())
      .toFuture()

  class Setup {
    await(deleteAll())
    disableFeatureSwitch(WarnForDuplicateFiles)
  }

  val duplicateFile = fileUploadModel.copy(
    fileStatus = UploadStatusEnum.DUPLICATE,
  )

  val duplicateFile2 = duplicateFile.copy(
    reference = "ref2",
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.now(),
      checksum = "123457",
      size = 200
    )))

  "GET /upscan/upload-status/:journeyId/:fileReference" should {
    "return OK (200)" when {
      "the user has an upload status in the repository" in new Setup {
        await(repository.updateStateOfFileUpload("1234", UploadJourney("ref1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val result: Future[Result] = controller.getStatusOfFileUpload("1234", "ref1")(FakeRequest())
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.obj("status" -> "WAITING")
      }

      "the user has duplicate uploads" when {
        "WarnForDuplicateFiles is enabled" must {
          "there is multiple sets of duplicates" in new Setup {
            enableFeatureSwitch(WarnForDuplicateFiles)
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref4"), isInitiateCall = true))

            val result = controller.getStatusOfFileUpload("1234", "ref4")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "Some of the files have the same contents. Check your uploaded files and remove duplicates using the ’Remove’ link.")
          }

          "there is one set of duplicates" when {
            "there is 1 duplicate" in new Setup {
              enableFeatureSwitch(WarnForDuplicateFiles)
              await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
              val result = controller.getStatusOfFileUpload("1234", "ref1")(FakeRequest())
              status(result) shouldBe OK
              contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "File 1 has the same contents as File 2. You can remove duplicate files using the ’Remove’ link.")
            }

            "there is 2 duplicates" in new Setup {
              enableFeatureSwitch(WarnForDuplicateFiles)
              await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
              val result = controller.getStatusOfFileUpload("1234", "ref3")(FakeRequest())
              status(result) shouldBe OK
              contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "File 1 has the same contents as Files 2 and 3. You can remove duplicate files using the ’Remove’ link.")
            }

            "there is 3 duplicates" in new Setup {
              enableFeatureSwitch(WarnForDuplicateFiles)
              await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
              val result = controller.getStatusOfFileUpload("1234", "ref4")(FakeRequest())
              status(result) shouldBe OK
              contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "File 1 has the same contents as Files 2, 3 and 4. You can remove duplicate files using the ’Remove’ link.")
            }

            "there is 4 duplicates" in new Setup {
              enableFeatureSwitch(WarnForDuplicateFiles)
              await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
              await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref5"), isInitiateCall = true))
              val result = controller.getStatusOfFileUpload("1234", "ref5")(FakeRequest())
              status(result) shouldBe OK
              contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "File 1 has the same contents as Files 2, 3, 4 and 5. You can remove duplicate files using the ’Remove’ link.")
            }
          }
        }
        "WarnForDuplicateFiles is disabled" must {
          "there is multiple sets of duplicates" in new Setup {
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref4"), isInitiateCall = true))

            val result = controller.getStatusOfFileUpload("1234", "ref4")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE")
          }
        }
      }
    }

    "return NOT_FOUND (404)" when {
      "the user has specified a file and journey id that is not in the repository" in new Setup {
        await(deleteAll())
        val result: Future[Result] = controller.getStatusOfFileUpload("1234", "ref1")(FakeRequest())
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
          |   "fields": {
          |     "key": "abcxyz",
          |     "algo": "md5"
          |   }
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
          |   "fields": {
          |     "key": "abcxyz",
          |     "algo": "md5"
          |   }
          | }
          |}
          |""".stripMargin)
    }

    "return 500 (Internal Server Error) when there is no upload state in the database" in new Setup {
      failedInitiateCall
      val result: WSResponse = await(buildClientForRequestToApp(uri = "/upscan/call-to-upscan/12345").post(""))
      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "POST /upscan/remove-file/:journeyId/:fileReference" should {
    "return 204 (No Content) when the user has specified a journey ID and file reference that is in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref1"), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref2"), isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result = await(controller.removeFile("1234", "ref1")(FakeRequest()))
      eventually {
        result.header.status shouldBe NO_CONTENT
        await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      }

    }

    "return 204 (No Content) when the user has specified a journey ID is NOT in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref1"), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref2"), isInitiateCall = true))
      val result: Future[Result] = controller.removeFile("1235", "ref1")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 204 (No Content) when the user has specified a file reference is NOT in the database" in new Setup {
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref1"), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref2"), isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result: Future[Result] = controller.removeFile("1235", "ref3")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 204 (No Content) and delete the journey record if the user deletes their last file" in new Setup {
      await(repository.updateStateOfFileUpload("1234", fileUploadModel.copy(reference = "ref1"), isInitiateCall = true))
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
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val body: Seq[(String, String)] = Seq(
          "key" -> "file1",
          "errorCode" -> "EntityTooSmall",
          "errorMessage" -> "Some arbitrary message",
          "errorRequestId" -> "request1"
        )
        val result: Result = await(controller.uploadFailure("1234")(FakeRequest("POST", "/upscan/upload-failed/1234").withFormUrlEncodedBody(body: _*)))
        result.header.status shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileEmpty"
      }

      "the failure is because the file is too large" in new Setup {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val body: Seq[(String, String)] = Seq(
          "key" -> "file1",
          "errorCode" -> "EntityTooLarge",
          "errorMessage" -> "Some arbitrary message",
          "errorRequestId" -> "request1"
        )
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest("POST", "/upscan/upload-failed/1234").withFormUrlEncodedBody(body: _*))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileTooLarge"
      }

      "the failure is because the file is not specified" in new Setup {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val body: Seq[(String, String)] = Seq(
          "key" -> "file1",
          "errorCode" -> "InvalidArgument",
          "errorMessage" -> "Some arbitrary message",
          "errorRequestId" -> "request1"
        )
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest("POST", "/upscan/upload-failed/1234").withFormUrlEncodedBody(body: _*))
        status(result) shouldBe NO_CONTENT
        val failureDetailsInRepo: FailureDetails = await(repository.getUploadsForJourney(Some("1234"))).get.head.failureDetails.get
        failureDetailsInRepo.failureReason shouldBe FailureReasonEnum.REJECTED
        failureDetailsInRepo.message shouldBe "upscan.fileNotSpecified"
      }

      "the failure is because there is some unknown client error" in new Setup {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val body: Seq[(String, String)] = Seq(
          "key" -> "file1",
          "errorCode" -> "InternalError",
          "errorMessage" -> "Some arbitrary message",
          "errorRequestId" -> "request1"
        )
        val result: Future[Result] = controller.uploadFailure("1234")(FakeRequest("POST", "/upscan/upload-failed/1234").withFormUrlEncodedBody(body: _*))
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

    "return 204 (No Content) when the form binds and update the record in Mongo (if previous upload is in FAILED state)" in new Setup {
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.FAILED,
          failureDetails = Some(FailureDetails(
            FailureReasonEnum.REJECTED,
            "some.message"
          ))
        ), isInitiateCall = true))
      val result: Future[Result] = controller.filePosted("1234")(FakeRequest("GET", "/file-posted?key=ref2"))
      status(result) shouldBe NO_CONTENT
      await(repository.getStatusOfFileUpload("1234", "ref2")).get.status shouldBe UploadStatusEnum.WAITING.toString
    }

    "return 204 (No Content) when the form binds and update the record in Mongo (if file was not in FAILED state)" in new Setup {
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      await(repository.updateStateOfFileUpload("1234",
        UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.WAITING
        ), isInitiateCall = true))
      val result: Future[Result] = controller.filePosted("1234")(FakeRequest("GET", "/file-posted?key=ref2"))
      status(result) shouldBe NO_CONTENT
      await(repository.getStatusOfFileUpload("1234", "ref2")).get.status shouldBe UploadStatusEnum.WAITING.toString
    }
  }

  "GET /upscan/file-verification/failed" should {
    "redirect to the non-JS file upload page and add the errorCode to the session" in new Setup {
      val result: Future[Result] = controller.preUpscanCheckFailed(false, NormalMode)(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge")).get(SessionKeys.errorCodeFromUpscan).get shouldBe "EntityTooLarge"
    }

    "redirect to the non-JS file upload page and add the errorCode to the session - check mode" in new Setup {
      val result: Future[Result] = controller.preUpscanCheckFailed(false, CheckMode)(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(CheckMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge")).get(SessionKeys.errorCodeFromUpscan).get shouldBe "EntityTooLarge"
    }

    "redirect to the additional file upload page and add the errorCode to the session" in new Setup {
      val result: Future[Result] = controller.preUpscanCheckFailed(true, NormalMode)(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge"))
      status(result) shouldBe SEE_OTHER
      await(result).session(FakeRequest("GET", "/file-verification/failed?errorCode=EntityTooLarge")).get(SessionKeys.errorCodeFromUpscan).get shouldBe "EntityTooLarge"
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
    }
  }

  "GET /upscan/file-verification/success" should {
    "show an ISE" when {
      "the repository doesn't have a status for this file under this journey" in new Setup {
        val result: Future[Result] = controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect back to the 'upload taking longer than expected' page when the recursive call times out" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val result: Future[Result] = controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "redirect to the non-JS file upload page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234")).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the non-JS file upload page when there is an error from Upscan - check mode" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.fileVerification(false, CheckMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(CheckMode).url
      await(result).session(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234")).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the successful upload page when there is no error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val result: Future[Result] = controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }

    "redirect to the additional file upload page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.fileVerification(true, NormalMode, false)(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
      status(result) shouldBe SEE_OTHER
      await(result).session(FakeRequest("GET", "/file-verification/failed?key=file1").withSession(SessionKeys.journeyId -> "J1234")).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
    }
  }


  "GET /upscan/duplicate-files/:journeyId" should {
    "return None when there is no duplicates in the repository" in new Setup {
      val result = controller.getDuplicateFiles("1234567")(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.obj()
    }

    "return Some when there is duplicates in the repository" when {
      "WarnForDuplicateFiles is enabled" must {
        "there is multiple sets of duplicates" in new Setup {
          enableFeatureSwitch(WarnForDuplicateFiles)
          await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref3"), isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref4"), isInitiateCall = true))

          val result = controller.getStatusOfFileUpload("1234", "ref4")(FakeRequest())
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE", "errorMessage" -> "Some of the files have the same contents. Check your uploaded files and remove duplicates using the ’Remove’ link.")
        }

        "there is one set of duplicates" when {
          "there is 1 duplicate" in new Setup {
            enableFeatureSwitch(WarnForDuplicateFiles)
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("message" -> "File 1 has the same contents as File 2. You can remove duplicate files using the ’Remove’ link.")
          }

          "there is 2 duplicates" in new Setup {
            enableFeatureSwitch(WarnForDuplicateFiles)
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("message" -> "File 1 has the same contents as Files 2 and 3. You can remove duplicate files using the ’Remove’ link.")
          }

          "there is 3 duplicates" in new Setup {
            enableFeatureSwitch(WarnForDuplicateFiles)
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("message" -> "File 1 has the same contents as Files 2, 3 and 4. You can remove duplicate files using the ’Remove’ link.")
          }

          "there is 4 duplicates" in new Setup {
            enableFeatureSwitch(WarnForDuplicateFiles)
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref5"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("message" -> "File 1 has the same contents as Files 2, 3, 4 and 5. You can remove duplicate files using the ’Remove’ link.")
          }
        }
      }

      "WarnForDuplicateFiles is disabled" must {
        "there is multiple sets of duplicates" in new Setup {
          await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref3"), isInitiateCall = true))
          await(repository.updateStateOfFileUpload("1234", duplicateFile2.copy(reference = "ref4"), isInitiateCall = true))

          val result = controller.getStatusOfFileUpload("1234", "ref4")(FakeRequest())
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj("status" -> "DUPLICATE")
        }

        "there is one set of duplicates" when {
          "there is 1 duplicate" in new Setup {
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj()
          }

          "there is 2 duplicates" in new Setup {
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj()
          }

          "there is 3 duplicates" in new Setup {
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj()
          }

          "there is 4 duplicates" in new Setup {
            await(repository.updateStateOfFileUpload("1234", duplicateFile, isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref2"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref3"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref4"), isInitiateCall = true))
            await(repository.updateStateOfFileUpload("1234", duplicateFile.copy(reference = "ref5"), isInitiateCall = true))
            val result = controller.getDuplicateFiles("1234")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj()
          }
        }
      }
    }
  }
}
