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

class MakingALateAppealControllerISpec extends IntegrationSpecCommonBase {
  val controller: MakingALateAppealController = injector.instanceOf[MakingALateAppealController]

  "GET /making-a-late-appeal" should {
    "return 200 (OK) when the user is authorised and has the correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/making-a-late-appeal").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/making-a-late-appeal").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /making-a-late-appeal" should {
    "return 303 (SEE OTHER) when the user POSTs non-empty data -  adding the key to the session" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("late-appeal-text" -> "This is a great reason.")

      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.lateAppealReason).get shouldBe "This is a great reason."
    }

    "return 400 (BAD REQUEST) when the user POSTs EMPTY data" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val fakeRequestWithEmptyBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("late-appeal-text" -> "")

      val request = await(controller.onSubmit()(fakeRequestWithEmptyBody))
      request.header.status shouldBe Status.BAD_REQUEST
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/making-a-late-appeal").withSession(
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
      val request = await(buildClientForRequestToApp(uri = "/making-a-late-appeal").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
