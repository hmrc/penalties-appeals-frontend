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
import models.appeals.{AppealSubmission, CrimeAppealInformation}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val penaltiesConnector: PenaltiesConnector = injector.instanceOf[PenaltiesConnector]

  "getAppealsDataForPenalty" should {
    s"return $Some and the $JsValue returned by the call when the call is successful" in {
      successfulGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789")
      val sampleJsonToPassBack: JsValue = Json.obj(
        "type" -> PenaltyTypeEnum.Late_Submission,
        "startDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString,
        "endDate" -> LocalDateTime.of(2020, 1, 1, 12, 0, 0).toString,
        "dueDate" -> LocalDateTime.of(2020, 2, 7, 12, 0, 0).toString,
        "dateCommunicationSent" -> LocalDateTime.of(2020, 2, 8, 12, 0, 0).toString
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

  "getListOfReasonableExcuses" should {
    s"return $Some and the $JsValue returned by the call when the call is successful" in {
      successfulFetchReasonableExcuseResponse
      val sampleJsonToPassBack: JsValue = Json.obj(
        "excuses" -> Json.arr(
          Json.obj(
            "type" -> "type1",
            "descriptionKey" -> "key1",
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
      val result = await(penaltiesConnector.getListOfReasonableExcuses()(HeaderCarrier(), ExecutionContext.Implicits.global))
      result.isDefined shouldBe true
      result.get shouldBe sampleJsonToPassBack
    }

    s"return $None" when {
      "the call returns 404" in {
        failedFetchReasonableExcuseListResponse(status = Status.NOT_FOUND)
        val result = await(penaltiesConnector.getListOfReasonableExcuses()(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call returns 500" in {
        failedFetchReasonableExcuseListResponse(status = Status.INTERNAL_SERVER_ERROR)
        val result = await(penaltiesConnector.getListOfReasonableExcuses()(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }


      "the call returns some unknown response" in {
        failedFetchReasonableExcuseListResponse(status = Status.IM_A_TEAPOT)
        val result = await(penaltiesConnector.getListOfReasonableExcuses()(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call fails completely with no response" in {
        failedCallForFetchingReasonableExcuse
        val result = await(penaltiesConnector.getAppealsDataForPenalty("1234", "123456789")(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }
    }
  }

  "submitAppeal" should {
    "return the response of the call" in {
      successfulAppealSubmission
      val model = AppealSubmission(
        submittedBy = "client",
        penaltyId = "1234",
        reasonableExcuse = "crime",
        honestyDeclaration = true,
        appealInformation = CrimeAppealInformation(
          `type` = "crime",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssue = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "HMRC-MTD-VAT~VRN~123456789"))
      result.status shouldBe OK
    }
  }
}
