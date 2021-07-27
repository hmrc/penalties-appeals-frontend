/*
 * Copyright 2021 HM Revenue & Customs
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

import models.NormalMode
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}

class LossOfStaffReasonControllerISpec extends IntegrationSpecCommonBase {
  val controller = injector.instanceOf[LossOfStaffReasonController]
  
  "GET /when-did-the-person-leave" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-person-leave").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
      )
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-person-leave")
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-did-the-person-leave").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-person-leave").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /when-did-the-person-leave" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to CYA page when appeal is < " +
      "30 days late" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, LocalDateTime.now().minusDays(20).toString)
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "date.day": "08",
            | "date.month": "02",
            | "date.year": "2021"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenPersonLeftTheBusiness).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to 'Making a Late appeal page' when appeal is > " +
      "30 days late" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "date.day": "08",
            | "date.month": "02",
            | "date.year": "2021"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenPersonLeftTheBusiness).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
        ).withJsonBody(
          Json.parse(
            """
              |{
              | "date.day": "08",
              | "date.month": "02",
              | "date.year": "2025"
              |}
              |""".stripMargin)
        )
        val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
        )
        val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
        )

        val noDayJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "",
            | "date.month": "02",
            | "date.year": "2025"
            |}
            |""".stripMargin
        )

        val noMonthJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "02",
            | "date.month": "",
            | "date.year": "2025"
            |}
            |""".stripMargin
        )

        val noYearJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "02",
            | "date.month": "02",
            | "date.year": ""
            |}
            |""".stripMargin
        )
        val requestNoDay = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noDayJsonBody)))
        requestNoDay.header.status shouldBe BAD_REQUEST

        val requestNoMonth = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noMonthJsonBody)))
        requestNoMonth.header.status shouldBe BAD_REQUEST

        val requestNoYear = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noYearJsonBody)))
        requestNoYear.header.status shouldBe BAD_REQUEST
      }

    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave")
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-did-the-person-leave").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-did-the-person-leave").post(""))
      request.status shouldBe SEE_OTHER
    }
  }
}
