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
import models.NormalMode
import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class CrimeReasonControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: CrimeReasonController = injector.instanceOf[CrimeReasonController]
  "GET /when-did-the-crime-happen" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-crime-happen").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-crime-happen").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-crime-happen").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoadForWhenCrimeHappened(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-crime-happen").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /when-did-the-crime-happen" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
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
        "date.year" -> "2021"
      )
      val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.dateOfCrime).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
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
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is in the future - relative to the time machine" in {
        setFeatureDate(Some(LocalDate.of(2022, 1, 1)))
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
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
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
        setFeatureDate(None)
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is missing a field" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
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
        val requestNoDay = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeys.withFormUrlEncodedBody(noDayJsonBody: _*)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeys.withFormUrlEncodedBody(noMonthJsonBody: _*)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithCorrectKeys.withFormUrlEncodedBody(noYearJsonBody: _*)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-crime-happen").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onSubmitForWhenCrimeHappened(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-crime-happen").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /has-this-crime-been-reported" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-this-crime-been-reported").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-this-crime-been-reported").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-this-crime-been-reported").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoadForHasCrimeBeenReported(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/has-this-crime-been-reported").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /has-this-crime-been-reported" should {
    "return 303 (SEE_OTHER) to CYA page (when appeal is not late) and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, LocalDate.now.minusDays(1).toString),
        (SessionKeys.journeyId, "1234")
      ).withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.hasCrimeBeenReportedToPolice).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER) to CYA page when appeal IS late and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, LocalDate.now.minusDays(31).toString),
        (SessionKeys.journeyId, "1234")
      ).withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.hasCrimeBeenReportedToPolice).get shouldBe "yes"
    }


    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        ).withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
          authToken -> "1234",
          (SessionKeys.penaltyNumber, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01"),
          (SessionKeys.endDateOfPeriod, "2020-01-01"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07"),
          (SessionKeys.dateCommunicationSent, "2020-02-08"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/has-this-crime-been-reported").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onSubmitForHasCrimeBeenReported(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-crime-happen").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
