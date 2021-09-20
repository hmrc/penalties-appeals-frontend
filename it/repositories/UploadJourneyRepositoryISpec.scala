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

  "updateStateOfFileUpload" should {
    "update the state based on the callback from Upscan" in {
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

      val result: CacheItem = await(repository.updateStateOfFileUpload("1234", callbackModel))
      result.id shouldBe "1234"
      val modelInRepository: UploadJourney = await(repository.get[UploadJourney]("1234")(DataKey("ref1"))).get
      modelInRepository shouldBe callbackModel
    }
  }
}
