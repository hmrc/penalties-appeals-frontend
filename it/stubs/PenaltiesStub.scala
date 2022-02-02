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
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDateTime
import scala.collection.JavaConverters

object PenaltiesStub {
  private val appealUriForLSP = "/penalties/appeals-data/late-submissions"
  private val appealUriForLPP = "/penalties/appeals-data/late-payments"
  private val fetchReasonableExcuseUri = "/penalties/appeals-data/reasonable-excuses"
  private val fetchOtherPenalties = (penaltyID: String) =>
    s"/penalties/appeals/multiple-penalties-in-same-period?enrolmentKey=HMRC-MTD-VAT~VRN~123456789&penaltyId=$penaltyID&isLPP=false"

  private val submitAppealUri = "/penalties/appeals/submit-appeal"
  private val submitAppealQueryParams = (isLPP: Boolean, penaltyId: String) => Map[String, StringValuePattern](
  ("enrolmentKey" -> equalTo("HMRC-MTD-VAT~VRN~123456789")),
  ("isLPP" -> equalTo(isLPP.toString)),
  ("penaltyId" -> equalTo(penaltyId)),
  ("correlationId" -> matching(".*"))
)

  def successfulGetAppealDataResponse(
                                       penaltyId: String,
                                       enrolmentKey: String,
                                       isLPP: Boolean = false,
                                       isAdditional: Boolean = false
                                     ): StubMapping = {
    val typeOfPenalty =
      if (isAdditional) PenaltyTypeEnum.Additional
      else if (isLPP) PenaltyTypeEnum.Late_Payment
      else PenaltyTypeEnum.Late_Submission
    val uri = if (isLPP) appealUriForLPP else appealUriForLSP
    val extraAdditionalParam = if (isLPP) s"&isAdditional=$isAdditional" else ""
    stubFor(
      get(
        urlEqualTo(
          s"$uri?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey$extraAdditionalParam"
        )
      ).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json
              .obj(
                "type" -> typeOfPenalty,
                "startDate" -> LocalDateTime
                  .of(2020, 1, 1, 12, 0, 0)
                  .toString,
                "endDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString,
                "dueDate" -> LocalDateTime.of(2020, 2, 7, 12, 0, 0).toString,
                "dateCommunicationSent" -> LocalDateTime
                  .of(2020, 2, 8, 12, 0, 0)
                  .toString
              )
              .toString()
          )
      )
    )
  }

  def successfulFetchReasonableExcuseResponse: StubMapping = {

    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(
              Json
                .obj(
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
                )
                .toString()
            )
        )
    )
  }

  def successfulAppealSubmission(isLPP: Boolean, penaltyId: String): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri)).withQueryParams(JavaConverters.mapAsJavaMap(submitAppealQueryParams(isLPP, penaltyId)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
        )
    )
  }

  def successfulCallForOtherPenalties(
                                           penaltyId: String,
                                           status: Int
                                         ): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchOtherPenalties(penaltyId)))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  def failedAppealSubmissionWithFault(isLPP: Boolean, penaltyId: String): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri)).withQueryParams(JavaConverters.mapAsJavaMap(submitAppealQueryParams(isLPP, penaltyId)))
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }

  def failedAppealSubmission(isLPP: Boolean, penaltyId: String, status: Option[Int] = None): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri)).withQueryParams(JavaConverters.mapAsJavaMap(submitAppealQueryParams(isLPP, penaltyId)))
        .willReturn(
          aResponse()
            .withStatus(status.fold(Status.INTERNAL_SERVER_ERROR)(identity))
        )
    )
  }

  def failedGetAppealDataResponse(
                                   penaltyId: String,
                                   enrolmentKey: String,
                                   status: Int = Status.INTERNAL_SERVER_ERROR
                                 ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP + s"?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
      )
    )
  }

  def failedFetchReasonableExcuseListResponse(
                                               status: Int = Status.INTERNAL_SERVER_ERROR
                                             ): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  def failedCall(penaltyId: String, enrolmentKey: String): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP + s"?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
        )
      ).willReturn(
        aResponse()
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
      )
    )
  }

  def failedCallForFetchingReasonableExcuse: StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri))
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }
}
