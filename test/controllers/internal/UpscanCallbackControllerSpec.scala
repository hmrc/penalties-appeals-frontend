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

package controllers.internal

import base.SpecBase
import models.upload._
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.mongo.cache.DataKey

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class UpscanCallbackControllerSpec extends SpecBase {
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val service: UpscanService = injector.instanceOf[UpscanService]
  val controller: UpscanCallbackController = new UpscanCallbackController(repository, service)

  class Setup {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  val invalidJsonBody: String =
    """
      |{
      | this: is invalid
      |}
      |
      |""".stripMargin

  val validJsonButInvalidModel: JsValue = Json.parse(
    """
      |{
      | "not": "recognised"
      |}
      |
      |""".stripMargin)

  val validCallbackFromUpscan: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref1",
      |    "downloadUrl": "download.url",
      |    "fileStatus": "READY",
      |    "uploadDetails": {
      |        "fileName": "file1.txt",
      |        "fileMimeType": "text/plain",
      |        "uploadTimestamp": "2018-04-24T09:30:00Z",
      |        "checksum": "check12345678",
      |        "size": 987
      |    },
      |    "uploadFields": {
      |       "key": "abcxyz",
      |      "algo": "md5"
      |    }
      |}
      |""".stripMargin
  )

  val validCallbackFromUpscanDuplicate: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref2",
      |    "downloadUrl": "download.url",
      |    "fileStatus": "READY",
      |    "uploadDetails": {
      |        "fileName": "file1.txt",
      |        "fileMimeType": "text/plain",
      |        "uploadTimestamp": "2018-04-24T09:30:00Z",
      |        "checksum": "check12345678",
      |        "size": 987
      |    },
      |    "uploadFields": {
      |         "key": "abcxyz",
      |         "algo": "md5"
      |    }
      |}
      |""".stripMargin
  )

  val callbackFromUpscanWithFailure: JsValue = Json.parse(
    """
      |{
      | "reference": "ref1",
      | "fileStatus": "FAILED",
      | "failureDetails": {
      |   "failureReason": "REJECTED",
      |   "message": "this file was rejected"
      | }
      |}
      |""".stripMargin
  )

  val uploadJourneyModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
      checksum = "check12345678",
      size = 987
    )),
    uploadFields = Some(Map(
        "key" -> "abcxyz",
        "algo" -> "md5"
    ))
  )

  val uploadJourneyModelDuplicate: UploadJourney = UploadJourney(
    reference = "ref2",
    fileStatus = UploadStatusEnum.DUPLICATE,
    downloadUrl = Some("download.url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
      checksum = "check12345678",
      size = 987
    )),
    uploadFields = Some(Map(
      "key" -> "abcxyz",
      "algo" -> "md5"
    ))
  )

  val uploadJourneyModelWithFailure: UploadJourney = uploadJourneyModel.copy(
    fileStatus = UploadStatusEnum.FAILED, downloadUrl = None, uploadDetails = None,
    failureDetails = Some(FailureDetails(
      failureReason = FailureReasonEnum.REJECTED,
      message = "upscan.invalidMimeType"
    ))
  )

  val uploadFieldsForUpdateCall: Map[String, String] = Map(
    "key" -> "abcxyz",
    "algo" -> "md5"
  )

  "UpscanController" should {
    "return an ISE" when {
      s"the body can not be parsed to an $UploadJourney model" in new Setup {
        val result = controller.callbackFromUpscan("12345", true)(fakeRequest.withBody(validJsonButInvalidModel))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return NO CONTENT" when {
      lazy val mockDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
      "the body is valid and state has been updated" in new Setup {
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref1", UploadStatusEnum.WAITING,
          uploadFields = Some(uploadFieldsForUpdateCall)), isInitiateCall = true))
        val result = await(controller.callbackFromUpscan("12345", true)(fakeRequest.withBody(validCallbackFromUpscan)))
        result.header.status shouldBe NO_CONTENT
        eventually {
          val modelInRepo: UploadJourney = await(repository.get[UploadJourney]("12345")(DataKey("ref1"))).get
          modelInRepo.copy(lastUpdated = mockDateTime) shouldBe uploadJourneyModel.copy(lastUpdated = mockDateTime)
        }
      }

      "the file is rejected and state has been updated" in new Setup {
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val result = await(controller.callbackFromUpscan("12345", true)(fakeRequest.withBody(callbackFromUpscanWithFailure)))
        result.header.status shouldBe NO_CONTENT
        eventually {
          val modelInRepo: UploadJourney = await(repository.get[UploadJourney]("12345")(DataKey("ref1"))).get
          modelInRepo.copy(lastUpdated = mockDateTime) shouldBe uploadJourneyModelWithFailure.copy(lastUpdated = mockDateTime, uploadFields = None)
        }
      }

      "the file is accepted but the file is a duplicate - mark as duplicate and keep the upload details (valid case)" in new Setup {
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref2", UploadStatusEnum.WAITING, uploadFields = Some(uploadFieldsForUpdateCall)), isInitiateCall = true))
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref1", UploadStatusEnum.WAITING, uploadFields = Some(uploadFieldsForUpdateCall)), isInitiateCall = true))
        await(repository.updateStateOfFileUpload("12345", uploadJourneyModel))
        //Used to get around a race condition
        eventually {
          await(repository.getUploadsForJourney(Some("12345")).map(_.get.find(_.reference == "ref1").get)).fileStatus shouldBe UploadStatusEnum.READY
        }
        val result = await(controller.callbackFromUpscan("12345", true)(fakeRequest.withBody(validCallbackFromUpscanDuplicate)))
        result.header.status shouldBe NO_CONTENT
        val modelInRepo: UploadJourney = await(repository.get[UploadJourney]("12345")(DataKey("ref2"))).get
        modelInRepo.copy(lastUpdated = mockDateTime) shouldBe uploadJourneyModelDuplicate.copy(lastUpdated = mockDateTime)
      }

      "a callback has been received but the user has requested for the file to be removed" in new Setup {
        val result = controller.callbackFromUpscan("12345", true)(fakeRequest.withBody(validCallbackFromUpscan))
        status(result) shouldBe NO_CONTENT
        val modelInRepo: Option[UploadJourney] = await(repository.get[UploadJourney]("12345")(DataKey("ref1")))
        modelInRepo.isEmpty shouldBe true
      }
    }
  }
}
