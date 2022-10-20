/*
 * Copyright 2022 HM Revenue & Customs
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
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import services.upscan.UpscanService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys
import views.html.reasonableExcuseJourneys.other.RemoveFilePage

import scala.concurrent.{ExecutionContext, Future}

class RemoveFileControllerSpec extends SpecBase with LogCapturing {
  val removeFilePage: RemoveFilePage = injector.instanceOf[RemoveFilePage]
  val mockUpscanService: UpscanService = mock(classOf[UpscanService])
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector, mockUpscanService, mockSessionService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    val controller: RemoveFileController = new RemoveFileController(mockUpscanService, errorHandler,
      removeFilePage)(mcc, appConfig, authPredicate, dataRequiredAction, dataRetrievalAction, ec)
  }

  "onPageLoad" when {

    "the user is authorised" should {
      "show the page when a file name is retrieved based on the file reference and journey id" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockUpscanService.getFileNameForJourney(any(), any())(any())).thenReturn(Future.successful(Some("file123.txt")))
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "show ISE when the file reference does not resolve to a file name" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockUpscanService.getFileNameForJourney(any(), any())(any())).thenReturn(Future.successful(None))
        val result = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "show ISE when a name retrieval fails" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockUpscanService.getFileNameForJourney(any(), any())(any())).thenReturn(Future.failed(new Exception("something went wrong :(")))
        val result = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" should {
      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmit" when {

    "the user is authorised" should {
      "redirect back to the JS enabled page when the isJsEnabled parameter is true" when {
        "the user clicks no" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result = controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "no")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
        }

        "the user clicks yes - removing the file" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.removeFileFromJourney(any(), any())).thenReturn(Future.successful((): Unit))
          val result = controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "yes")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
        }
      }

      "redirect back to the upload list page when the isJsEnabled parameter is false" when {
        "the user clicks no" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.getAmountOfFilesUploadedForJourney(any())(any())).thenReturn(Future.successful(1))
          val result = controller.onSubmit(fileReference = "fileref123", isJsEnabled = false, mode = NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "no")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
        }

        "the user clicks yes - removing the file" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.removeFileFromJourney(any(), any())).thenReturn(Future.successful((): Unit))
          when(mockUpscanService.getAmountOfFilesUploadedForJourney(any())(any())).thenReturn(Future.successful(1))
          val result = controller.onSubmit(fileReference = "fileref123", isJsEnabled = false, mode = NormalMode)(
            fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "yes")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
        }
      }

      "show an error when the user does not select an option" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockUpscanService.getFileNameForJourney(any(), any())(any())).thenReturn(Future.successful(Some("file123.txt")))
        val result = controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(
          fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "")))
        status(result) shouldBe BAD_REQUEST
      }

      "show an ISE" when {
        "the file could not be removed" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.removeFileFromJourney(any(), any())).thenReturn(Future.failed(new Exception("broken :(")))
          withCaptureOfLoggingFrom(logger) {
            logs => {
              val result = await(controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(
                fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "yes"))))
               logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_REMOVAL_FAILURE_UPSCAN.toString)) shouldBe true
              result.header.status shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }

        "the file name can not be retrieved - on form error" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.getFileNameForJourney(any(), any())(any())).thenReturn(Future.failed(new Exception("this is an exception :)")))
          withCaptureOfLoggingFrom(logger) {
            logs => {
              val result = await(controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(
                fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "what"))))
              logs.exists(_.getMessage.contains(PagerDutyKeys.FILE_NAME_RETRIEVAL_FAILURE_UPSCAN.toString)) shouldBe true
              result.header.status shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

    }

    "the user is unauthorised" should {
      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmit(fileReference = "fileref123", isJsEnabled = true, mode = NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
