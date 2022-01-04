/*
 * Copyright 2022 HM Revenue & Customs
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

object UpscanStub {
  private val initiateUpscanUrl = "/upscan/v2/initiate"

  def successfulInitiateCall(responseBody: String): StubMapping = {
    stubFor(
      post(urlEqualTo(initiateUpscanUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(responseBody)
        )
    )
  }

  def successfulCallInvalidJson: StubMapping = {
    stubFor(
      post(urlEqualTo(initiateUpscanUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(Json.obj().toString())
        )
    )
  }

  def failedInitiateCall(responseBody: String): StubMapping = {
    stubFor(
      post(urlEqualTo(initiateUpscanUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
        )
    )
  }
}
