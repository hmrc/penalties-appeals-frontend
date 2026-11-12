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

import controllers.testHelpers.AuthorisationTest
import models.NormalMode
import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

class AgentsControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: AgentsController = injector.instanceOf[AgentsController]

  "GET /what-caused-you-to-miss-the-deadline" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode), "GET", "/what-caused-you-to-miss-the-deadline")
  }

  "POST /what-caused-you-to-miss-the-deadline" should {
    "return 303 (SEE_OTHER) to 'reasonable excuse selection' page and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "client")
      val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).get shouldBe "client"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }
    runControllerPredicateTests(controller.onSubmitForWhatCausedYouToMissTheDeadline(NormalMode), "POST", "/what-caused-you-to-miss-the-deadline")
  }

  "GET /who-planned-to-submit-vat-return" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode), "GET", "/who-planned-to-submit-vat-return")

  }

  "POST /who-planned-to-submit-vat-return" should {
    "return 303 (SEE_OTHER) to who planned to submit vat return page and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "client")
      val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).get shouldBe "client"
    }

    "return 400 (BAD_REQUEST)" when {
      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request = await(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode)(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForWhoPlannedToSubmitVATReturn(NormalMode), "POST", "/who-planned-to-submit-vat-return")
  }
}
