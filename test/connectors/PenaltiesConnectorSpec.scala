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

import base.SpecBase
import config.AppConfig
import models.appeals.{AppealSubmission, CrimeAppealInformation}
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
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "lateAppeal": "false"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPP: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_PAYMENT",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "lateAppeal": "false"
      |}
      |""".stripMargin)

  val appealDataAsJsonLPPAdditional: JsValue = Json.parse(
    """
      |{
      | "type": "ADDITIONAL",
      | "startDate": "2020-01-01T12:00:00",
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

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val mockHeaderGenerator: HeaderGenerator = mock(classOf[HeaderGenerator])
    val connector = new PenaltiesConnector(
      mockHttpClient,
      mockAppConfig,
      mockHeaderGenerator
    )
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
      when(mockAppConfig.appealLPPDataForPenaltyAndEnrolmentKey(any(), any(),any()))
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

    s"return $None when the connector returns an unknown status e.g. ISE (${Status.INTERNAL_SERVER_ERROR})" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.IM_A_TEAPOT, "")))
      when(mockAppConfig.appealLSPDataForPenaltyAndEnrolmentKey(any(), any()))
        .thenReturn("http://url/url")

      val result: Option[JsValue] = await(connector.getAppealsDataForPenalty("12345", "123456789", isLPP = false, isAdditional = false))
      result.isDefined shouldBe false
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

  "submitAppeal with headers" should {
    "return the HTTP response back to the caller" in new Setup {
      when(mockHeaderGenerator.headersForPEGA()).thenReturn(Seq("someHeader" -> "someHeaderValue"))
      val mockHeaders: Seq[(String, String)] = mockHeaderGenerator.headersForPEGA()
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](any(), any(),ArgumentMatchers.eq(mockHeaders))(any(),
        any(), ArgumentMatchers.eq(hc.copy(authorization = None)), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "")))
      when(mockAppConfig.submitAppealUrl(any(), any(), any()))
        .thenReturn("http://url/url?enrolmentKey=HMRC-MTD-VAT~VRN~123456789")
      val appealSubmissionModel: AppealSubmission = AppealSubmission(
        sourceSystem = "MDTP", taxRegime = "VAT", customerReferenceNo = "VRN1234567890", dateOfAppeal = LocalDateTime.of(
          2020,1,1,0,0,0), isLPP = true, appealSubmittedBy = "client",
        agentDetails = None,  appealInformation = CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
          startDateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssueToPolice = true, statement = None, lateAppeal = false,
          lateAppealReason = None, isClientResponsibleForSubmission = None, isClientResponsibleForLateSubmission = None
        )
      )
      val result: HttpResponse = await(connector.submitAppeal(appealSubmissionModel, "HMRC-MTD-VAT~VRN~123456789", isLPP = false, "123456789"))
      result.status shouldBe OK
    }

    "return an exception when something unexpected goes wrong" in new Setup {
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](any(), any(), any())(any(),
        any(), any(), any()))
        .thenReturn(Future.failed(new Exception("something went wrong.")))
      when(mockAppConfig.submitAppealUrl(any(), any(), any()))
        .thenReturn("http://url/url")
      val appealSubmissionModel: AppealSubmission = AppealSubmission(
        sourceSystem = "MDTP", taxRegime = "VAT", customerReferenceNo = "VRN1234567890", dateOfAppeal = LocalDateTime.of(
          2020,1,1,0,0,0), isLPP = true, appealSubmittedBy = "client",
        agentDetails = None, appealInformation = CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
          startDateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssueToPolice = true, statement = None, lateAppeal = false,
          lateAppealReason = None, isClientResponsibleForSubmission = None, isClientResponsibleForLateSubmission = None
        )
      )
      val result: Exception = intercept[Exception](await(connector.submitAppeal(appealSubmissionModel,
        "HMRC-MTD-VAT~VRN~123456789", isLPP = false, "123456789")))
      result.getMessage shouldBe "something went wrong."
    }
  }

  "getOtherPenaltiesInTaxPeriod" should {
    "return the HTTP response back to the caller" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "")))
      when(mockAppConfig.otherPenaltiesForPeriodUrl(any(), any(), any()))
        .thenReturn("http://url/url")

      val result: HttpResponse = await(connector.getOtherPenaltiesInTaxPeriod("1234",
        "HMRC-MTD-VAT~VRN~123456789", isLPP = false))
      result.status shouldBe OK
    }

    "return an exception when something unexpected goes wrong" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.failed(new Exception("something went wrong.")))
      when(mockAppConfig.otherPenaltiesForPeriodUrl(any(), any(), any()))
        .thenReturn("http://url/url")

      val result: Exception = intercept[Exception](await(connector.getOtherPenaltiesInTaxPeriod("1234",
        "HMRC-MTD-VAT~VRN~123456789", isLPP = false)))
      result.getMessage shouldBe "something went wrong."
    }
  }
}