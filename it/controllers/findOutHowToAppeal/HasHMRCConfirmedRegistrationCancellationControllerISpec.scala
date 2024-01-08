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

package controllers.findOutHowToAppeal

import controllers.testHelpers.AuthorisationTest
import play.api.http.Status
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

class HasHMRCConfirmedRegistrationCancellationControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: HasHMRCConfirmedRegistrationCancellationController = injector.instanceOf[HasHMRCConfirmedRegistrationCancellationController]

  "GET /has-hmrc-confirmed-cancellation" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/has-hmrc-confirmed-cancellation")
        .withSession(authToken -> "1234", SessionKeys.journeyId-> "1234")
      val request = await(controller.onPageLoad()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoad(), "GET", "/has-hmrc-confirmed-cancellation")
  }

  "POST /has-hmrc-confirmed-cancellation" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - when user answers yes" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-hmrc-confirmed-cancellation")
        .withSession(authToken -> "1234", SessionKeys.journeyId -> "1234")
        .withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.findOutHowToAppeal.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.hasHMRCConfirmedRegistrationCancellation).get shouldBe "yes"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/has-hmrc-confirmed-cancellation")
          .withSession(authToken -> "1234", SessionKeys.journeyId -> "1234")
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "invalid option is entered" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidChars: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "blah")
        val result: Result = await(controller.onSubmit()(fakeRequestWithInvalidChars))
        result.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmit(), "POST", "/has-hmrc-confirmed-cancellation")
  }
}
