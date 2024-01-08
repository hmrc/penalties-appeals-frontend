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
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.IntegrationSpecCommonBase

class IfYouvePaidYourVATControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {

  val controller: IfYouvePaidYourVATController = injector.instanceOf[IfYouvePaidYourVATController]

  "GET /if-youve-paid-your-VAT" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val request: Result = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe OK
    }
  }
}
