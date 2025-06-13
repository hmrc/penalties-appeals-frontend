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
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDate
import scala.jdk.CollectionConverters._

object PenaltiesStub {
  private val appealUriForLSP = (regime: String, idType: String, id: String) =>
    s"/penalties/$regime/appeals-data/late-submissions/$idType/$id"

  private val appealUriForLPP = (regime: String, idType: String, id: String) =>
    s"/penalties/$regime/appeals-data/late-payments/$idType/$id"

  private val fetchReasonableExcuseUri = (regime: String) =>
   s"/penalties/$regime/appeals-data/reasonable-excuses"

  private val submitAppealUri = (regime: String, idType: String, id: String) =>
    s"/penalties/$regime/appeals/submit-appeal/$idType/$id"

  private val submitAppealQueryParams = (isLPP: Boolean, penaltyNumber: String) => Map[String, StringValuePattern](
  ("isLPP" -> equalTo(isLPP.toString)),
  ("penaltyNumber" -> equalTo(penaltyNumber)),
  ("correlationId" -> matching(".*"))
)
  private val multiplePenaltiesUri = (penaltyId: String, regime: String, idType: String, id: String) =>
    s"/penalties/$regime/appeals-data/multiple-penalties/$idType/$id?penaltyId=$penaltyId"

  def successfulGetAppealDataResponse(
                                       penaltyId: String,
                                       regime: String,
                                       idType: String,
                                       id: String,
                                       isLPP: Boolean = false,
                                       isAdditional: Boolean = false
                                     ): StubMapping = {
    val typeOfPenalty =
      if (isAdditional) PenaltyTypeEnum.Additional
      else if (isLPP) PenaltyTypeEnum.Late_Payment
      else PenaltyTypeEnum.Late_Submission
    val uri = if (isLPP) appealUriForLPP(regime, idType, id) else appealUriForLSP(regime, idType, id)
    val extraAdditionalParam = if (isLPP) s"&isAdditional=$isAdditional" else ""
    stubFor(
      get(
        urlEqualTo(
          s"$uri?penaltyId=$penaltyId$extraAdditionalParam"
        )
      ).willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
                "type" -> typeOfPenalty,
              "startDate" -> LocalDate.of(2020, 1, 1).toString,
              "endDate" -> LocalDate.of(2020, 1, 31).toString,
              "dueDate" -> LocalDate.of(2020, 3, 7).toString,
              "dateCommunicationSent" -> LocalDate.of(2020, 3, 8).toString
              ).toString()
          )
      )
    )
  }

  def successfulFetchReasonableExcuseResponse(regime: String): StubMapping = {

    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(regime)))
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

  def successfulAppealSubmission(regime: String, idType: String, id: String, isLPP: Boolean, penaltyNumber: String): StubMapping = {
    val responseBody =
      """
        |{
        | "caseId": "PR-1234",
        | "status": 200
        |}
        |""".stripMargin
    stubFor(
      post(urlPathMatching(submitAppealUri(regime, idType, id))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(responseBody)
        )
    )
  }

  def successfulGetMultiplePenalties(penaltyId: String, regime: String, idType: String, id: String): StubMapping = {
    stubFor(
      get(urlEqualTo(multiplePenaltiesUri(penaltyId, regime, idType, id)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(
              Json.obj(
                "firstPenaltyChargeReference" -> "123456789",
                "firstPenaltyAmount" -> "101.01",
                "secondPenaltyChargeReference" -> "123456790",
                "secondPenaltyAmount" -> "1.02",
                "firstPenaltyCommunicationDate" -> "2023-04-06",
                "secondPenaltyCommunicationDate" -> "2023-04-07"
              ).toString()
            )
        )
    )
  }

  def failedGetMultiplePenalties(penaltyId: String, regime: String, idType: String, id: String, status: Int = Status.INTERNAL_SERVER_ERROR): StubMapping = {
    stubFor(
      get(urlEqualTo(multiplePenaltiesUri(penaltyId, regime, idType, id)))
        .willReturn(
          aResponse()
          .withStatus(status)
        )
    )
  }

  def failedAppealSubmissionWithFault(regime: String, idType: String, id: String, isLPP: Boolean, penaltyNumber: String): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri(regime, idType, id))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }

  def failedAppealSubmission(regime: String, idType: String, id: String, isLPP: Boolean, penaltyNumber: String, status: Option[Int] = None): StubMapping = {
    stubFor(
      post(urlPathMatching(submitAppealUri(regime, idType, id))).withQueryParams(submitAppealQueryParams(isLPP, penaltyNumber).asJava)
        .willReturn(
          aResponse()
            .withStatus(status.fold(Status.INTERNAL_SERVER_ERROR)(identity))
            .withBody("Some issue with document storage")
        )
    )
  }

  def failedGetAppealDataResponse(
                                   penaltyId: String,
                                   regime: String,
                                   idType: String,
                                   id: String,
                                   status: Int = Status.INTERNAL_SERVER_ERROR
                                 ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP(regime, idType, id) + s"?penaltyId=$penaltyId"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
      )
    )
  }

  def failedFetchReasonableExcuseListResponse(regime: String,
                                               status: Int = Status.INTERNAL_SERVER_ERROR
                                             ): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(regime)))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  def failedCall(penaltyId: String, regime: String, idType: String, id: String): StubMapping = {
    stubFor(
      get(
        urlEqualTo(
          appealUriForLSP(regime, idType, id) + s"?penaltyId=$penaltyId"
        )
      ).willReturn(
        aResponse()
          .withFault(Fault.CONNECTION_RESET_BY_PEER)
      )
    )
  }

  def failedCallForFetchingReasonableExcuse(regime: String): StubMapping = {
    stubFor(
      get(urlEqualTo(fetchReasonableExcuseUri(regime)))
        .willReturn(
          aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)
        )
    )
  }
}
