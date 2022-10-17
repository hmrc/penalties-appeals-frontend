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

package connectors.httpParsers

import base.SpecBase
import models.appeals.MultiplePenaltiesData
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys

import java.time.LocalDate

class MultiplePenaltiesHttpParserSpec extends SpecBase with LogCapturing {

  val multiplePenaltiesModel: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456790",
    secondPenaltyAmount = 101.02,
    firstPenaltyCommunicationDate = LocalDate.of(2022, 1, 1),
    secondPenaltyCommunicationDate = LocalDate.of(2022, 1, 2)
  )

  val multiplePenaltiesJson: JsValue = Json.parse(
    """
      |{
      | "firstPenaltyChargeReference": "123456789",
      | "firstPenaltyAmount": 101.01,
      | "secondPenaltyChargeReference": "123456790",
      | "secondPenaltyAmount": 101.02,
      | "firstPenaltyCommunicationDate": "2022-01-01",
      | "secondPenaltyCommunicationDate": "2022-01-02"
      |}
      |""".stripMargin)

  val invalidJson: JsValue = Json.obj("foo" -> "bar")

  class Setup(status: Int, optJson: Option[JsValue] = None, responseHeaders: Map[String, Seq[String]] = Map.empty) {

    val httpResponse: AnyRef with HttpResponse = HttpResponse.apply(status, optJson.get, responseHeaders)
    private val httpMethod = "GET"
    private val url = "/"

    def readResponse: MultiplePenaltiesHttpParser.MultiplePenaltiesResponse =
      MultiplePenaltiesHttpParser.MultiplePenaltiesResponseReads.read(httpMethod, url, httpResponse)
  }

  "reads" should {
    s"return the model if the status is ${Status.OK} and the model is parsed" in new Setup(Status.OK, Some(multiplePenaltiesJson)) {
      readResponse shouldBe Right(multiplePenaltiesModel)
    }


    s"return InvalidJson if the status is ${Status.OK} but the model can not be parsed" in new Setup(Status.OK, Some(invalidJson)) {
      readResponse shouldBe Left(InvalidJson)
    }

    s"return NoContent if the status is ${Status.NO_CONTENT}" in new Setup(Status.NO_CONTENT, Some(Json.obj())) {
      readResponse shouldBe Left(NoContent)
    }

    s"return $UnexpectedFailure if random non Success status code returned" in new Setup(Status.INTERNAL_SERVER_ERROR, optJson = Some(Json.obj())) {
      withCaptureOfLoggingFrom(logger) {
        logs => {
          readResponse shouldBe Left(UnexpectedFailure(500, "Unexpected response, status 500 returned"))
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES.toString)) shouldBe true
        }
      }
    }
  }

}
