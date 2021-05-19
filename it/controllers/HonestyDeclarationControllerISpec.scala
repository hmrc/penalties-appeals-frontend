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
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

class HonestyDeclarationControllerISpec extends IntegrationSpecCommonBase {
  val controller = injector.instanceOf[HonestyDeclarationController]

  "GET /honesty-declaration" should {
    "return 200 (OK) when the user is authorised and has the correct keys" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/honesty-declaration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.reasonableExcuse, "crime")
      )
      val request = await(controller.onPageLoad()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/honesty-declaration")
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/honesty-declaration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00")
      )
      val request = await(controller.onPageLoad()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/honesty-declaration").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /honesty-declaration" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/honesty-declaration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.reasonableExcuse, "crime")
      ).withJsonBody(Json.parse(
        """
          |{
          | "value": "true"
          |}
          |""".stripMargin))

      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      //TODO: change to when crime happened page
      request.header.headers("Location") shouldBe ""
      request.session(fakeRequestWithCorrectKeys).get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
    }

    "return 400 (BAD REQUEST) when the user POSTs invalid data" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/honesty-declaration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.reasonableExcuse, "crime")
      ).withJsonBody(Json.parse(
        """
          |{
          | "value": "this_is_fake"
          |}
          |""".stripMargin))

      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.BAD_REQUEST
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/honesty-declaration")
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/honesty-declaration").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00")
      )
      val request = await(controller.onSubmit()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/honesty-declaration").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

}
