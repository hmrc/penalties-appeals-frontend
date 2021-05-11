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

package connectors

import models.PenaltyTypeEnum
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {
  val penaltiesConnector: PenaltiesConnector = injector.instanceOf[PenaltiesConnector]
  "getAppealsDataForPenalty" should {
    s"return $Some and the $JsValue returned by the call when the call is successful" in {
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val sampleJsonToPassBack: JsValue = Json.obj(
        "type" -> PenaltyTypeEnum.Late_Submission,
        "startDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString,
        "endDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString
      )
      val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789")(HeaderCarrier(), ExecutionContext.Implicits.global))
      result.isDefined shouldBe true
      result.get shouldBe sampleJsonToPassBack
    }

    s"return $None" when {
      "the call returns 404" in {
        failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", status = Status.NOT_FOUND)
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789")(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call returns some unknown response" in {
        failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", status = Status.IM_A_TEAPOT)
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789")(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call fails completely with no response" in {
        failedCall("1234", "HMRC-MTD-VAT~VRN~123456789")
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789")(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }
    }
  }
}
