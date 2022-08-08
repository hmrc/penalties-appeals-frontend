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

class AgentsControllerISpec extends IntegrationSpecCommonBase {
  val controller: AgentsController = injector.instanceOf[AgentsController]

  "GET /what-caused-you-to-miss-the-deadline" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/what-caused-you-to-miss-the-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/what-caused-you-to-miss-the-deadline").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /what-caused-you-to-miss-the-deadline" should {
    "return 303 (SEE_OTHER) to 'reasonable excuse selection' page and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now.minusDays(1)
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "client")
      val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).get shouldBe "client"
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
        val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithInvalidBody))
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
        val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/what-caused-you-to-miss-the-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/what-caused-you-to-miss-the-deadline").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
  "GET /who-planned-to-submit-vat-return" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/who-planned-to-submit-vat-return").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/who-planned-to-submit-vat-return").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /who-planned-to-submit-vat-return" should {
    "return 303 (SEE_OTHER) to who planned to submit vat return page and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now.minusDays(1)
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "client")
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).get shouldBe "client"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now.minusDays(1)
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now.minusDays(1)
      ))) {
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/who-planned-to-submit-vat-return").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
