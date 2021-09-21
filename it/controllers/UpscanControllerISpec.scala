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

import models.upload.{UploadJourney, UploadStatusEnum}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import utils.IntegrationSpecCommonBase

import scala.concurrent.Future

class UpscanControllerISpec extends IntegrationSpecCommonBase {
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val controller: UpscanController = injector.instanceOf[UpscanController]

  def deleteAll(): Future[DeleteResult] =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture

  "GET /upscan/upload-status/:journeyId/:fileReference" should {
    "return OK (200)" when {
      "the user has an upload status in the repository" in {
        await(repository.updateStateOfFileUpload("1234", UploadJourney("file-1", UploadStatusEnum.WAITING)))
        val result = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
        status(result) shouldBe OK
        contentAsString(result) shouldBe "\"WAITING\""
      }
    }

    "return NOT_FOUND (404)" when {
      "the user has specified a file and journey id that is not in the repository" in {
        await(deleteAll())
        val result = controller.getStatusOfFileUpload("1234", "file-1")(FakeRequest())
        status(result) shouldBe NOT_FOUND
      }
    }
  }
}
