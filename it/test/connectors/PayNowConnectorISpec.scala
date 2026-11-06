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

package connectors

import java.time.LocalDate

import models.payApi.{PayNowRequestModel, PayNowResponseModel}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout, INTERNAL_SERVER_ERROR}
import stubs.PayNowStub.{successfulPayNowCall, unsuccessfulPayNowCall}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import scala.concurrent.ExecutionContext

class PayNowConnectorISpec extends IntegrationSpecCommonBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val payNowConnector: PayNowConnector = injector.instanceOf[PayNowConnector]

  val requestModel: PayNowRequestModel = PayNowRequestModel(
    "9999",
    "1234567890",
    6666,
    LocalDate.of(2020, 1, 31),
    "http://url/return-url",
    "http://url/back-url"
  )

  val responseModel: PayNowResponseModel = PayNowResponseModel(
    "1234",
    "http://url/next-url"
  )

  "setupJourney" should {

    val responseBody = Json.obj(
      "journeyId" -> "1234",
      "nextUrl" -> "http://url/next-url"
    ).toString()

    "return a Right when a successful call is made" in {
      successfulPayNowCall(responseBody)
      val result = await(payNowConnector.setupJourney(requestModel))
      result.isRight shouldBe true
      result.toOption.get shouldBe responseModel
    }

    "return a Left when an unsuccessful call is made" in {
      unsuccessfulPayNowCall
      val result = await(payNowConnector.setupJourney(requestModel))
      result.isLeft shouldBe true
      result.left.toOption.get.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
