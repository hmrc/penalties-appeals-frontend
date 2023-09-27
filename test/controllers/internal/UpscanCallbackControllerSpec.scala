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

package controllers.internal

import java.time.LocalDateTime

import base.SpecBase
import com.typesafe.config.Config
import models.upload._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, times, verify, when}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import services.upscan.UpscanService

import scala.concurrent.{ExecutionContext, Future}

class UpscanCallbackControllerSpec extends SpecBase {
  val mockConfiguration: Configuration = mock(classOf[Configuration])
  val mockConfig: Config = mock(classOf[Config])
  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]
  val mockRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val service: UpscanService = injector.instanceOf[UpscanService]
  val controller: UpscanCallbackController = new UpscanCallbackController(mockRepository, service)(mcc, ec)

  val validJsonButInvalidModel: JsValue = Json.parse(
    """
      |{
      | "not": "recognised"
      |}
      |
      |""".stripMargin)

  val validCallbackFromUpscan: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref1",
      |    "downloadUrl": "download.url",
      |    "fileStatus": "READY",
      |    "uploadDetails": {
      |        "fileName": "file1.txt",
      |        "fileMimeType": "text/plain",
      |        "uploadTimestamp": "2018-04-24T09:30:00Z",
      |        "checksum": "check12345678",
      |        "size": 987
      |    },
      |    "uploadFields": {
      |       "key": "abcxyz",
      |      "algo": "md5"
      |    }
      |}
      |""".stripMargin
  )

  val callbackFromUpscanWithFailure: JsValue = Json.parse(
    """
      |{
      | "reference": "ref1",
      | "fileStatus": "FAILED",
      | "failureDetails": {
      |   "failureReason": "REJECTED",
      |   "message": "this file was rejected"
      | }
      |}
      |""".stripMargin
  )

  val uploadJourneyModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
      checksum = "check12345678",
      size = 987
    )),
    uploadFields = Some(Map(
        "key" -> "abcxyz",
        "algo" -> "md5"
    ))
  )

  val uploadJourneyModelWithFailure: UploadJourney = uploadJourneyModel.copy(
    fileStatus = UploadStatusEnum.FAILED, downloadUrl = None, uploadDetails = None,
    failureDetails = Some(FailureDetails(
      failureReason = FailureReasonEnum.REJECTED,
      message = "upscan.invalidMimeType"
    ))
  )

  val uploadFieldsForUpdateCall: Map[String, String] = Map(
    "key" -> "abcxyz",
    "algo" -> "md5"
  )

  class Setup {
    val journeyCaptor: ArgumentCaptor[UploadJourney] = ArgumentCaptor.forClass(classOf[UploadJourney])
    reset(mockRepository)
    reset(mockConfiguration)
    reset(mockConfig)
    when(mockConfiguration.underlying).thenReturn(mockConfig)
    when(mockRepository.updateStateOfFileUpload(ArgumentMatchers.eq("12345"), journeyCaptor.capture(), any())).thenReturn(Future.successful(Some("id")))
    when(mockRepository.getFieldsForFileReference(ArgumentMatchers.eq("12345"), any())).thenReturn(Future.successful(Some(Map("key" -> "yek"))))
  }

  "UpscanController" should {
    "return an ISE" when {
      s"the body can not be parsed to an $UploadJourney model" in {
        val result: Future[Result] = controller.callbackFromUpscan("12345", isJsEnabled = true)(fakeRequest.withBody(validJsonButInvalidModel))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return NO CONTENT" when {

      "the body is valid and state has been updated" in new Setup {
        val result: Result = await(controller.callbackFromUpscan("12345", isJsEnabled = true)(fakeRequest.withBody(validCallbackFromUpscan)))
        result.header.status shouldBe NO_CONTENT
        verify(mockRepository, times(1)).getFieldsForFileReference(ArgumentMatchers.eq("12345"), any())
        verify(mockRepository, times(1)).updateStateOfFileUpload(ArgumentMatchers.eq("12345"), any(), any())
        journeyCaptor.getValue.fileStatus shouldBe UploadStatusEnum.READY
      }

      "the file is rejected and state has been updated" in new Setup  {
        val result: Result = await(controller.callbackFromUpscan("12345", isJsEnabled = true)(fakeRequest.withBody(callbackFromUpscanWithFailure)))
        result.header.status shouldBe NO_CONTENT
        verify(mockRepository, times(0)).getFieldsForFileReference(ArgumentMatchers.eq("12345"), any())
        verify(mockRepository, times(1)).updateStateOfFileUpload(ArgumentMatchers.eq("12345"), any(), any())
        journeyCaptor.getValue.fileStatus shouldBe UploadStatusEnum.FAILED
      }
    }
  }
}
