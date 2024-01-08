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
import play.api.http.Status.OK
import play.api.mvc.{AnyContent, AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.{IntegrationSpecCommonBase, SessionKeys}

class AppealAfterVATIsPaidControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {

  val controller: AppealAfterVATIsPaidController = injector.instanceOf[AppealAfterVATIsPaidController]

  "GET /you-can-appeal-online-after-you-pay" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }
  }

  "POST /you-can-appeal-online-after-you-pay" when {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val request: Result = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.findOutHowToAppeal.routes.OtherWaysToAppealController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.doYouWantToPayNow).get shouldBe "no"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val request: Result = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the value is invalid" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request: Result = await(controller.onSubmit()(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }
  }
}
