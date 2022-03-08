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

package connectors

import models.PenaltyTypeEnum
import models.appeals.{AppealSubmission, CrimeAppealInformation}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

class PenaltiesConnectorISpec extends IntegrationSpecCommonBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val penaltiesConnector: PenaltiesConnector = injector.instanceOf[PenaltiesConnector]
  val correlationId: String = "correlationId"

  "getAppealUrlBasedOnPenaltyType" should {
    "return the correct url for LPP" in {
      val expectedResult =
        "http://localhost:11111/penalties/appeals-data/late-payments?penaltyId=1234&enrolmentKey=HMRC-MTD-VAT~VRN~HMRC-MTD-VAT~VRN~123456789&isAdditional=false"
      val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = false)
      actualResult shouldBe expectedResult
    }
    "return the correct url for LPP Additional" in {
      val expectedResult =
        "http://localhost:11111/penalties/appeals-data/late-payments?penaltyId=1234&enrolmentKey=HMRC-MTD-VAT~VRN~HMRC-MTD-VAT~VRN~123456789&isAdditional=true"
      val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = true, isAdditional = true)
      actualResult shouldBe expectedResult
    }


    "return the correct url for LSP" in {
      val expectedResult =
        "http://localhost:11111/penalties/appeals-data/late-submissions?penaltyId=1234&enrolmentKey=HMRC-MTD-VAT~VRN~HMRC-MTD-VAT~VRN~123456789"
      val actualResult = penaltiesConnector.getAppealUrlBasedOnPenaltyType("1234", "HMRC-MTD-VAT~VRN~123456789", isLPP = false, isAdditional = false)
      actualResult shouldBe expectedResult
    }
  }

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
      val result = await(penaltiesConnector.getAppealsDataForPenalty(
        "1234", "123456789", isLPP = false, isAdditional = false)(HeaderCarrier(), ExecutionContext.Implicits.global))
      result.isDefined shouldBe true
      result.get shouldBe sampleJsonToPassBack
    }

    s"return $None" when {
      "the call returns 404" in {
        failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", status = Status.NOT_FOUND)
        val result = await(penaltiesConnector.getAppealsDataForPenalty(
          "1234", "123456789", isLPP = false, isAdditional = false)(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call returns some unknown response" in {
        failedGetAppealDataResponse("1234", "HMRC-MTD-VAT~VRN~123456789", status = Status.IM_A_TEAPOT)
        val result = await(penaltiesConnector.getAppealsDataForPenalty(
          "1234", "123456789", isLPP = false, isAdditional = false)(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }

      "the call fails completely with no response" in {
        failedCall("1234", "HMRC-MTD-VAT~VRN~123456789")
        val result = await(penaltiesConnector.getAppealsDataForPenalty(
          "1234", "123456789", isLPP = false, isAdditional = false)(HeaderCarrier(), ExecutionContext.Implicits.global))
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
        val result = await(penaltiesConnector.getAppealsDataForPenalty(
          "1234", "123456789", isLPP = false, isAdditional = false)(HeaderCarrier(), ExecutionContext.Implicits.global))
        result.isDefined shouldBe false
      }
    }
  }

  "submitAppeal" should {
    "return the response of the call" in {
      successfulAppealSubmission(isLPP = false, penaltyNumber = "123456789")
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "VAT",
        customerReferenceNo = "VRN123456789",
        dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
        isLPP = false,
        appealSubmittedBy = "client",
        agentReferenceNo = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssueToPolice = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "HMRC-MTD-VAT~VRN~123456789", isLPP = false, "123456789", correlationId))
      result.status shouldBe OK
    }

    "return the response of the call for LPP" in {
      successfulAppealSubmission(isLPP = true, penaltyNumber = "123456789")
      val model = AppealSubmission(
        sourceSystem = "MDTP",
        taxRegime = "VAT",
        customerReferenceNo = "VRN123456789",
        dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
        isLPP = true,
        appealSubmittedBy = "client",
        agentReferenceNo = None,
        appealInformation = CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssueToPolice = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      )
      val result = await(penaltiesConnector.submitAppeal(model, "HMRC-MTD-VAT~VRN~123456789", isLPP = true, "123456789", correlationId))
      result.status shouldBe OK
    }
  }
}
