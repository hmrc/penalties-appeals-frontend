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

package repositories

import models.upload._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import org.scalatest.concurrent.Eventually.eventually
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.cache.DataKey
import utils.IntegrationSpecCommonBase

import java.time.LocalDateTime
import scala.concurrent.Future

class UploadJourneyRepositoryISpec extends IntegrationSpecCommonBase {

  lazy val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]

  def deleteAll(): Future[DeleteResult] =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture()

  class Setup {
    await(deleteAll())
  }

  val callbackModel: UploadJourney = fileUploadModel

  val callbackModelFailed: UploadJourney = callbackModel.copy(
    fileStatus = UploadStatusEnum.FAILED,
    downloadUrl = None,
    uploadDetails = None,
    failureDetails = Some(
      FailureDetails(
        failureReason = FailureReasonEnum.QUARANTINE,
        message = "File has a virus"
      )
    )
  )

  val callbackModel2: UploadJourney = callbackModel.copy(
    reference = "ref2",
    downloadUrl = Some("download.file2/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2023, 1, 1, 1, 1),
      checksum = "check1234",
      size = 3
    ))
  )

  "updateStateOfFileUpload" should {
    "update the state based on the callback from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      val modelInRepository: UploadJourney = await(repository.get[UploadJourney]("1234")(DataKey("ref1"))).get
      modelInRepository shouldBe callbackModel
    }

    "not update the state when the document does not exist" in new Setup {
      val modelInRepository: Option[UploadJourney] = await(repository.get[UploadJourney]("1234")(DataKey("ref1")))
      modelInRepository.isEmpty shouldBe true
    }

    "not update the state when the document requested to be updated is not part of the initiate call not exist" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      val modelInRepository: Option[UploadJourney] = await(repository.get[UploadJourney]("1234")(DataKey("ref1")))
      modelInRepository.isEmpty shouldBe true
    }
  }

  "getFieldsForFileReference" should {
    s"return $None when the uploadDetails does not exist" in new Setup {
      val result = await(repository.getFieldsForFileReference("1234", "ref1"))
      result.isEmpty shouldBe true
    }

    s"return $Some when the uploadDetails exists" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      val result = await(repository.getFieldsForFileReference("1234", "ref1"))
      result.isDefined shouldBe true
      result shouldBe callbackModel.uploadFields
    }
  }

  "getStatusOfFileUpload" should {
    s"return $None when the document is not in Mongo" in new Setup {
      val result: Option[UploadStatus] = await(repository.getStatusOfFileUpload("1234", ""))
      result.isEmpty shouldBe true
    }

    s"return $Some when the document is in Mongo" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      val result: Option[UploadStatus] = await(repository.getStatusOfFileUpload("1234", "ref1"))
      result.isDefined shouldBe true
      result.get.status shouldBe UploadStatusEnum.READY.toString
    }

    s"return $Some when the document is in Mongo (failed upload)" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModelFailed, isInitiateCall = true))
      val result: Option[UploadStatus] = await(repository.getStatusOfFileUpload("1234", "ref1"))
      result.isDefined shouldBe true
      result.get.status shouldBe FailureReasonEnum.QUARANTINE.toString
      result.get.errorMessage.get shouldBe "File has a virus"
    }
  }

  "getUploadsForJourney" should {
    s"return $None when the document is not in Mongo" in new Setup {
      await(deleteAll())
      val result: Option[Seq[UploadJourney]] = await(repository.getUploadsForJourney(Some("1234")))
      result.isEmpty shouldBe true
    }

    s"return $None when no journey ID is given" in new Setup {
      await(deleteAll())
      val result: Option[Seq[UploadJourney]] = await(repository.getUploadsForJourney(None))
      result.isEmpty shouldBe true
    }

    s"return $Some when the document is in Mongo" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      val result: Option[Seq[UploadJourney]] = await(repository.getUploadsForJourney(Some("1234")))
      result.isDefined shouldBe true
      result.get shouldBe Seq(callbackModel)
    }
  }

  "getNumberOfDocumentsForJourneyId" should {
    "return the amount of uploads" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 0 when there is no uploads for the journey" in new Setup {
      await(repository.collection.deleteMany(Document()).toFuture())
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 0
    }
  }

  "removeUploadsForJourney" should {
    "remove the document for the given journey ID" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.collection.countDocuments().toFuture()) shouldBe 1
      await(repository.removeUploadsForJourney("1234"))
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
    }

    "not remove anything when the journey ID doesn't exist in the repo" in new Setup {
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      await(repository.removeUploadsForJourney("1234"))
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
    }
  }

  "removeFileForJourney" should {
    "remove the file in the journey if it exists" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1234", "ref1"))
      eventually {
        await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
        await(repository.getUploadsForJourney(Some("1234"))).get.head shouldBe callbackModel2
      }
    }

    "remove the whole document if the user removes their last upload" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      await(repository.removeFileForJourney("1234", "ref1"))
      eventually {
        await(repository.collection.countDocuments().toFuture()) shouldBe 0
      }
    }

    "do not remove the file in the journey if the file specified doesn't exist" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1234", "ref1234"))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "do not remove the file in the journey if the journey specified doesn't exist" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1235", "ref1234"))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }
  }

  "getAllChecksumsForJourney" should {
    "retrieve all checksums for all READY uploads" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      val result = await(repository.getAllChecksumsForJourney(Some("1234")))
      result.size shouldBe 2
    }

    "return an empty Seq if only failed uploads are present" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModelFailed, isInitiateCall = true))
      val result = await(repository.getAllChecksumsForJourney(Some("1234")))
      result.isEmpty shouldBe true
    }

    "return an empty Seq if there is no uploads" in new Setup {
      val result = await(repository.getAllChecksumsForJourney(Some("1234")))
      result.isEmpty shouldBe true
    }
  }

  "getFileForJourney" should {
    "return a upload journey model when the file reference exists" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", callbackModel, isInitiateCall = true))
      val result = await(repository.getFileForJourney("J1234", "ref1"))
      result.isDefined shouldBe true
      result.get shouldBe callbackModel
    }

    "return nothing when the file reference does not exist" in new Setup {
      val result = await(repository.getFileForJourney("J1234", "F1234"))
      result.isEmpty shouldBe true
    }
  }

  "getFileIndexForJourney" should {
    "return file index with a journey id and reference" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", callbackModel, isInitiateCall = true))
      val result = await(repository.getFileIndexForJourney("J1234", "ref1"))
      result shouldBe 0
    }

    "return nothing when the file reference does not exist" in new Setup {
      val result = await(repository.getFileIndexForJourney("J1234", "F1234"))
      result shouldBe -1
    }
  }
}