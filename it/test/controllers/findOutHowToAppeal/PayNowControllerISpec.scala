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

import java.time.LocalDate

import controllers.testHelpers.AuthorisationTest
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.PayNowStub.{successfulPayNowCall, unsuccessfulPayNowCall}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IntegrationSpecCommonBase, SessionKeys}

class PayNowControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: PayNowController = injector.instanceOf[PayNowController]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val vrn = "123456789"
  val chargeReference ="123456789"
  val vatAmount = BigDecimal(123.45)
  val dueDateOfPeriod = LocalDate.now()

  "GET /pay-now" should {
    "return 303 (SEE_OTHER) when there is a successful call" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.vatAmount -> 1234,
      SessionKeys.principalChargeReference -> "123456789"))) {
      val responseBody: String = Json.obj(
        "journeyId" -> "1234",
        "nextUrl" -> "http://url/next-url"
      ).toString()
      successfulPayNowCall(responseBody)
      val result: Result = await(controller.redirect()(fakeRequest))
      result.header.status shouldBe SEE_OTHER
      result.header.headers("Location") shouldBe "http://url/next-url"
    }

    "return an error when a call is unsuccessful" in {
      unsuccessfulPayNowCall
      val result = await(controller.redirect()(fakeRequest))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
