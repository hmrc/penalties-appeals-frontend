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

import config.featureSwitches.{FeatureSwitching, NonJSRouting}
import models.session.UserAnswers
import models.upload._
import models.{NormalMode, PenaltyTypeEnum}
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Cookie, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.AuthStub
import stubs.UpscanStub.successfulInitiateCall
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OtherReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: OtherReasonController = injector.instanceOf[OtherReasonController]
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]

  class Setup(sessionDataToStore: UserAnswers = UserAnswers("1234", Json.obj())) extends UserAnswersSetup(sessionDataToStore) {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  "GET /when-inability-to-manage-account-happened" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /when-inability-to-manage-account-happened" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {

        val noDayJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "",
          "date.month" -> "02",
          "date.year" -> "2025"
        )

        val noMonthJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "02",
          "date.month" -> "",
          "date.year" -> "2025"
        )

        val noYearJsonBody: Seq[(String, String)] = Seq(
          "date.day" -> "02",
          "date.month" -> "02",
          "date.year" -> ""
        )
        val requestNoDay = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").post(""))
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-evidence-for-the-appeal" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      disableFeatureSwitch(NonJSRouting)
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, false)(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      enableFeatureSwitch(NonJSRouting)
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled but the feature switch is enabled" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      enableFeatureSwitch(NonJSRouting)
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, false)(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      disableFeatureSwitch(NonJSRouting)
    }

    "return 200 (OK) when the user does have JavaScript enabled and the feature switch is disabled" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      disableFeatureSwitch(NonJSRouting)
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest))
      request.header.status shouldBe OK
      enableFeatureSwitch(NonJSRouting)
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode, true)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-evidence-for-the-appeal?isJsEnabled=true").get())
      request.status shouldBe SEE_OTHER
    }
  }


  "GET /why-was-the-vat-late" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-was-the-vat-late").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-was-the-vat-late").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /why-was-the-vat-late" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("why-return-submitted-late-text" -> "Other Reason")
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whyReturnSubmittedLate).get shouldBe "Other Reason"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/why-was-the-vat-late").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-was-the-vat-late").post(""))
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-first-document" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 200 (OK) when the users has JS active and the feature switch is enabled" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      enableFeatureSwitch(NonJSRouting)
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
      disableFeatureSwitch(NonJSRouting)
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest.withSession(
        SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-first-document").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /uploaded-documents" should {
    "return OK and correct view" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val callBackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(request) shouldBe OK
    }

    "return OK and the correct view - showing the inset text for duplicate uploads" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val callbackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel.copy(
        reference = "ref2",
        fileStatus = UploadStatusEnum.DUPLICATE),
        isInitiateCall = true))
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(request) shouldBe OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(".govuk-inset-text").text() should startWith("File 1 has the same contents as File 2.")
    }

    "return OK and the correct view - showing the inset text for multiple duplicate uploads" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val callbackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      val callbackModel2: UploadJourney = UploadJourney(
        reference = "ref2",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file2.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1235",
          size = 3
        ))
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel2, isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", callbackModel.copy(
        reference = "ref3",
        fileStatus = UploadStatusEnum.DUPLICATE), isInitiateCall = true)
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel2.copy(
        reference = "ref4",
        fileStatus = UploadStatusEnum.DUPLICATE), isInitiateCall = true)
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(request) shouldBe OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(".govuk-inset-text").text() should startWith("Some of the files have the same contents.")
    }

    "return 303 (SEE_OTHER) when the user has no uploads" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) when the user has no successful uploads" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/uploaded-documents").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-another-document" should {

    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-another-document").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /remove-file-upload" should {
    "return an ISE when the form fails to bind" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = controller.removeFileUpload(NormalMode)(fakeRequest
        .withFormUrlEncodedBody("file" -> "file1"))
      status(request) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to the first document upload page when there is no uploads left" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val request = controller.removeFileUpload(NormalMode)(fakeRequest.withFormUrlEncodedBody("fileReference" -> "file1"))
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "reload the upload list when there is more uploads" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file2", UploadStatusEnum.READY), isInitiateCall = true))
      val request = controller.removeFileUpload(NormalMode)(fakeRequest.withFormUrlEncodedBody("fileReference" -> "file1"))
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }

  }

  "POST /upload-taking-longer-than-expected" should {
    "show an ISE" when {
      "the repository doesn't have a status for this file under this journey" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(SessionKeys.fileReference -> "file1"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect back to the 'upload taking longer than expected' page when the recursive call times out" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "redirect to the non-JS first file upload page when there is an error from Upscan" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest.withSession(
        SessionKeys.fileReference -> "file1",
        SessionKeys.isAddingAnotherDocument -> "false"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the non-JS upload another document page when there is an error from Upscan" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType"))), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest
        .withSession(SessionKeys.fileReference -> "file1",
          SessionKeys.isAddingAnotherDocument -> "true"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the successful upload page when there is no error from Upscan" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY), isInitiateCall = true))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequest
        .withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }
  }

  "POST /uploaded-documents" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file" +
      " upload page when user specifies 'yes'" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file upload" +
      " page when user specifies 'no'" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequest))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/uploaded-documents").post(""))
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-evidence-question" should {
    "return 200 (OK) when the user is authorised" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }
    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-question").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForUploadEvidenceQuestion(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-evidence-question").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /upload-evidence-question" should{
    "return 303 (SEE_OTHER), navigate to check your answer page when the user answers no" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "no"
    }

    "return 303 (SEE_OTHER), navigate to upload evidence and add the session key to the session - when the user answers yes" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes", "isJsEnabled" -> "true")
      val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER), navigate to first upload page and add the session key to the session - when the user answers yes, JS is disabled" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes", "isJsEnabled" -> "false")
      val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, false).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.isUploadEvidence).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new Setup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/upload-evidence-question").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForUploadEvidenceQuestion(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-evidence-question").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}