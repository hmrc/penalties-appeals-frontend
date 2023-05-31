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

class CrimeReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching with AuthorisationTest {
  val controller: CrimeReasonController = injector.instanceOf[CrimeReasonController]
  "GET /when-did-the-crime-happen" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhenCrimeHappened(NormalMode), "GET", "/when-did-the-crime-happen")
  }

  "POST /when-did-the-crime-happen" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[LocalDate](SessionKeys.dateOfCrime).get shouldBe LocalDate.parse("2021-02-08")
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new UserAnswersSetup(userAnswers()) {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest))
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
        val requestNoDay = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWhenCrimeHappened(NormalMode), "POST", "/when-did-the-crime-happen")
  }

  "GET /has-this-crime-been-reported" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForHasCrimeBeenReported(NormalMode), "GET", "/has-this-crime-been-reported")
  }

  "POST /has-this-crime-been-reported" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      )
    )) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasCrimeBeenReportedToPolice).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER) to CYA page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasCrimeBeenReportedToPolice).get shouldBe "yes"
    }


    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForHasCrimeBeenReported(NormalMode), "POST", "/has-this-crime-been-reported")
  }
}
