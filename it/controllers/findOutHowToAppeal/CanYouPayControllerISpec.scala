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

import config.featureSwitches.{FeatureSwitching, ShowFindOutHowToAppealJourney}
import controllers.testHelpers.AuthorisationTest
import play.api.http.Status
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.{IntegrationSpecCommonBase, SessionKeys}

class CanYouPayControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {

  val controller: CanYouPayController = injector.instanceOf[CanYouPayController]

  "GET /can-you-pay" should {
    "return 200 (OK) when the user is authorised and feature switch is enabled" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.vatAmount -> BigDecimal(123.45),
      SessionKeys.principalChargeReference -> "123456789",
      SessionKeys.isCaLpp -> false
    ))) {
      enableFeatureSwitch(ShowFindOutHowToAppealJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 404 (NOT_FOUND) when the user is authorised but the feature switch is disabled" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.vatAmount -> BigDecimal(123.45),
      SessionKeys.principalChargeReference -> "123456789",
      SessionKeys.isCaLpp -> false
    ))) {
      disableFeatureSwitch(ShowFindOutHowToAppealJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe NOT_FOUND
    }
  }

  "POST /can-you-pay" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct with no" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.vatAmount -> BigDecimal(123.45),
      SessionKeys.principalChargeReference -> "123456789",
      SessionKeys.isCaLpp -> false
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.AppealAfterPaymentPlanSetUpController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.willUserPay).get shouldBe "no"
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct with yes" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.vatAmount -> BigDecimal(123.45),
      SessionKeys.principalChargeReference -> "123456789",
      SessionKeys.isCaLpp -> false
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.AppealAfterVATIsPaidController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.willUserPay).get shouldBe "yes"
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct with paid" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "paid")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.IfYouvePaidYourVATController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.willUserPay).get shouldBe "paid"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.vatAmount -> BigDecimal(123.45),
        SessionKeys.principalChargeReference -> "123456789",
        SessionKeys.isCaLpp -> false
      ))) {
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "the value is invalid" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.vatAmount -> BigDecimal(123.45),
        SessionKeys.principalChargeReference -> "123456789",
        SessionKeys.isCaLpp -> false
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "fake_value")
        val request = await(controller.onSubmit()(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }
  }
}
