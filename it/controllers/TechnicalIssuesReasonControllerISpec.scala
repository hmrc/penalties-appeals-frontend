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

import config.featureSwitches.FeatureSwitching
import models.{CheckMode, NormalMode}
import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class TechnicalIssuesReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: TechnicalIssuesReasonController = injector.instanceOf[TechnicalIssuesReasonController]

  "GET /when-did-the-technology-issues-begin" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-technology-issues-begin").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /when-did-the-technology-issues-end" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234"),
        (SessionKeys.whenDidTechnologyIssuesBegin, "2020-01-01")
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-technology-issues-end").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /when-did-the-technology-issues-begin" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - when in check mode" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, LocalDate.now().minusDays(20).toString),
        (SessionKeys.journeyId, "1234")
      ).withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(CheckMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(
        SessionKeys.whenDidTechnologyIssuesBegin).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        ).withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        ).withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is missing a field" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        )

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
        val requestNoDay = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }

    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-begin").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesBegan(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-technology-issues-begin").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /when-did-the-technology-issues-end" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - to CYA when in CheckMode" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, LocalDate.now().minusDays(20).toString),
        (SessionKeys.journeyId, "1234"),
        (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
      ).withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(CheckMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenDidTechnologyIssuesEnd).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - to late appeal when in NormalMode" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, LocalDate.now().minusDays(31).toString),
        (SessionKeys.journeyId, "1234"),
        (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
      ).withFormUrlEncodedBody(
        "date.day" -> "08",
        "date.month" -> "02",
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenDidTechnologyIssuesEnd).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is earlier than the start date" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234"),
          (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
        ).withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "01",
          "date.year" -> "2021"
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234"),
          (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
        ).withFormUrlEncodedBody(
          "date.day" -> "02",
          "date.month" -> "01",
          "date.year" -> "2022"
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe BAD_REQUEST
        setFeatureDate(None)
      }

      "the date submitted is in the future" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234"),
          (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
        ).withFormUrlEncodedBody(
          "date.day" -> "08",
          "date.month" -> "02",
          "date.year" -> "2025"
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234"),
          (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
        )
        val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is missing a field" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234"),
          (SessionKeys.whenDidTechnologyIssuesBegin, "2021-02-01")
        )

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
        val requestNoDay = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }

    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-technology-issues-end").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onSubmitForWhenTechnologyIssuesEnded(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-technology-issues-end").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
