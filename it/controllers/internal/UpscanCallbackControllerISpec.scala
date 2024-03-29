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

import java.time.LocalDateTime

import crypto.CryptoProvider
import models.upload._
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.cache.DataKey
import utils.IntegrationSpecCommonBase

class UpscanCallbackControllerISpec extends IntegrationSpecCommonBase {

  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]

  lazy val cryptoProvider: CryptoProvider = injector.instanceOf[CryptoProvider]

  implicit val crypto: Encrypter with Decrypter = cryptoProvider.getCrypto

  val validJsonToParse: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref1",
      |    "downloadUrl": "http://example.com/file1.txt",
      |    "fileStatus": "READY",
      |    "uploadDetails": {
      |        "fileName": "file1.txt",
      |        "fileMimeType": "text/plain",
      |        "uploadTimestamp": "2023-04-24T09:30:00Z",
      |        "checksum": "check12345678",
      |        "size": 987
      |    },
      |    "uploadFields": {
      |      "key": "abcxyz",
      |      "x-amz-algorithm": "AWS4-HMAC-SHA256"
      |    }
      |}
      |""".stripMargin
  )

  val validFailureJsonToParse: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref1",
      |    "fileStatus": "FAILED",
      |    "failureDetails": {
      |       "failureReason": "QUARANTINE",
      |       "message": "This file has a virus"
      |    }
      |}
      |""".stripMargin
  )

  val jsonAsModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("http://example.com/file1.txt"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2023, 4, 24, 9, 30),
      checksum = "check12345678",
      size = 987
    )),
    uploadFields = Some(Map(
      "key" -> "abcxyz",
      "x-amz-algorithm" -> "AWS4-HMAC-SHA256"
    ))
  )

  val jsonAsModelForFailure: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.FAILED,
    downloadUrl = None,
    uploadDetails = None,
    failureDetails = Some(
      FailureDetails(
        failureReason = FailureReasonEnum.QUARANTINE,
        message = "upscan.fileHasVirus"
      )
    )
  )

  val uploadFieldsForUpdateCall: Map[String, String] = Map(
    "key" -> "abcxyz",
    "x-amz-algorithm" -> "AWS4-HMAC-SHA256"
  )

  "POST /upscan-callback/:journeyId" should {
    "return BAD REQUEST" when {
      "the body received is valid JSON but not a valid model" in {
        val result = await(buildClientForRequestToApp("/internal", "/upscan-callback/12345?isJsEnabled=true").post(
          Json.parse(
            """
              |{
              | "valid": "json"
              |}
              |""".stripMargin)
        ))
        result.status shouldBe BAD_REQUEST
        result.body.contains("Invalid UploadJourney payload") shouldBe true
      }
    }

    "return NO CONTENT" when {
      "the body received is valid and the state is updated" in {
        await(repository.collection.deleteMany(filter = Document()).toFuture())
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref1", UploadStatusEnum.WAITING,
          uploadFields = Some(uploadFieldsForUpdateCall)), isInitiateCall = true))
        val result = await(buildClientForRequestToApp("/internal", "/upscan-callback/12345?isJsEnabled=true").post(validJsonToParse))
        result.status shouldBe NO_CONTENT
        await(repository.collection.countDocuments().toFuture()) shouldBe 1
        val modelInRepository: UploadJourney = await(repository.get[SensitiveUploadJourney]("12345")(DataKey("ref1"))(SensitiveUploadJourney.format)).get.decryptedValue
        modelInRepository.downloadUrl shouldBe jsonAsModel.downloadUrl
        modelInRepository.reference shouldBe jsonAsModel.reference
        modelInRepository.failureDetails shouldBe jsonAsModel.failureDetails
        modelInRepository.fileStatus shouldBe jsonAsModel.fileStatus
        modelInRepository.uploadDetails shouldBe jsonAsModel.uploadDetails
        modelInRepository.uploadFields shouldBe jsonAsModel.uploadFields
      }

      "the body received is valid and the state is updated - failure callback" in {
        await(repository.collection.deleteMany(filter = Document()).toFuture())
        await(repository.updateStateOfFileUpload("12345", UploadJourney("ref1", UploadStatusEnum.WAITING), isInitiateCall = true))
        val result = await(buildClientForRequestToApp("/internal", "/upscan-callback/12345?isJsEnabled=true").post(validFailureJsonToParse))
        result.status shouldBe NO_CONTENT
        await(repository.collection.countDocuments().toFuture()) shouldBe 1
        val modelInRepository: UploadJourney = await(repository.get[SensitiveUploadJourney]("12345")(DataKey("ref1"))).get.decryptedValue
        modelInRepository.downloadUrl shouldBe jsonAsModelForFailure.downloadUrl
        modelInRepository.reference shouldBe jsonAsModelForFailure.reference
        modelInRepository.failureDetails shouldBe jsonAsModelForFailure.failureDetails
        modelInRepository.fileStatus shouldBe jsonAsModelForFailure.fileStatus
        modelInRepository.uploadDetails shouldBe jsonAsModelForFailure.uploadDetails
      }
    }
  }
}
