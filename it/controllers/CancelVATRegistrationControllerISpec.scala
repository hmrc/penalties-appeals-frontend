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

import play.api.http.Status
import play.api.libs.json.{Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}

class CancelVATRegistrationControllerISpec extends IntegrationSpecCommonBase{
  val controller: CancelVATRegistrationController = injector.instanceOf[CancelVATRegistrationController]
  "GET /cancel-vat-registration" should{
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/cancel-vat-registration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
      )
      val request = await(controller.onPageLoadForCancelVATRegistration()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }
    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/cancel-vat-registration")
      val request = await(controller.onPageLoadForCancelVATRegistration()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/cancel-vat-registration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForCancelVATRegistration()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/cancel-vat-registration").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
  "POST /cancel-vat-registration" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration").withSession(
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
            | "value": "yes"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe "#"
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.cancelVATRegistration).get shouldBe "yes"
    }
    "return 303 (SEE_OTHER) to CYA page when appeal IS late and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent -> LocalDateTime.now.minusDays(31).toString)
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "value": "yes"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe "#"
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.cancelVATRegistration).get shouldBe "yes"
    }
    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
        )
        val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
      "the value is invalid" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration").withSession(
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
              | "value": "fake_value"
              |}
              |""".stripMargin)
        )
        val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }
    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration")
      val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/cancel-vat-registration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/cancel-vat-registration").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
