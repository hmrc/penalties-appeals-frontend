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

import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

class ReasonableExcuseControllerISpec extends IntegrationSpecCommonBase {
  val controller: ReasonableExcuseController = injector.instanceOf[ReasonableExcuseController]

  "GET /reason-for-missing-deadline" should {
    "return 200 (OK) when the user is authorised and the reasonable excuses can be fetched" in {
      successfulFetchReasonableExcuseResponse
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoad()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in {
      failedFetchReasonableExcuseListResponse()
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoad()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/reason-for-missing-deadline").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /reason-for-missing-deadline" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in {
      successfulFetchReasonableExcuseResponse
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      ).withFormUrlEncodedBody("value" -> "type1")

      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HonestyDeclarationController.onPageLoad().url
      request.session(fakeRequestWithCorrectKeys).get(SessionKeys.reasonableExcuse).get shouldBe "type1"
    }

    "return 400 (BAD REQUEST) when the user POSTs invalid data" in {
      successfulFetchReasonableExcuseResponse
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07"),
        (SessionKeys.dateCommunicationSent, "2020-02-08"),
        (SessionKeys.journeyId, "1234")
      ).withFormUrlEncodedBody("value" -> "this_is_fake")

      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.BAD_REQUEST
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in {
      failedFetchReasonableExcuseListResponse()
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01"),
        (SessionKeys.endDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onSubmit()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/reason-for-missing-deadline").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
