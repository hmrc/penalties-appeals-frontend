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

package services.upscan

import akka.actor.ActorSystem
import base.SpecBase
import connectors.UpscanConnector
import connectors.httpParsers.UpscanInitiateHttpParser.UnexpectedFailure
import models.upload._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.cache.CacheItem

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanServiceSpec extends SpecBase {
  val repository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val connector: UpscanConnector = mock(classOf[UpscanConnector])
  val actorSystemForScheduling: ActorSystem = injector.instanceOf[ActorSystem]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  class Setup {
    reset(repository, connector)
    val service: UpscanService = new UpscanService(
      repository,
      connector,
      actorSystemForScheduling
    )(appConfig, errorHandler)
  }
  "initiateSynchronousCallToUpscan" should {
    "return Left when the call to upscan fails" in new Setup {
      when(connector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "something went wrong"))))
      val result = await(service.initiateSynchronousCallToUpscan("J1234", false))
      result.isLeft shouldBe true
    }

    "return Right and update the journey in Mongo when the call succeeds" in new Setup {
      when(connector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1", UploadFormTemplateRequest("/", Map.empty)))))
      when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheItem("", Json.obj(), Instant.now(), Instant.now())))
      val result = await(service.initiateSynchronousCallToUpscan("J1234", false))
      result.isRight shouldBe true
    }
  }

  "waitForStatus" should {
    val blockToDoNothing = (_: Option[FailureDetails], _: Option[String]) => Future(Ok(""))
    "return an ISE" when {
      "the status doesn't exist in Mongo" in new Setup {
        when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        val result = service.waitForStatus("J1234", "File1234", 100000000L, blockToDoNothing)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the recursion times out" in new Setup {
        when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(UploadStatus(UploadStatusEnum.WAITING.toString))))
        val result = service.waitForStatus("J1234", "File1234", System.nanoTime() + 1000000000L, blockToDoNothing)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "run the block when a response is decision from Upscan" in new Setup {
      when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(UploadStatus(UploadStatusEnum.READY.toString))))
      when(repository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(UploadJourney("File1234", UploadStatusEnum.READY)))))
      val result = service.waitForStatus("J1234", "File1234", System.nanoTime() + 1000000000L, blockToDoNothing)
      status(result) shouldBe OK
    }
  }
}
