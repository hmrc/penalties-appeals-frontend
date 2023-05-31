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
import controllers.testHelpers.AuthorisationTest
import models.{NormalMode, PenaltyTypeEnum}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class BereavementReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching with AuthorisationTest {

  val controller: BereavementReasonController = injector.instanceOf[BereavementReasonController]

  "GET /when-did-the-person-die" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhenThePersonDied(NormalMode)(fakeRequest))
      request.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhenThePersonDied(NormalMode), "GET", "/when-did-the-person-die")
  }

  "POST /when-did-the-person-die" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      )
    )) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenDidThePersonDie).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to 'Making a Late appeal page' when appeal is > " +
      "30 days late" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.whenDidThePersonDie).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is missing a field" in new UserAnswersSetup(userAnswers()) {

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
        val requestNoDay = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenThePersonDied(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWhenThePersonDied(NormalMode), "POST", "/when-did-the-person-die")
  }
}

