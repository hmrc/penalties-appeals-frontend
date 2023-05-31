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
import models.PenaltyTypeEnum
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class YouCannotAppealControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {

  val controller: YouCannotAppealController = injector.instanceOf[YouCannotAppealController]

  "GET /you-cannot-appeal" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoad(), "GET", "/you-cannot-appeal")

  }
}
