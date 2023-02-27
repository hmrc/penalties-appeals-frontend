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

package controllers

import base.SpecBase
import connectors.UpscanConnector
import connectors.httpParsers.InvalidJson
import models.upload._
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys
import utils.SessionKeys
import viewtils.EvidenceFileUploadsHelper
import java.time.LocalDateTime

import config.featureSwitches.{FeatureSwitching, WarnForDuplicateFiles}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanControllerSpec extends SpecBase with LogCapturing with FeatureSwitching {
  val repository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val connector: UpscanConnector = mock(classOf[UpscanConnector])
  val service: UpscanService = mock(classOf[UpscanService])
  val helper: EvidenceFileUploadsHelper = mock(classOf[EvidenceFileUploadsHelper])
  val controller: UpscanController = new UpscanController(
    repository,
    connector,
    service,
    helper
  )

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
          when(repository.getFileIndexForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(1))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel)
        }

        "the user has a successful file upload in the database" in {
          val returnModel = UploadStatus(UploadStatusEnum.READY.toString, None)
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          when(repository.getFileIndexForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(1))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel)
        }

        "the user has a failed file upload in the database" in {
          val returnModel = UploadStatus(FailureReasonEnum.QUARANTINE.toString, Some("upscan.fileHasVirus"))
          val resultModel = returnModel.copy(errorMessage = Some("File 2 contains a virus. Choose another file."))
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          when(repository.getFileIndexForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(1))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(resultModel)
        }

        "the user has a duplicate file upload in the database - it should call to get an up-to-date inset text message" in {
          val returnModel = UploadStatus(FailureReasonEnum.DUPLICATE.toString, Some("old text"))
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(returnModel)))
          when(helper.getInsetTextForUploadsInRepository(ArgumentMatchers.any())(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("text to display")))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(returnModel.copy(errorMessage = Some("text to display")))
        }
      }

      "return NOT FOUND" when {
        "the database does not contain such values specified" in {
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(None))
          withCaptureOfLoggingFrom(logger) {
            logs => {
              val result =
                await(controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest))
              logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_UPLOAD_STATUS_NOT_FOUND_UPSCAN.toString)) shouldBe true
              result.header.status shouldBe NOT_FOUND
            }
          }
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
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("1234")))
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
          withCaptureOfLoggingFrom(logger) {
            logs => {
              val result = controller.initiateCallToUpscan("1234")(fakeRequest)
              status(result) shouldBe INTERNAL_SERVER_ERROR
              logs.exists(_.getMessage.contains(PagerDutyKeys.FAILED_INITIATE_CALL_UPSCAN.toString)) shouldBe true
            }
          }
        }
      }
    }

    "removeFile" must {
      "return NO CONTENT" when {
        "the journey and file exists or doesn't exist in the database" in {
          when(repository.removeFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful((): Unit))
          val result = controller.removeFile("J1234", "F1234")(fakeRequest)
          status(result) shouldBe NO_CONTENT
        }
      }

      "return ISE" when {
        "the repository fails to delete the specified journey-file combination" in {
          when(repository.removeFileForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("Something *spooky* went wrong.")))
          withCaptureOfLoggingFrom(logger) {
            logs => {
              val result = controller.removeFile("J1234", "F1234")(fakeRequest)
              status(result) shouldBe INTERNAL_SERVER_ERROR
              logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_REMOVAL_FAILURE_UPSCAN.toString)) shouldBe true
            }
          }
        }
      }
    }

    "uploadFailure" should {
      "return a BAD_REQUEST when the S3 service doesn't match the expected response model" in {
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = controller.uploadFailure("J1234")(fakeRequest.withFormUrlEncodedBody())
            status(result) shouldBe BAD_REQUEST
            logs.exists(_.getMessage.contains(PagerDutyKeys.UPLOAD_FAILURE_UPSCAN.toString)) shouldBe true
          }
        }
      }

      "return NO_CONTENT" when {
        "the failure is because the file is too small" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("file1")))
          when(repository.getFileIndexForJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(-1))
          val result = controller.uploadFailure("J1234")(fakeRequest.withFormUrlEncodedBody(
            "key" -> "file1",
            "errorCode" -> "EntityTooSmall",
            "errorMessage" -> "Some message about file"
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because the file is too large" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("file1")))
          val result = controller.uploadFailure("J1234")(fakeRequest.withFormUrlEncodedBody(
            "key" -> "file1",
            "errorCode" -> "EntityTooLarge",
            "errorMessage" -> "Some message about file"
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because the file is not specified" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("file1")))
          val result = controller.uploadFailure("J1234")(fakeRequest.withFormUrlEncodedBody(
            "key" -> "file1",
            "errorCode" -> "InvalidArgument",
            "errorMessage" -> "Some message about file"
          ))
          status(result) shouldBe NO_CONTENT
        }

        "the failure is because there is some unknown client error" in {
          when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("file1")))
          val result = controller.uploadFailure("J1234")(fakeRequest.withFormUrlEncodedBody(
            "key" -> "file1",
            "errorCode" -> "ExpiredToken",
            "errorMessage" -> "Some message about file"
          ))
          status(result) shouldBe NO_CONTENT
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
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(controller.filePosted("J1234")(fakeRequest))
            logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_POSTED_FAILURE_UPSCAN.toString)) shouldBe true
            result.header.status shouldBe BAD_REQUEST
          }
        }
      }

      "return No Content and update the file upload when called" in {
        when(repository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        when(repository.updateStateOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some("file1")))
        val result = controller.filePosted("J1234")(FakeRequest("GET", "/file-posted?key=key1&bucket=bucket1"))
        status(result) shouldBe NO_CONTENT
      }
    }

    "preUpscanCheckFailed" should {
      "redirect to the non-JS file upload page" in {
        val result = controller.preUpscanCheckFailed(false, NormalMode)(FakeRequest("GET", "/upscan/file-verification/failed?errorCode=EntityTooLarge"))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      }

      "redirect to the non-JS file upload page - check mode" in {
        val result = controller.preUpscanCheckFailed(false, CheckMode)(FakeRequest("GET", "/upscan/file-verification/change/failed?errorCode=EntityTooLarge"))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(CheckMode).url
      }

      "redirect to the upload list page if requesting another document" in {
        val result = controller.preUpscanCheckFailed(true, NormalMode)(FakeRequest("GET", "/upscan/file-verification/failed?errorCode=EntityTooLarge"))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      }
    }

    "fileVerification" should {
      "show an ISE if the form fails to bind" in {
        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/upscan/file-verification/success?key1=file1")))
            logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_VERIFICATION_FAILURE_UPSCAN.toString)) shouldBe true
            result.header.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "run the block passed to the service if the form binds and run the result" in {
        when(service.waitForStatus(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Ok("")))
        val result = controller.fileVerification(false, NormalMode, false)(FakeRequest("GET", "/upscan/file-verification/success?key=file1").withSession(SessionKeys.journeyId -> "J1234"))
        status(result) shouldBe OK
      }
    }

    "getDuplicateFiles" should {
      "return None when there is no duplicates in the repository" in {
        when(helper.getInsetTextForUploadsInRepository(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.getDuplicateFiles("1234567")(FakeRequest())
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.obj()
      }
      "WarnForDuplicateFiles is enabled" must {
        "return Some when there is duplicates in the repository" in {
            enableFeatureSwitch(WarnForDuplicateFiles)
            when(helper.getInsetTextForUploadsInRepository(ArgumentMatchers.any())(ArgumentMatchers.any()))
              .thenReturn(Future.successful(Some("this is some text to display")))
            val result = controller.getDuplicateFiles("1234567")(FakeRequest())
            status(result) shouldBe OK
            contentAsJson(result) shouldBe Json.obj("message" -> "this is some text to display")
          }
        }
      "WarnForDuplicateFiles is disabled" must {
        "return None when there is duplicates in the repository" in {
          disableFeatureSwitch(WarnForDuplicateFiles)
          when(helper.getInsetTextForUploadsInRepository(ArgumentMatchers.any())(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some("this is some text to display")))
          val result = controller.getDuplicateFiles("1234567")(FakeRequest())
          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj()
        }
       }
      }
    }
  }
}
