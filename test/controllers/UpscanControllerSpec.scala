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

import base.SpecBase
import connectors.UpscanConnector
import connectors.httpParsers.UpscanInitiateHttpParser.InvalidJson
import models.upload.{FailureReasonEnum, UploadDetails, UploadFormTemplateRequest, UploadJourney, UploadStatus, UploadStatusEnum, UpscanInitiateResponseModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.mongo.cache.CacheItem

import java.time.{Instant, LocalDateTime}
import scala.concurrent.Future

class UpscanControllerSpec extends SpecBase {
  val repository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val connector: UpscanConnector = mock(classOf[UpscanConnector])
  val controller: UpscanController = new UpscanController(
    repository,
    connector
  )(appConfig, stubMessagesControllerComponents())

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

  "UpscanController" should {
    "getStatusOfFileUpload" must {
      "return OK" when {
        "the user has an upload state in the database" in {
          val returnModel = UploadStatus(UploadStatusEnum.READY.toString, None)
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel)
        }

        "the user has a successful file upload in the database" in {
          val returnModel = UploadStatus(UploadStatusEnum.READY.toString, None)
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel)
        }

        "the user has a failed file upload in the database" in {
          val returnModel = UploadStatus(FailureReasonEnum.QUARANTINE.toString, Some("upscan.fileHasVirus"))
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel)
        }
      }

      "return NOT FOUND" when {
        "the database does not contain such values specified" in {
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(None))
          val result =
            controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe NOT_FOUND
        }
      }
    }

    "initiateCallToUpscan" must {
      "return OK" when {
        "the user has a upload state in the database" in {
          when(connector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(
              UpscanInitiateResponseModel(
                "1234", UploadFormTemplateRequest("1234", Map("A" -> "B"))
              )
            )))
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.toJsObject(uploadJourneyModel)(UploadJourney.writes), Instant.now(), Instant.now())))
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
          when(connector.initiateToUpscan(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Left(InvalidJson)))
          val result = controller.initiateCallToUpscan("1234")(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "removeFile" must  {
      "return NO CONTENT" when {
        "the journey and file exists or doesn't exist in the database" in {
          when(repository.removeFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful())
          val result = controller.removeFile("J1234", "F1234")(fakeRequest)
          status(result) shouldBe NO_CONTENT
        }
      }

      "return ISE" when {
        "the repository fails to delete the specified journey-file combination" in {
          when(repository.removeFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("Something *spooky* went wrong.")))
          val result = controller.removeFile("J1234", "F1234")(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "uploadFailure" should {
      "return a BAD_REQUEST when the S3 service doesn't match the expected response model" in {
        val result = controller.uploadFailure("J1234")(fakeRequest.withJsonBody(
          Json.parse("{}")
        ))
        status(result) shouldBe BAD_REQUEST
      }

      "return NO_CONTENT" when {
        "the failure is because the file is too small" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.obj(), Instant.now(), Instant.now())))
          val result = controller.uploadFailure("J1234")(fakeRequest.withJsonBody(
            Json.obj(
              "key" -> "file1",
              "errorCode" -> "EntityTooSmall",
              "errorMessage" -> "Some message about file"
            )
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because the file is too large" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.obj(), Instant.now(), Instant.now())))
          val result = controller.uploadFailure("J1234")(fakeRequest.withJsonBody(
            Json.obj(
              "key" -> "file1",
              "errorCode" -> "EntityTooLarge",
              "errorMessage" -> "Some message about file"
            )
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because the file is not specified" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.obj(), Instant.now(), Instant.now())))
          val result = controller.uploadFailure("J1234")(fakeRequest.withJsonBody(
            Json.obj(
              "key" -> "file1",
              "errorCode" -> "InvalidArgument",
              "errorMessage" -> "Some message about file"
            )
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because there is some unknown client error" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(CacheItem("1234", Json.obj(), Instant.now(), Instant.now())))
          val result = controller.uploadFailure("J1234")(fakeRequest.withJsonBody(
            Json.obj(
              "key" -> "file1",
              "errorCode" -> "ExpiredToken",
              "errorMessage" -> "Some message about file"
            )
          ))
          status(result) shouldBe NO_CONTENT
        }
      }
    }

    "preFlightUpload" should {
      "return a Created response with the CORS Allow Origin header" in {
        val result = controller.preFlightUpload("J1234")(fakeRequest)
        status(result) shouldBe CREATED
        await(result).header.headers(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN) shouldBe "*"
      }
    }

    "filePosted" should {
      "return Bad Request when the query parameters can not be bound" in {
        val result = controller.filePosted("J1234")(fakeRequest)
        status(result) shouldBe BAD_REQUEST
      }

      "return No Content and update the file upload when called" in {
        when(repository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheItem("1234", Json.obj(), Instant.now(), Instant.now())))
        val result = controller.filePosted("J1234")(FakeRequest("GET", "/file-posted?key=key1&bucket=bucket1"))
        status(result) shouldBe NO_CONTENT
      }
    }
  }
}
