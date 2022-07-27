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

import base.SpecBase
import config.AppConfig
import connectors.httpParsers.MultiplePenaltiesHttpParser.MultiplePenaltiesResponse
import connectors.httpParsers.{NoContent, UnexpectedFailure}
import models.appeals.{AppealSubmission, CrimeAppealInformation, MultiplePenaltiesData}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnectorSpec extends SpecBase {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01T13:00:00",
      | "lateAppeal": "false"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPP: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_PAYMENT",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01T13:00:00",
      | "lateAppeal": "false"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPPAdditional: JsValue = Json.parse(
    """
      |{
      | "type": "ADDITIONAL",
      | "startDate": "2020-01-01",
      | "endDate": "2020-01-01T13:00:00",
      | "lateAppeal": "false"
      |}
      |""".stripMargin)

  val reasonableExcusesAsJson: JsValue = Json.parse(
    """
      |{
      |  "excuses": [
      |    {
      |      "type": "bereavement",
      |      "descriptionKey": "reasonableExcuses.bereavementReason"
      |    },
      |    {
      |      "type": "crime",
      |      "descriptionKey": "reasonableExcuses.crimeReason"
      |    },
      |    {
      |      "type": "fireOrFlood",
      |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
      |    },
      |    {
      |      "type": "health",
      |      "descriptionKey": "reasonableExcuses.healthReason"
      |    },
      |    {
      |      "type": "lossOfStaff",
      |      "descriptionKey": "reasonableExcuses.lossOfStaffReason"
      |    },
      |    {
      |      "type": "technicalIssues",
      |      "descriptionKey": "reasonableExcuses.technicalIssuesReason"
      |    },
      |    {
      |      "type": "other",
      |      "descriptionKey": "reasonableExcuses.otherReason"
      |    }
      |  ]
      |}
      |""".stripMargin)

  val multiplePenaltiesJson: JsValue = Json.parse(
    """
      |{
      | "firstPenaltyChargeReference": "123456789",
      | "firstPenaltyAmount": "101.01",
      | "secondPenaltyChargeReference": "123456790",
      | "secondPenaltyAmount": "101.02"
      |}
      |""".stripMargin
  )

  val multiplePenaltiesModel: MultiplePenaltiesData = MultiplePenaltiesData(
    firstPenaltyChargeReference = "123456789",
    firstPenaltyAmount = 101.01,
    secondPenaltyChargeReference = "123456790",
    secondPenaltyAmount = 101.02
  )

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val connector = new PenaltiesConnector(
      mockHttpClient,
      mockAppConfig
    )
    val correlationId: String = "id"
  }

  "getAppealUrlBasedOnPenaltyType" should {
    "return the correct URL when the appeal is for a LPP" in new Setup {
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(), any()))
        .thenReturn("http://url/url")
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://wrongurl/wrongurl")

      val result: String = connector.getAppealUrlBasedOnPenaltyType("1234", "XXXXXX", isLPP = true, isAdditional = false)
      result shouldBe "http://url/url"
    }

    "return the correct URL when the appeal is for a LPP Additional" in new Setup {
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(), any()))
        .thenReturn("http://url/url")
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://wrongurl/wrongurl")

      val result: String = connector.getAppealUrlBasedOnPenaltyType("1234", "XXXXXX", isLPP = true, isAdditional = true)
      result shouldBe "http://url/url"
    }

    "return the correct URL when the appeal is for a LSP" in new Setup {
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(), any()))
        .thenReturn("http://wrongurl/wrongurl")
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://url/url")

      val result: String = connector.getAppealUrlBasedOnPenaltyType("1234", "XXXXXX", isLPP = false, isAdditional = false)
      result shouldBe "http://url/url"
    }
  }

  "getAppealsDataForPenalty" should {
    s"return $Some $JsValue when the connector call succeeds" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, appealDataAsJson.toString())))
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = false, isAdditional = false))
      result.isDefined shouldBe true
      result.get.toString() shouldBe appealDataAsJson.toString()
    }

    s"return $Some $JsValue when the connector call succeeds for LPP" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, appealDataAsJsonLPP.toString())))
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = true, isAdditional = false))
      result.isDefined shouldBe true
      result.get.toString() shouldBe appealDataAsJsonLPP.toString()
    }

    s"return $Some $JsValue when the connector call succeeds for LPP - Additional" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, appealDataAsJsonLPPAdditional.toString())))
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = true, isAdditional = true))
      result.isDefined shouldBe true
      result.get.toString() shouldBe appealDataAsJsonLPPAdditional.toString()
    }

    s"return $None when the connector returns NOT_FOUND (${Status.NOT_FOUND})" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, "")))
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = false, isAdditional = false))
      result.isDefined shouldBe false
    }

    s"return $None when the connector returns an unknown status e.g. ISE (${Status.IM_A_TEAPOT})" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.IM_A_TEAPOT, "")))
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = false, isAdditional = false))
      result.isDefined shouldBe false
    }
  }

  "getMultiplePenaltiesForPrincipleCharge" should {
    s"return Right with the correct model when the connector call succeeds" in new Setup {
      when(mockHttpClient.GET[MultiplePenaltiesResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Right(multiplePenaltiesModel)))
      when(mockAppConfig.multiplePenaltyDataUrl(any(), any()))
        .thenReturn("http://url/url")

      val result: MultiplePenaltiesResponse = await(connector.getMultiplePenaltiesForPrincipleCharge("1234", "123456789"))
      result.isRight shouldBe true
      result shouldBe Right(multiplePenaltiesModel)
    }

    s"return Left when $NO_CONTENT is returned from the call" in new Setup {
      when(mockHttpClient.GET[MultiplePenaltiesResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Left(NoContent)))
      when(mockAppConfig.multiplePenaltyDataUrl(any(), any()))
        .thenReturn("http://url/url")

      val result: MultiplePenaltiesResponse = await(connector.getMultiplePenaltiesForPrincipleCharge("1234", "123456789"))
      result.isLeft shouldBe true
      result.left.get shouldBe NoContent
    }

    s"return Left when $NOT_FOUND is returned from the call" in new Setup {
      when(mockHttpClient.GET[MultiplePenaltiesResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(Status.NOT_FOUND, s"Unexpected response, status $NOT_FOUND returned"))))
      when(mockAppConfig.multiplePenaltyDataUrl(any(), any()))
        .thenReturn("http://url/url")

      val result: MultiplePenaltiesResponse = await(connector.getMultiplePenaltiesForPrincipleCharge("1234", "123456789"))
      result.isLeft shouldBe true
      result.left.get shouldBe UnexpectedFailure(NOT_FOUND, s"Unexpected response, status $NOT_FOUND returned")
    }

    s"return Left when $INTERNAL_SERVER_ERROR is returned from the call" in new Setup {
      when(mockHttpClient.GET[MultiplePenaltiesResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned"))))
      when(mockAppConfig.multiplePenaltyDataUrl(any(), any()))
        .thenReturn("http://url/url")

      val result: MultiplePenaltiesResponse = await(connector.getMultiplePenaltiesForPrincipleCharge("1234", "123456789"))
      result.isLeft shouldBe true
      result.left.get shouldBe UnexpectedFailure(INTERNAL_SERVER_ERROR, s"Unexpected response, status $INTERNAL_SERVER_ERROR returned")
    }
  }

  "getListOfReasonableExcuses" should {
    "return OK and a response JSON" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, reasonableExcusesAsJson.toString())))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result: Option[JsValue] = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe true
      result.get shouldBe reasonableExcusesAsJson
    }

    s"return $None when a 404 is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result: Option[JsValue] = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }

    s"return $None when a 500 is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result: Option[JsValue] = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }

    s"return $None when an unknown response  is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.IM_A_TEAPOT, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result: Option[JsValue] = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }
  }

  "submitAppeal" should {
    "return the HTTP response back to the caller" in new Setup {
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](any(), any(), any())(any(),
        any(), ArgumentMatchers.eq(hc.copy(authorization = None, otherHeaders = hc.otherHeaders)), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "")))
      when(mockAppConfig.submitAppealUrl(any(), any(), any(), any()))
        .thenReturn("http://url/url?enrolmentKey=HMRC-MTD-VAT~VRN~123456789")
      val appealSubmissionModel: AppealSubmission = AppealSubmission(
        sourceSystem = "MDTP", taxRegime = "VAT", customerReferenceNo = "VRN1234567890", dateOfAppeal = LocalDateTime.of(
          2020, 1, 1, 0, 0, 0), isLPP = true, appealSubmittedBy = "client",
        agentDetails = None, appealInformation = CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
          startDateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssueToPolice = true, statement = None, lateAppeal = false,
          lateAppealReason = None, isClientResponsibleForSubmission = None, isClientResponsibleForLateSubmission = None
        )
      )
      val result: HttpResponse = await(connector.submitAppeal(appealSubmissionModel, "HMRC-MTD-VAT~VRN~123456789", isLPP = false, "123456789", correlationId))
      result.status shouldBe OK
    }

    "return an exception when something unexpected goes wrong" in new Setup {
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.failed(new Exception("something went wrong.")))
      when(mockAppConfig.submitAppealUrl(any(), any(), any(), any()))
        .thenReturn("http://url/url")
      val appealSubmissionModel: AppealSubmission = AppealSubmission(
        sourceSystem = "MDTP", taxRegime = "VAT", customerReferenceNo = "VRN1234567890", dateOfAppeal = LocalDateTime.of(
          2020, 1, 1, 0, 0, 0), isLPP = true, appealSubmittedBy = "client",
        agentDetails = None, appealInformation = CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
          startDateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssueToPolice = true, statement = None, lateAppeal = false,
          lateAppealReason = None, isClientResponsibleForSubmission = None, isClientResponsibleForLateSubmission = None
        )
      )
      val result: Exception = intercept[Exception](await(connector.submitAppeal(appealSubmissionModel,
        "HMRC-MTD-VAT~VRN~123456789", isLPP = false, "123456789", correlationId)))
      result.getMessage shouldBe "something went wrong."
    }
  }
}