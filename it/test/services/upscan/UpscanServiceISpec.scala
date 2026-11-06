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

package services.upscan

import models.NormalMode
import models.upload.{FailureDetails, UploadJourney, UploadStatusEnum}
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.UpscanStub.{failedInitiateCall, successfulInitiateCall}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase
import java.time.LocalDateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanServiceISpec extends IntegrationSpecCommonBase {
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val service: UpscanService = injector.instanceOf[UpscanService]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  "initiateSynchronousCallToUpscan" should {
    "return Left when the call to Upscan fails" in new Setup {
      failedInitiateCall
      val result = service.initiateSynchronousCallToUpscan("J1234", false, NormalMode)
      await(result).isLeft shouldBe true
    }

    "return Right and update Mongo when the upscan initiate call succeeds" in new Setup {
      val mockDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
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
      val result = service.initiateSynchronousCallToUpscan("J1234", false, NormalMode)
      await(result).isRight shouldBe true
      val expectedUploadJourneyModel = UploadJourney(
        reference = "12345",
        fileStatus = UploadStatusEnum.WAITING,
        lastUpdated = mockDateTime,
        uploadFields = Some(Map(
          "key" -> "abcxyz",
          "algo" -> "md5"
        ))
      )
      await(repository.getUploadsForJourney(Some("J1234"))).get.head.copy(lastUpdated = mockDateTime) shouldBe expectedUploadJourneyModel
    }
  }

  "waitForStatus" should {
    val blockToDoNothing = (_: Option[FailureDetails], _: Option[String]) => Future(Ok("ran the block"))

    "return an ISE" when {
      "there is no upload details in Mongo" in new Setup {
        val result = service.waitForStatus("J1234", "file1", System.nanoTime() + 1000000000L, NormalMode, false, blockToDoNothing)(FakeRequest(), implicitly)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the 'file upload is taking longer than expected' page when the recursive call times out" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val result = service.waitForStatus("J1234", "file1", System.nanoTime() + 1000000000L, NormalMode, false, blockToDoNothing)(FakeRequest(), implicitly)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "run the block when the file has the correct status" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val result = service.waitForStatus("J1234", "file1", System.nanoTime() + 1000000000L, NormalMode, false, blockToDoNothing)(FakeRequest(), implicitly)
      status(result) shouldBe OK
      contentAsString(result) shouldBe "ran the block"
    }
  }

  "removeFileFromJourney" should {
    "remove the file from the specified journey" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("F1234", UploadStatusEnum.READY), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("F1235", UploadStatusEnum.READY), isInitiateCall = true))
      await(service.removeFileFromJourney("J1234", "F1234"))
      eventually {
        await(repository.getUploadsForJourney(Some("J1234"))).get.size shouldBe 1
        await(repository.getUploadsForJourney(Some("J1234"))).get.head.reference shouldBe "F1235"
      }
    }
  }

  "getAmountOfFilesUploadedForJourney" should {
    "return the amount of files uploaded" in new Setup  {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("F1234", UploadStatusEnum.READY), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("F1235", UploadStatusEnum.READY), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("F1236", UploadStatusEnum.FAILED), isInitiateCall = true))
      val result = service.getAmountOfFilesUploadedForJourney("J1234")
      await(result) shouldBe 2
    }

    "return 0 when there is no uploads for the journey" in new Setup  {
      val result = service.getAmountOfFilesUploadedForJourney("J1234")
      await(result) shouldBe 0
    }
  }

  "getFileNameForJourney" should {
    "return the file name if the file reference exists" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", fileUploadModel, isInitiateCall = true))
      val result = service.getFileNameForJourney(journeyId = "J1234", fileReference = "ref1")
      await(result).isDefined shouldBe true
      await(result).get shouldBe "file1.txt"
    }

    "return nothing if the file reference does not exist" in new Setup {
      val result = service.getFileNameForJourney(journeyId = "J1234", fileReference = "ref1")
      await(result).isEmpty shouldBe true
    }
  }
}
