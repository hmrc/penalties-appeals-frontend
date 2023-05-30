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
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class MakingALateAppealControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
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

    runControllerAuthorisationTests(controller.onPageLoad(), "GET", "/making-a-late-appeal")
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

    "return 400 (BAD REQUEST) when the user POSTs invalid characters data" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val fakeRequestWithInvalidChars: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("late-appeal-text" -> "コし")

      val result: Result = await(controller.onSubmit()(fakeRequestWithInvalidChars))
      result.header.status shouldBe Status.BAD_REQUEST
    }

    runControllerAuthorisationTests(controller.onSubmit(), "POST", "/making-a-late-appeal")

  }
}
