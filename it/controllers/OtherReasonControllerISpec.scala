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
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class OtherReasonControllerISpec extends IntegrationSpecCommonBase {
  val controller: OtherReasonController = injector.instanceOf[OtherReasonController]
  "GET /when-inability-to-manage-account-happened" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
      )
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened")
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /when-inability-to-manage-account-happened" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
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
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe "" //TODO change to reason page url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
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
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
        )
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the date submitted is missing a field" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
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
        val requestNoDay = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noDayJsonBody)))
        requestNoDay.header.status shouldBe Status.BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noMonthJsonBody)))
        requestNoMonth.header.status shouldBe Status.BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noYearJsonBody)))
        requestNoYear.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened")
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}