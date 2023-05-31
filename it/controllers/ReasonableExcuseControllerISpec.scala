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

import controllers.testHelpers.AuthorisationTest
import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class ReasonableExcuseControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: ReasonableExcuseController = injector.instanceOf[ReasonableExcuseController]

  "GET /reason-for-missing-deadline" should {
    "return 200 (OK) when the user is authorised and the reasonable excuses can be fetched" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in new UserAnswersSetup(userAnswers(Json.obj())) {
      failedFetchReasonableExcuseListResponse()
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onPageLoad(), "GET", "/reason-for-missing-deadline")
  }

  "POST /reason-for-missing-deadline" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "type1")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HonestyDeclarationController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.reasonableExcuse).get shouldBe "type1"
    }

    "return 400 (BAD REQUEST) when the user POSTs invalid data" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "this_is_fake")

      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.BAD_REQUEST
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in new UserAnswersSetup(userAnswers(Json.obj())) {
      failedFetchReasonableExcuseListResponse()
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onSubmit(), "POST", "/reason-for-missing-deadline")
  }
}
