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

package stubs


import com.github.tomakehurst.wiremock.client.WireMock._

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json


object PayNowStub {
  private val payNowUrl = "/pay-api/view-and-change/vat/other/journey/start"

  def successfulPayNowCall(responseBody: String): StubMapping = {
    stubFor(
      post(urlEqualTo(payNowUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.CREATED)
            .withBody(responseBody)
        )
    )
  }

  def unsuccessfulPayNowCall: StubMapping = {
    stubFor(
      post(urlEqualTo(payNowUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .withBody(Json.obj(
              "statusCode" -> 500,
              "message" -> "There was an error"
            ).toString())
        )
    )
  }
}
