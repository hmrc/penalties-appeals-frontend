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

import config.featureSwitches.FeatureSwitching
import models.{NormalMode, PenaltyTypeEnum}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class HealthReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: HealthReasonController = injector.instanceOf[HealthReasonController]
  "GET /was-a-hospital-stay-required" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/was-a-hospital-stay-required").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/was-a-hospital-stay-required").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /was-a-hospital-stay-required" should {
    "return 303 (SEE_OTHER), navigate to when did health issue happen and add the session key to the session - when the user answers no" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.wasHospitalStayRequired).get shouldBe "no"
    }

    "return 303 (SEE_OTHER), navigate to when did health issue happen and add the session key to the session - when the user answers yes" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.wasHospitalStayRequired).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/was-a-hospital-stay-required").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/was-a-hospital-stay-required").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /when-did-health-issue-happen" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-health-issue-happen").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-health-issue-happen").get)
      request.status shouldBe Status.SEE_OTHER
    }

  }

  "POST /when-did-health-issue-happen" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now.minusDays(1)
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers(Json.obj(
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
        val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in new UserAnswersSetup(userAnswers(Json.obj(
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

        val requestNoDay = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST

      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-health-issue-happen").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-health-issue-happen").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /has-the-hospital-stay-ended" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not have the start date" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-the-hospital-stay-ended").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "POST /has-the-hospital-stay-ended" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session keys to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(1),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "hasStayEnded" -> "yes",
        "stayEndDate.day" -> "08",
        "stayEndDate.month" -> "02",
        "stayEndDate.year" -> "2021"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueEnded).get shouldBe LocalDate.parse("2021-02-08")
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHealthEventEnded).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "hasStayEnded" -> "no"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHealthEventEnded).get shouldBe "no"
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "stayEndDate.day" -> "08",
          "stayEndDate.month" -> "02",
          "stayEndDate.year" -> "2025"
        )
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      ))) {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "stayEndDate.day" -> "02",
          "stayEndDate.month" -> "01",
          "stayEndDate.year" -> "2022"
        )
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
        setFeatureDate(None)
      }

      "the date submitted is before the start date" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      ))) {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "stayEndDate.day" -> "01",
          "stayEndDate.month" -> "01",
          "stayEndDate.year" -> "2021"
        )
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      ))) {
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
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
        val requestNoDay = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST

      }
    }

    "return 500 (ISE) when the user is authorised but the session does not have a start date" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/has-the-hospital-stay-ended").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/has-the-hospital-stay-ended").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /when-did-hospital-stay-begin" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-hospital-stay-begin").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-hospital-stay-begin").get)
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /when-did-hospital-stay-begin" should {
    "return 303 (SEE_OTHER) to did hospital stay end page and add the session keys to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(1)
    ))) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-hospital-stay-begin").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-hospital-stay-begin").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /when-did-hospital-stay-end" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not have the start date" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-the-hospital-stay-ended").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "POST /when-did-hospital-stay-end" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session keys to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(1),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueEnded).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenHealthIssueEnded).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 500 (ISE) when the user is authorised but the session does not have a start date" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-hospital-stay-end").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-hospital-stay-end").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

}
