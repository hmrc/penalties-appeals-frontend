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

import java.time.{Instant, LocalDateTime}

import base.SpecBase
import connectors.UpscanConnector
import connectors.httpParsers.UpscanInitiateHttpParser.InvalidJson
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.upscan.{UploadFormTemplateRequest, UpscanInitiateResponseModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}
import play.api.libs.json._
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.mongo.cache.CacheItem

import scala.concurrent.{ExecutionContext, Future}

class UpscanControllerSpec extends SpecBase {
  val repository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val connector: UpscanConnector = mock(classOf[UpscanConnector])
  val controller: UpscanController = new UpscanController(
    repository,
    connector
  )(appConfig, stubMessagesControllerComponents())

  "UpscanController" should {
    "return OK" when {
      "the user has an upload state in the database" in {
        when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(UploadStatusEnum.WAITING)))
        val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
        status(result) shouldBe OK
        contentAsString(result) shouldBe "\"WAITING\""
      }

      "the user has a successful file upload in the database" in {
        when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(UploadStatusEnum.READY)))

        val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
        status(result) shouldBe OK
        contentAsString(result) shouldBe "\"READY\""
      }
    }

      "return NOT FOUND" when {
        "the database does not contain such values specified" in {
          when(
            repository.getStatusOfFileUpload(
              ArgumentMatchers.any(),
              ArgumentMatchers.any()
            )
          ).thenReturn(Future.successful(None))
          val result =
            controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe NOT_FOUND
        }
      }
    }

    val uploadJourneyModel: UploadJourney = UploadJourney(
      reference = "ref1",
      fileStatus = UploadStatusEnum.READY,
      downloadUrl = Some("download.url"),
      uploadDetails = Some(
        UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
          checksum = "check12345678",
          size = 987
        )
      )
    )

    "initiateCallToUpscan" must {
      "return OK" when {
        "the user has a upload state in the database" in {
          when(connector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(
              UpscanInitiateResponseModel(
                "1234", UploadFormTemplateRequest("1234", Map("A" ->"B"))
              )
            )))
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.toJsObject(uploadJourneyModel)(UploadJourney.format), Instant.now(), Instant.now())))
          val result = controller.initiateCallToUpscan("1234")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj(
            "reference" -> "1234",
            "uploadRequest" -> Json.obj(
              "href" -> "1234",
              "fields" -> Map("A" -> "B")
            )
          )
        }
      }

      "return Internal Server Error" when {
        "the user does not have an upload state in the database" in {
          when(
            connector.initiateToUpscan(ArgumentMatchers.any())(
              ArgumentMatchers.any(),
              ArgumentMatchers.any()
            )
          ).thenReturn(Future.successful(Left(InvalidJson)))
          val result = controller.initiateCallToUpscan("1234")(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
}