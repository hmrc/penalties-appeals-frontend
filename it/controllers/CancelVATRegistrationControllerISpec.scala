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

import controllers.findOutHowToAppeal.CancelVATRegistrationController
import controllers.testHelpers.AuthorisationTest
import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

class CancelVATRegistrationControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: CancelVATRegistrationController = injector.instanceOf[CancelVATRegistrationController]
  "GET /cancel-vat-registration" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForCancelVATRegistration()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForCancelVATRegistration(), "GET", "/cancel-vat-registration")
  }

  "POST /cancel-vat-registration" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.findOutHowToAppeal.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.cancelVATRegistration).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForCancelVATRegistration()(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForCancelVATRegistration(), "POST", "/cancel-vat-registration")
  }
}
