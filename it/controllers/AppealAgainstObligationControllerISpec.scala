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

class AppealAgainstObligationControllerISpec extends IntegrationSpecCommonBase {
  val controller: AppealAgainstObligationController = injector.instanceOf[AppealAgainstObligationController]

  "GET /other-relevant-information" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent-> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId-> "1234")
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/other-relevant-information").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234")
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"))
    )) {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "1234")
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new UserAnswersSetup(userAnswers(Json.obj())) {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/other-relevant-information").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /other-relevant-information" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "1234")
        .withFormUrlEncodedBody("other-relevant-information-text" -> "Other Reason")
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.otherRelevantInformation).get shouldBe "Other Reason"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information")
          .withSession(authToken -> "1234",
            SessionKeys.journeyId -> "1234")
        val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234")
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "1234")
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new UserAnswersSetup(userAnswers(Json.obj())){
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/other-relevant-information").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

}
