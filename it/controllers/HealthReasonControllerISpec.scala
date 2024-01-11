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
import models.NormalMode
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class HealthReasonControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {
  val controller: HealthReasonController = injector.instanceOf[HealthReasonController]
  "GET /was-a-hospital-stay-required" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWasHospitalStayRequired(NormalMode), "GET", "/was-a-hospital-stay-required")
  }

  "POST /was-a-hospital-stay-required" should {
    "return 303 (SEE_OTHER), navigate to when did health issue happen and add the session key to the session - when the user answers no" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.wasHospitalStayRequired).get shouldBe "no"
    }

    "return 303 (SEE_OTHER), navigate to when did health issue happen and add the session key to the session - when the user answers yes" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.wasHospitalStayRequired).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWasHospitalStayRequired(NormalMode), "POST", "/was-a-hospital-stay-required")
  }

  "GET /when-did-health-issue-happen" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhenHealthReasonHappened(NormalMode), "GET", "/when-did-health-issue-happen")
  }

  "POST /when-did-health-issue-happen" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      )
    )) {
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

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
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
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new UserAnswersSetup(userAnswers()) {
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

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
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

        val requestNoDay = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST

      }
    }

    runControllerPredicateTests(controller.onSubmitForWhenHealthReasonHappened(NormalMode), "POST", "/when-did-health-issue-happen")
  }

  "GET /has-the-hospital-stay-ended" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForHasHospitalStayEnded(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForHasHospitalStayEnded(NormalMode), "GET", "/has-the-hospital-stay-ended")
  }

  "POST /has-the-hospital-stay-ended" should {

    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and " +
      "add the session keys to the session when the body is correct and user clicks no" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      )
    )) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "hasStayEnded" -> "no"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHealthEventEnded).get shouldBe "no"
    }

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and " +
      "add the session key to the session when the body is correct and user clicks no" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "hasStayEnded" -> "no"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHealthEventEnded).get shouldBe "no"
    }

    "return 303 (SEE_OTHER) to when did hospital stay end page and " +
      "add the session keys to the session when the body is correct and user clicks yes" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
        "hasStayEnded" -> "yes"
      )
      val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHealthEventEnded).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "stayEndDate.day" -> "08",
          "stayEndDate.month" -> "02",
          "stayEndDate.year" -> "2025"
        )
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in new UserAnswersSetup(userAnswers()) {
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

      "the date submitted is before the start date" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody(
          "stayEndDate.day" -> "01",
          "stayEndDate.month" -> "01",
          "stayEndDate.year" -> "2021"
        )
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest))
        request.header.status shouldBe BAD_REQUEST
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
        val requestNoDay = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForHasHospitalStayEnded(NormalMode)(fakeRequest.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST

      }
    }

    runControllerPredicateTests(controller.onSubmitForHasHospitalStayEnded(NormalMode), "POST", "/has-the-hospital-stay-ended")
  }

  "GET /when-did-hospital-stay-begin" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode), "GET", "/when-did-hospital-stay-begin")
  }

  "POST /when-did-hospital-stay-begin" should {
    "return 303 (SEE_OTHER) to did hospital stay end page and add the session keys to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
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

    runControllerPredicateTests(controller.onSubmitForWhenDidHospitalStayBegin(NormalMode), "POST", "/when-did-hospital-stay-begin")
  }

  "GET /when-did-hospital-stay-end" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      )
    )) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not have the start date" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe routes.InternalServerErrorController.onPageLoad().url
    }

    runControllerPredicateTests(controller.onPageLoadForWhenDidHospitalStayEnd(NormalMode), "GET", "/when-did-hospital-stay-end")
  }

  "POST /when-did-hospital-stay-end" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session keys to the session when the body is correct" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20),
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      )
    )) {
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

    "return 303 (SEE_OTHER) to making a late appeal page when appeal IS late and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2020-01-01")
      )
    )) {
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

    "return 500 (ISE) when the user is authorised but the session does not have a start date" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode)(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe routes.InternalServerErrorController.onPageLoad().url
    }

    runControllerPredicateTests(controller.onSubmitForWhenDidHospitalStayEnd(NormalMode), "POST", "/when-did-hospital-stay-end")
  }

}
