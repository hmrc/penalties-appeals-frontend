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
import connectors.httpParsers.UnexpectedFailure
import models.session.UserAnswers
import models.upload._
import models.{CheckMode, NormalMode, UserRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Cookie, Result}
import play.api.test.Helpers._
import services.upscan.UpscanService
import testUtils.{AuthTestModels, UploadData}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.errors.ServiceUnavailablePage
import views.html.reasonableExcuseJourneys.other._
import views.html.reasonableExcuseJourneys.other.noJs._
import viewtils.EvidenceFileUploadsHelper

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class OtherReasonControllerSpec extends SpecBase {
  val whenDidYouBecomeUnablePage: WhenDidBecomeUnablePage = injector.instanceOf[WhenDidBecomeUnablePage]
  val whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage = injector.instanceOf[WhyReturnSubmittedLatePage]
  val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]
  val uploadFirstDocumentPage: UploadFirstDocumentPage = injector.instanceOf[UploadFirstDocumentPage]
  val uploadTakingLongerThanExpectedPage: UploadTakingLongerThanExpectedPage = injector.instanceOf[UploadTakingLongerThanExpectedPage]
  val mockUpscanService: UpscanService = mock(classOf[UpscanService])
  val uploadAnotherDocumentPage: UploadAnotherDocumentPage = injector.instanceOf[UploadAnotherDocumentPage]
  val uploadEvidenceQuestionPage: UploadEvidenceQuestionPage = injector.instanceOf[UploadEvidenceQuestionPage]
  val uploadListPage: UploadListPage = injector.instanceOf[UploadListPage]
  val evidenceFileUploadsHelper: EvidenceFileUploadsHelper = injector.instanceOf[EvidenceFileUploadsHelper]
  val serviceUnavailablePage: ServiceUnavailablePage = injector.instanceOf[ServiceUnavailablePage]
  val mockConfig: Configuration = mock(classOf[Configuration])
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global


  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], previousUpload: Option[Seq[UploadJourney]] = None) {

    reset(mockAuthConnector, mockUpscanService, mockUploadJourneyRepository, mockSessionService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(previousUpload))

    val controller: OtherReasonController = new OtherReasonController(
      whenDidYouBecomeUnablePage,
      whyReturnSubmittedLatePage,
      uploadEvidencePage,
      uploadFirstDocumentPage,
      uploadTakingLongerThanExpectedPage,
      uploadAnotherDocumentPage,
      uploadListPage,
      uploadEvidenceQuestionPage,
      mainNavigator,
      mockUpscanService,
      evidenceFileUploadsHelper,
      mockUploadJourneyRepository,
      serviceUnavailablePage,
      mockSessionService
    )(authPredicate, dataRequiredAction, dataRetrievalAction, appConfig, mockConfig, errorHandler, mcc, ec)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
  }

  "OtherReasonController" should {
    "onPageLoadForWhenDidBecomeUnable" when {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.whenDidBecomeUnable -> "2021-01-01")))))
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenDidBecomeUnable" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to reason page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whenDidBecomeUnable -> LocalDate.of(2021, 2, 1))
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(CheckMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whenDidBecomeUnable -> LocalDate.of(2021, 2, 1))

        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "what",
              "date.month" -> "is",
              "date.year" -> "this"
            )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "123456",
              "date.month" -> "432567",
              "date.year" -> "3124567"
            )))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForUploadEvidence" when {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(false)
        val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult,
        Some(UploadData.oneWaitingUploads)) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(false)
        val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "the user does not have JavaScript enabled" when {
        "return OK when the feature switch is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(false)
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "redirect to page /uploaded-documents when there are previous uploads in NormalMode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(callBackModel))))
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
        }

        "redirect to page /upload-first-document when there are no previous upload NormalMode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(None))
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
        }

        "redirect to page /uploaded-documents when there are previous uploads in CheckMode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(callBackModel))))
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(CheckMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
        }

        "redirect to page /upload-first-document when there are no previous upload CheckMode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(None))
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(CheckMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(CheckMode).url
        }

        "return 303 (SEE_OTHER) when the feature switch is enabled" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhyReturnSubmittedLate" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to file upload when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
            "why-return-submitted-late-text" -> "This is a reason")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whyReturnSubmittedLate -> "This is a reason")
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(CheckMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
            "why-return-submitted-late-text" -> "This is a reason")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.whyReturnSubmittedLate -> "This is a reason")
        }

        "return 400 (BAD_REQUEST) when the user does not enter a reason" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
            "why-return-submitted-late-text" -> "")))
          status(result) shouldBe BAD_REQUEST
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForFirstFileUpload" should {
      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "the user does not have JavaScript enabled" when {

        "return OK" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return BAD_REQUEST and correct view when there is an errorCode in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val request = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge"))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return BAD_REQUEST and correct view when there is an failureMessageFromUpscan in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val request = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile"))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return ISE when the call to initiate file upload fails" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Left(UnexpectedFailure(500, ""))))
          val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeys)
          val content: String = contentAsString(result)

          status(result) shouldBe INTERNAL_SERVER_ERROR
          content.contains("Sorry, the service is unavailable") shouldBe true
          content.contains("We have not saved your answers.") shouldBe true
          content.contains("You will be able to use the service later.") shouldBe true
          content.contains("If you prefer, you can appeal by letter. Write to:") shouldBe true

        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onPageLoadForUploadComplete" should {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(callBackModel))))
          val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view - for duplicate uploads" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(
            callBackModel.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE),
            callBackModel.copy(reference = "ref3", fileStatus = UploadStatusEnum.DUPLICATE)
          ))))
          val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "redirect to first upload page" when {
          "there is no uploads" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(None))
            val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(userRequestWithCorrectKeys)
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
          }

          "there is no successful uploads" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val duplicateCallbackModel = UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType")))
            when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(duplicateCallbackModel))))
            val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(userRequestWithCorrectKeys)
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
          }
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onPageLoadForAnotherFileUpload" should {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result = controller.onPageLoadForAnotherFileUpload(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return BAD_REQUEST and correct view when there is an errorCode in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val request = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge"))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return BAD_REQUEST and correct view when there is an failureMessageFromUpscan in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val request = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile"))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
          val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(request)
          status(result) shouldBe BAD_REQUEST
        }

        "return ISE when the call to initiate file upload fails" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Left(UnexpectedFailure(500, ""))))
          val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "the user is unauthorised" when {
          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "removeFileUpload" should {
        "the user is authorised" must {

          "redirect to the first upload page when the files left is 0" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockUpscanService.removeFileFromJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful((): Unit))
            when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
              .thenReturn(Future.successful(0))
            val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest
              .withFormUrlEncodedBody("fileReference" -> "file1")
            ))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
          }

          "reload the upload list page when the files left is > 0" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockUpscanService.removeFileFromJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful((): Unit))
            when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
              .thenReturn(Future.successful(1))
            val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest
              .withFormUrlEncodedBody("fileReference" -> "file2")
            ))
            status(result) shouldBe SEE_OTHER
          }
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onPageLoadForUploadTakingLongerThanExpected" should {
        "the user is authorised" must {
          "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(userRequestWithCorrectKeys)
            status(result) shouldBe OK
          }
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onSubmitForUploadTakingLongerThanExpected" should {
        "run the block" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockUpscanService.waitForStatus(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future(Ok("")))
          val result = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest.withSession(
            SessionKeys.fileReference -> "F1234")))
          status(result) shouldBe OK
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onSubmitForUploadComplete" should {
        "the user is authorised" must {
          "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
            "- routing to non-JS evidence upload page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "no")))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          }

          "return 400 (BAD_REQUEST) when the user does not enter an option" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
              .thenReturn(Future.successful(0))
            val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "")))
            status(result) shouldBe BAD_REQUEST
          }

          "return 303 (SEE_OTHER) when the files uploaded is 5" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockUploadJourneyRepository.getUploadsForJourney(Some("1234")))
              .thenReturn(Future.successful(Option(Seq(callBackModel, callBackModel, callBackModel, callBackModel, callBackModel))))
            val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "yes")))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url

          }

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }
          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }
    }

    "onPageLoadForUploadEvidenceQuestion" should {
      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no")))))
          val result: Future[Result] = controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForUploadEvidenceQuestion" should {

      "user submits the form" when {
        "the validation is performed against possible values - redirect on success and set the session key value" in
          new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            when(mockSessionService.updateAnswers(answerCaptor.capture()))
              .thenReturn(Future.successful(true))
            val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "yes", "isJsEnabled" -> "true")))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
            answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "yes")
          }

        "the validation is performed against possible values - value does not appear in options list" in
          new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "this_is_fake")))
            status(result) shouldBe BAD_REQUEST
          }

        "the validation is performed against an empty value - value is an empty string" in
          new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestConverter(correctUserAnswers, fakeRequest = fakeRequest
              .withFormUrlEncodedBody("value" -> "")))
            status(result) shouldBe BAD_REQUEST
          }
      }

      "return 500" when {
        "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest
            .withFormUrlEncodedBody("value" -> "no"))
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}