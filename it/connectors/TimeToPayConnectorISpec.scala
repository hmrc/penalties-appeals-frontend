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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo, urlPathMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.ess.{TimeToPayRequestModel, TimeToPayResponseModel}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.TimeToPayStub.successfulTTPCall
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import scala.concurrent.ExecutionContext

class TimeToPayConnectorISpec extends IntegrationSpecCommonBase {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val ttpConnector: TimeToPayConnector = injector.instanceOf[TimeToPayConnector]

  val requestModel: TimeToPayRequestModel = TimeToPayRequestModel(
    "http://url/return-url",
    "http://url/back-url"
  )

  val responseModel: TimeToPayResponseModel = TimeToPayResponseModel(
    "1234",
    "http://url/next-url"
  )

  "setupJourney" should {

    val responseBody = Json.obj(
      "journeyId" -> "1234",
      "nextUrl" -> "http://url/next-url"
    ).toString()

    "return a Right when a successful call is made" in {
      successfulTTPCall(responseBody)
      val result = await(ttpConnector.setupJourney(requestModel))
      result.isRight shouldBe true
      result.toOption.get shouldBe responseModel
    }
  }

}
