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
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.mvc.Result
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.IntegrationSpecCommonBase

class OtherWaysToAppealControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {

  val controller: OtherWaysToAppealController = injector.instanceOf[OtherWaysToAppealController]

  "GET /other-ways-to-appeal" should {
    "return 200 (OK) when the user is authorised and feature switch is enabled" in new UserAnswersSetup(userAnswers()) {
      enableFeatureSwitch(ShowFindOutHowToAppealJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 404 (NOT_FOUND) when the user is authorised but the feature switch is disabled" in new UserAnswersSetup(userAnswers()) {
      disableFeatureSwitch(ShowFindOutHowToAppealJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe NOT_FOUND
    }
  }

}
