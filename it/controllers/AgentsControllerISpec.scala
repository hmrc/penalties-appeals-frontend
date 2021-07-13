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
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDateTime

class AgentsControllerISpec extends IntegrationSpecCommonBase {
  val controller: AgentsController = injector.instanceOf[AgentsController]
  
  "GET /why-return-was-submitted-late-agent" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-return-was-submitted-late-agent").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
      )
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-return-was-submitted-late-agent")
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-return-was-submitted-late-agent").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-return-was-submitted-late-agent").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /why-return-was-submitted-late-agent" should {
    "return 303 (SEE_OTHER) to reasonable excuse selection page and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/why-return-was-submitted-late-agent").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, LocalDateTime.now.minusDays(1).toString)
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "value": "client"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      //TODO: change to Reasonable excuse selection page
      request.header.headers("Location") shouldBe "#"
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.causeOfLateSubmissionAgent).get shouldBe "client"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/why-return-was-submitted-late-agent").withSession(
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
        val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/why-return-was-submitted-late-agent").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
        )
        val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/why-return-was-submitted-late-agent")
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/why-return-was-submitted-late-agent").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-return-was-submitted-late-agent").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
  "GET /who-planned-to-submit-vat-return" should {
    "return 200 (OK) when the user is authorised" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/who-planned-to-submit-vat-return").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00")
      )
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/who-planned-to-submit-vat-return")
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/who-planned-to-submit-vat-return").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/who-planned-to-submit-vat-return").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /who-planned-to-submit-vat-return" should {
    "return 303 (SEE_OTHER) to who planned to submit vat return page and add the session key to the session when the body is correct" in {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, LocalDateTime.now.minusDays(1).toString)
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "value": "client"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      //TODO: change to Reasonable excuse selection page
      request.header.headers("Location") shouldBe "#"
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whoPlannedToSubmitVATReturn).get shouldBe "client"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return").withSession(
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
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00")
        )
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return")
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/who-planned-to-submit-vat-return").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/who-planned-to-submit-vat-return").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
