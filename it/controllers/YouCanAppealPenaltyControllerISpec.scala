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

import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class YouCanAppealPenaltyControllerISpec extends IntegrationSpecCommonBase {
  val controller: YouCanAppealPenaltyController = injector.instanceOf[YouCanAppealPenaltyController]

  "GET /you-can-appeal-this-penalty" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/you-can-appeal-this-penalty").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
    
    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/you-can-appeal-this-penalty").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
  
  "POST /you-can-appeal-this-penalty" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmit()(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.AppealStartController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.youCanAppealThisPenalty).get shouldBe "yes"
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
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the value is invalid" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmit()(fakeRequestInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }
    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/you-can-appeal-this-penalty").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/you-can-appeal-this-penalty").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
