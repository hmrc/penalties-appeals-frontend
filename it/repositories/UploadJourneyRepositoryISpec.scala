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

package repositories

import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}
import utils.IntegrationSpecCommonBase

import java.time.LocalDateTime
import scala.concurrent.Future

class UploadJourneyRepositoryISpec extends IntegrationSpecCommonBase {
  lazy val repository = injector.instanceOf[UploadJourneyRepository]

  def deleteAll(): Future[DeleteResult] =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture

  class Setup {
    await(deleteAll())
  }

  val callbackModel = UploadJourney(
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
  )

  val callbackModel2 = UploadJourney(
    reference = "ref2",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file2/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 3
    ))
  )


  "updateStateOfFileUpload" should {
    "update the state based on the callback from Upscan" in new Setup {
      val result: CacheItem = await(repository.updateStateOfFileUpload("1234", callbackModel))
      result.id shouldBe "1234"
      val modelInRepository: UploadJourney = await(repository.get[UploadJourney]("1234")(DataKey("ref1"))).get
      modelInRepository shouldBe callbackModel
    }
  }

  "getStatusOfFileUpload" should {
    s"return $None when the document is not in Mongo" in new Setup {
      val result = await(repository.getStatusOfFileUpload("1234", "ref1"))
      result.isDefined shouldBe false
    }

    s"return $Some when the document is in Mongo" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      val result = await(repository.getStatusOfFileUpload("1234", "ref1"))
      result.isDefined shouldBe true
      result.get shouldBe UploadStatusEnum.READY
    }
  }

  "getUploadsForJourney" should {
    s"return $None when the document is not in Mongo" in new Setup {
      await(deleteAll())
      val result = await(repository.getUploadsForJourney(Some("1234")))
      result.isDefined shouldBe false
    }

    s"return $None when no Id is given" in new Setup {
      await(deleteAll())
      val result = await(repository.getUploadsForJourney(None))
      result.isDefined shouldBe false
    }

    s"return $Some when the document is in Mongo" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      val result = await(repository.getUploadsForJourney(Some("1234")))
      result.isDefined shouldBe true
      result.get shouldBe Seq(callbackModel)
    }
  }

  "getNumberOfDocumentsForJourneyId" should {
    "return the amount of uploads" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel2))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "return 0 when there is no uploads for the journey" in new Setup {
      await(repository.collection.deleteMany(Document()).toFuture())
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 0
    }
  }

  "removeUploadsForJourney" should {
    "remove the document for the given journey ID" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
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
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel2))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1234", "ref1"))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      await(repository.getUploadsForJourney(Some("1234"))).get.head shouldBe callbackModel2
    }

    "do not remove the file in the journey if the file specified doesn't exist" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel2))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1234", "ref1234"))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }

    "do not remove the file in the journey if the journey specified doesn't exist" in new Setup {
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel2))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      await(repository.removeFileForJourney("1235", "ref1234"))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
    }
  }
}