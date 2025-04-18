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

import org.apache.pekko.actor.ActorSystem
import base.SpecBase
import connectors.UpscanConnector
import connectors.httpParsers.{ErrorResponse, UnexpectedFailure}
import models.NormalMode
import models.upload._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys
import java.time.{Instant, LocalDateTime}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanServiceSpec extends SpecBase with LogCapturing {
  val mockRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val mockConnector: UpscanConnector = mock(classOf[UpscanConnector])
  val actorSystemForScheduling: ActorSystem = injector.instanceOf[ActorSystem]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  class Setup(useMockAppConfig: Boolean = false) {
    reset(mockRepository)
    reset(mockConnector)
    reset(mockAppConfig)
    val service: UpscanService = new UpscanService(
      mockRepository,
      mockConnector,
      actorSystemForScheduling
    )(if(useMockAppConfig) mockAppConfig else appConfig, errorHandler)
  }
  "initiateSynchronousCallToUpscan" should {
    "return Left when the call to upscan fails" in new Setup {
      when(mockConnector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "something went wrong"))))
      withCaptureOfLoggingFrom(logger) {
        logs => {
          val result: Either[ErrorResponse, UpscanInitiateResponseModel] =
            await(service.initiateSynchronousCallToUpscan("J1234", isAddingAnotherDocument = false, NormalMode))
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_UPSCAN.toString)) shouldBe true
          result.isLeft shouldBe true
        }
      }
    }

    "return Right and update the journey in Mongo when the call succeeds" in new Setup {
      when(mockConnector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1", UploadFormTemplateRequest("/", Map.empty)))))
      when(mockRepository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("file1")))
      val result: Either[ErrorResponse, UpscanInitiateResponseModel] =
        await(service.initiateSynchronousCallToUpscan("J1234", false, NormalMode))
      result.isRight shouldBe true
    }
  }

  "waitForStatus" should {
    val blockToDoNothing = (_: Option[FailureDetails], _: Option[String]) => Future(Ok(""))
    "return an ISE" when {
      "the status doesn't exist in Mongo" in new Setup {
        when(mockRepository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        val result: Future[Result] = service.waitForStatus(
          "J1234", "File1234", 100000000L, NormalMode, isAddingAnotherDocument = false, blockToDoNothing)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the 'file upload taking too long' page - when the recursion times out" in new Setup {
      when(mockRepository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(UploadStatus(UploadStatusEnum.WAITING.toString))))
      val result: Future[Result] = service.waitForStatus("J1234", "File1234", System.nanoTime() + 1000000000L, NormalMode, false, blockToDoNothing)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "run the block when a response is decision from Upscan" in new Setup {
      when(mockRepository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(UploadStatus(UploadStatusEnum.READY.toString))))
      when(mockRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(UploadJourney("File1234", UploadStatusEnum.READY)))))
      val result: Future[Result] = service.waitForStatus(
        "J1234", "File1234", System.nanoTime() + 1000000000L, NormalMode, isAddingAnotherDocument = false, blockToDoNothing)
      status(result) shouldBe OK
    }
  }

  "removeFileFromJourney" should {
    "propagate the request" in new Setup {
      when(mockRepository.removeFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful((): Unit))
      val result: Future[Unit] = service.removeFileFromJourney("J1234", "F1234")
      await(result) shouldBe ((): Unit)
    }
  }

  "getAmountOfFilesUploadedForJourney" should {
    "return 0" when {
      "there is no uploads in Mongo for this journey" in new Setup {
        when(mockRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq())))
        val result: Future[Int] = service.getAmountOfFilesUploadedForJourney("J1234")
        await(result) shouldBe 0
      }

      "there is no record in Mongo for this journey" in new Setup {
        when(mockRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        val result: Future[Int] = service.getAmountOfFilesUploadedForJourney("J1234")
        await(result) shouldBe 0
      }
    }

    "return the amount of uploads in Mongo for the journey" in new Setup {
      when(mockRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(
          UploadJourney("file1", UploadStatusEnum.READY),
          UploadJourney("file2", UploadStatusEnum.READY),
          UploadJourney("file4", UploadStatusEnum.FAILED)
        ))))
      val result: Future[Int] = service.getAmountOfFilesUploadedForJourney("J1234")
      await(result) shouldBe 2
    }
  }

  "getFileNameForJourney" should {
    "retrieve the upload details and return the file name" in new Setup {
      when(mockRepository.getFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(
          Some(
            UploadJourney("file1",
              UploadStatusEnum.READY,
              uploadDetails = Some(
                UploadDetails(fileName = "file123.txt", fileMimeType = "mime/type", uploadTimestamp = LocalDateTime.now(), checksum = "check123", size = 1)
              )
            )
          )
        ))
      val result: Future[Option[String]] = service.getFileNameForJourney("journey1", "file1")
      await(result).isDefined shouldBe true
      await(result).get shouldBe "file123.txt"
    }

    "return None when the repository finds nothing" in new Setup {
      when(mockRepository.getFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))
      val result: Option[String] = await(service.getFileNameForJourney("journey1", "file1"))
      result.isEmpty shouldBe true
    }

    "propagate the any error back to the caller" in new Setup {
      when(mockRepository.getFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("i broke :(")))
      val result: Exception = intercept[Exception](await(service.getFileNameForJourney("journey1", "file1")))
      result.getMessage shouldBe "i broke :("
    }
  }


  "scheduleCallbackOperation" should {
    val blockToDoNothing = Future(Ok(""))
    "run the block after X ms when the app config value is enabled" in new Setup(useMockAppConfig = true) {
      when(mockAppConfig.upscanCallbackDelayEnabled).thenReturn(true)
      when(mockAppConfig.upscanCallbackUpdateDelay).thenReturn(500)
      val timeBeforeTest = System.currentTimeMillis
      val result = await(service.scheduleCallbackOperation(blockToDoNothing))
      val timeAfterTest = System.currentTimeMillis
      result shouldBe Ok("")
      (timeBeforeTest + 499 < timeAfterTest) shouldBe true
    }


    "run the block instantly when the app config value is disabled" in new Setup(useMockAppConfig = true) {
      when(mockAppConfig.upscanCallbackDelayEnabled).thenReturn(false)
      val timeBeforeTest = Instant.now().toEpochMilli
      val result = await(service.scheduleCallbackOperation(blockToDoNothing))
      val timeAfterTest = Instant.now().toEpochMilli
      result shouldBe Ok("")
      (timeBeforeTest + 499 > timeAfterTest) shouldBe true
    }
  }
}
