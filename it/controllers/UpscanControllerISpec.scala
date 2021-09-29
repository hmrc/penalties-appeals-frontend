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

import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.UpscanStub._
import utils.IntegrationSpecCommonBase

import java.time.LocalDateTime
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
        val result = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
        status(result) shouldBe OK
        contentAsString(result) shouldBe "\"WAITING\""
      }
    }

    "return NOT_FOUND (404)" when {
      "the user has specified a file and journey id that is not in the repository" in new Setup {
        await(deleteAll())
        val result = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
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
      val result = await(
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
      val result = await(buildClientForRequestToApp(uri = "/upscan/call-to-upscan/12345").post(""))
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
      val result = controller.removeFile("1234", "ref1")(FakeRequest())
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
      val result = controller.removeFile("1235", "ref1")(FakeRequest())
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
      val result = controller.removeFile("1235", "ref3")(FakeRequest())
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
      val result = controller.removeFile("1234", "ref1")(FakeRequest())
      status(result) shouldBe NO_CONTENT
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
    }
  }
}
