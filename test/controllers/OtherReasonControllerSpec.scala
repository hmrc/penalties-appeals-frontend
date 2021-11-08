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
import connectors.httpParsers.UpscanInitiateHttpParser.UnexpectedFailure
import models.upload.{UploadFormTemplateRequest, UploadJourney, UploadStatusEnum, UpscanInitiateResponseModel}
import models.{CheckMode, NormalMode, UserRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.{Cookie, Result}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import services.upscan.UpscanService
import testUtils.{AuthTestModels, UploadData}
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.components.upload.YouHaveUploadedFilesPage
import views.html.reasonableExcuseJourneys.other._
import views.html.reasonableExcuseJourneys.other.noJs._
import viewtils.EvidenceFileUploadsHelper

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class OtherReasonControllerSpec extends SpecBase {
  val whenDidYouBecomeUnablePage: WhenDidBecomeUnablePage = injector.instanceOf[WhenDidBecomeUnablePage]
  val whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage = injector.instanceOf[WhyReturnSubmittedLatePage]
  val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]
  val uploadFirstDocumentPage: UploadFirstDocumentPage = injector.instanceOf[UploadFirstDocumentPage]
  val uploadTakingLongerThanExpectedPage: UploadTakingLongerThanExpectedPage = injector.instanceOf[UploadTakingLongerThanExpectedPage]
  val mockUpscanService: UpscanService = mock(classOf[UpscanService])
  val uploadAnotherDocumentPage: UploadAnotherDocumentPage = injector.instanceOf[UploadAnotherDocumentPage]
  val youHaveUploadedFilesPage: YouHaveUploadedFilesPage = injector.instanceOf[YouHaveUploadedFilesPage]
  val evidenceFileUploadsHelper: EvidenceFileUploadsHelper = injector.instanceOf[EvidenceFileUploadsHelper]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global


  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]],
    Option[ItmpAddress]]], previousUpload: Option[Seq[UploadJourney]] = None) {

    reset(mockAuthConnector, mockUpscanService, mockUploadJourneyRepository)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
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
      youHaveUploadedFilesPage,
      mainNavigator,
      mockUpscanService,
      mockUploadJourneyRepository,
      evidenceFileUploadsHelper
    )(authPredicate, dataRequiredAction, appConfig, errorHandler, mcc, ec)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(
      2020, 2, 1, 0, 0, 0))
  }

  "OtherReasonController" should {
    "onPageLoadForWhenDidBecomeUnable" when {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(
          fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.whenDidBecomeUnable -> "2021-01-01")))
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
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
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.of(
            2021, 2, 1).toString
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          await(result).session.get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.of(
            2021, 2, 1).toString
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": "what",
                  | "date.month": "is",
                  | "date.year": "this"
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 31,
                  | "date.month": 2,
                  | "date.year": 2021
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 124356,
                  | "date.month": 432567,
                  | "date.year": 3124567
                  |}
                  |""".stripMargin))))
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
        val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(userRequestWithCorrectKeysAndJS)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(
        AuthTestModels.successfulAuthResult, Some(UploadData.oneWaitingUploads)
      ) {
        val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeysAndJS))
        status(result) shouldBe OK
      }

      "the user does not have JavaScript enabled" when {

        "return 303 (SEE_OTHER)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhyReturnSubmittedLate" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to file upload when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "why-return-submitted-late-text": "This is a reason"
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
          await(result).session.get(SessionKeys.whyReturnSubmittedLate).get shouldBe "This is a reason"
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "why-return-submitted-late-text": "This is a reason"
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
          await(result).session.get(SessionKeys.whyReturnSubmittedLate).get shouldBe "This is a reason"
        }

        "return 400 (BAD_REQUEST) when the user does not enter a reason" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "why-return-submitted-late-text": ""
                |}
                |""".stripMargin))))
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
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return SEE_OTHER and correct view when JS is enabled" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeysAndJS)
        status(result) shouldBe SEE_OTHER
      }

      "return BAD_REQUEST and correct view when there is an errorCode in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val fakeRequest = UserRequest(vrn)(fakeRequestWithCorrectKeys.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge"))
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest)
        status(result) shouldBe BAD_REQUEST
      }

      "return BAD_REQUEST and correct view when there is an failureMessageFromUpscan in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val fakeRequest = UserRequest(vrn)(fakeRequestWithCorrectKeys.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile"))
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest)
        status(result) shouldBe BAD_REQUEST
      }

      "return ISE when the call to initiate file upload fails" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Left(UnexpectedFailure(500, ""))))
        val result: Future[Result] = controller.onPageLoadForFirstFileUpload(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
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
        when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(1))
        val result: Future[Result] = controller.onPageLoadForUploadComplete(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
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
      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult){
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result = controller.onPageLoadForAnotherFileUpload(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return BAD_REQUEST and correct view when there is an errorCode in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val fakeRequest = UserRequest(vrn)(fakeRequestWithCorrectKeys.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge"))
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest)
        status(result) shouldBe BAD_REQUEST
      }

      "return BAD_REQUEST and correct view when there is an failureMessageFromUpscan in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val fakeRequest = UserRequest(vrn)(fakeRequestWithCorrectKeys.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile"))
        when(mockUpscanService.initiateSynchronousCallToUpscan(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right(UpscanInitiateResponseModel("file1ref", UploadFormTemplateRequest("/", Map.empty)))))
        val result: Future[Result] = controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest)
        status(result) shouldBe BAD_REQUEST
      }

      "return ISE when the call to initiate file upload fails" in new Setup(AuthTestModels.successfulAuthResult) {
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
        "redirect to multi-file upload page when the JS enabled cookie is present" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "fileReference": "file1"
                |}
                |""".stripMargin)
          ).withCookies(Cookie("jsenabled", "true")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
        }

        "redirect to the first upload page when the files left is 0" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockUpscanService.removeFileFromJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful((): Unit))
          when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
            .thenReturn(Future.successful(0))
          val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "fileReference": "file1"
                |}
                |""".stripMargin)
          ))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
        }

        "reload the upload list page when the files left is > 0" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockUpscanService.removeFileFromJourney(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful((): Unit))
          when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
            .thenReturn(Future.successful(1))
          val result: Future[Result] = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "fileReference": "file2"
                |}
                |""".stripMargin)
          ))
          status(result) shouldBe SEE_OTHER //TODO 'SEE_OTHER' to be replaced by 'OK' under appropriate routing
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
          val result: Future[Result] = controller.onPageLoadForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeys)
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
        when(mockUpscanService.waitForStatus(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(Ok("")))
        val result = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.journeyId -> "J1234",
          SessionKeys.fileReference -> "F1234"))
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
          val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "value": "no"
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.nextFileUpload).get shouldBe "no"
        }

        "return 400 (BAD_REQUEST) when the user does not enter an option" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockUpscanService.getAmountOfFilesUploadedForJourney(ArgumentMatchers.any())(ArgumentMatchers.any()))
            .thenReturn(Future.successful(0))
          val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "value": ""
                |}
                |""".stripMargin))))
          status(result) shouldBe BAD_REQUEST
        }

        "return 303 (SEE_OTHER) when the files uploaded is 5" in new Setup(AuthTestModels.successfulAuthResult) {
          val s1 = UploadJourney("ref1",UploadStatusEnum.READY,None,None,None,LocalDateTime.now())
          when(mockUploadJourneyRepository.getUploadsForJourney(any())).thenReturn(Future.successful(Some(Seq(s1,s1,s1,s1,s1))))

          val result: Future[Result] = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "value": "yes"
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER

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
}