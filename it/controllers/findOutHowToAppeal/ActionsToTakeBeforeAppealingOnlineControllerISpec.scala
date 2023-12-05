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

import config.featureSwitches.{FeatureSwitching, ShowCAFindOutHowToAppealJourney, ShowFindOutHowToAppealLSPJourney}
import controllers.testHelpers.AuthorisationTest
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.{IntegrationSpecCommonBase, SessionKeys}

class ActionsToTakeBeforeAppealingOnlineControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {

  override def afterAll(): Unit = {
    super.afterAll()
    sys.props -= ShowFindOutHowToAppealLSPJourney.name
  }

  val controller: ActionsToTakeBeforeAppealingOnlineController = injector.instanceOf[ActionsToTakeBeforeAppealingOnlineController]

  "GET /actions-to-take-before-appealing-online" should {
    "return 200 (OK) when the user is authorised and ShowFindOutHowToAppealLSPJourney is enabled (LSP)" in new UserAnswersSetup(userAnswers()) {
      enableFeatureSwitch(ShowFindOutHowToAppealLSPJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 200 (OK) when the user is authorised and ShowCAFindOutHowToAppealJourney is enabled (LPP)" in new UserAnswersSetup(userAnswers(Json.obj(SessionKeys.isCaLpp -> true))) {
      disableFeatureSwitch(ShowFindOutHowToAppealLSPJourney)
      enableFeatureSwitch(ShowCAFindOutHowToAppealJourney)
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }

    "return 404 (NOT_FOUND) when the user is authorised but ShowFindOutHowToAppealLSPJourney is disabled" in new UserAnswersSetup(userAnswers()) {
      disableFeatureSwitch(ShowFindOutHowToAppealLSPJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe NOT_FOUND
    }

    "return 404 (NOT_FOUND) when the user is authorised but ShowCAFindOutHowToAppealJourney is disabled" in new UserAnswersSetup(userAnswers(Json.obj(SessionKeys.isCaLpp -> true))) {
      disableFeatureSwitch(ShowCAFindOutHowToAppealJourney)
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe NOT_FOUND
    }
  }
}
