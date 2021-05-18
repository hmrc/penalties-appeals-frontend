/*
 * Copyright 2021 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo, urlMatching}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDateTime

object PenaltiesStub {
  private val appealUri = "/penalties/appeals-data/late-submissions"
  private val fetchReasonableExcuseUri = "/penalties/appeals-data/reasonable-excuses"

  def successfulGetAppealDataResponse(penaltyId: String, enrolmentKey: String): StubMapping = {
    stubFor(get(urlEqualTo(s"$appealUri?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
              "type" -> PenaltyTypeEnum.Late_Submission,
              "startDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString,
              "endDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString
            ).toString()
          )
      )
    )
  }

  def successfulFetchReasonableExcuseResponse: StubMapping = {
    stubFor(get(urlEqualTo(fetchReasonableExcuseUri))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
              "excuses" -> Json.arr(
                Json.obj(
                  "type" -> "type1",
                  "descriptionKey" -> "key1"
                ),
                Json.obj(
                  "type" -> "type2",
                  "descriptionKey" -> "key2"
                ),
                Json.obj(
                  "type" -> "other",
                  "descriptionKey" -> "key3"
                )
              )
            ).toString()
          )
      )
    )
  }

  def failedGetAppealDataResponse(penaltyId: String, enrolmentKey: String, status: Int = Status.INTERNAL_SERVER_ERROR): StubMapping = {
    stubFor(get(urlEqualTo(appealUri + s"?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"))
      .willReturn(
        aResponse()
          .withStatus(status)
      )
    )
  }

  def failedFetchReasonableExcuseListResponse(status: Int = Status.INTERNAL_SERVER_ERROR): StubMapping = {
    stubFor(get(urlEqualTo(fetchReasonableExcuseUri))
      .willReturn(
        aResponse()
          .withStatus(status)
      )
    )
  }

  def failedCall(penaltyId: String, enrolmentKey: String): StubMapping = {
    stubFor(get(urlEqualTo(appealUri + s"?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"))
      .willReturn(
        aResponse()
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
      )
    )
  }

  def failedCallForFetchingReasonableExcuse: StubMapping = {
    stubFor(get(urlEqualTo(fetchReasonableExcuseUri))
      .willReturn(
        aResponse()
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
      )
    )
  }
}
