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
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

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
    val connector = new PenaltiesConnector(
      mockHttpClient,
      mockAppConfig
    )
  }

  "getAppealsDataForPenalty" should {
    s"return $Some $JsValue when the connector call succeeds" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, appealDataAsJson.toString())))
      when(mockAppConfig.appealDataForPenaltyAndEnrolmentKey(Matchers.any(), Matchers.any()))
        .thenReturn("http://url/url")

      val result = await(connector.getAppealsDataForPenalty("12345", "123456789"))
      result.isDefined shouldBe true
      result.get.toString() shouldBe appealDataAsJson.toString()
    }

    s"return $None when the connector returns NOT_FOUND (${Status.NOT_FOUND})" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, "")))
      when(mockAppConfig.appealDataForPenaltyAndEnrolmentKey(Matchers.any(), Matchers.any()))
        .thenReturn("http://url/url")

      val result = await(connector.getAppealsDataForPenalty("12345", "123456789"))
      result.isDefined shouldBe false
    }

    s"return $None when the connector returns an unknown status e.g. ISE (${Status.INTERNAL_SERVER_ERROR})" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.IM_A_TEAPOT, "")))
      when(mockAppConfig.appealDataForPenaltyAndEnrolmentKey(Matchers.any(), Matchers.any()))
        .thenReturn("http://url/url")

      val result = await(connector.getAppealsDataForPenalty("12345", "123456789"))
      result.isDefined shouldBe false
    }
  }

  "getListOfReasonableExcuses" should {
    "return OK and a response JSON" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, reasonableExcusesAsJson.toString())))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe true
      result.get shouldBe reasonableExcusesAsJson
    }

    s"return $None when a 404 is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }

    s"return $None when a 500 is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }

    s"return $None when an unknown response  is returned from the call" in new Setup {
      when(mockHttpClient.GET[HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.IM_A_TEAPOT, "")))
      when(mockAppConfig.reasonableExcuseFetchUrl)
        .thenReturn("http://url/url")
      val result = await(connector.getListOfReasonableExcuses())
      result.isDefined shouldBe false
    }
  }

  "submitAppeal" should {
    "return the HTTP response back to the caller" in new Setup {
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(),
        Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, "")))
      when(mockAppConfig.submitAppealUrl(Matchers.any()))
        .thenReturn("http://url/url?enrolmentKey=HMRC-MTD-VAT~VRN~123456789")
      val appealSubmissionModel = AppealSubmission(
        submittedBy = "client", penaltyId = "1234", reasonableExcuse = "crime", honestyDeclaration = true, appealInformation = CrimeAppealInformation(
          `type` = "crime", dateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssue = true, statement = None, lateAppeal = false, lateAppealReason = None
        )
      )
      val result = await(connector.submitAppeal(appealSubmissionModel, "HMRC-MTD-VAT~VRN~123456789"))
      result.status shouldBe OK
    }

    "return an exception when something unexpected goes wrong" in new Setup {
      when(mockHttpClient.POST[AppealSubmission, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(),
        Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.failed(new Exception("something went wrong.")))
      when(mockAppConfig.submitAppealUrl(Matchers.any()))
        .thenReturn("http://url/url")
      val appealSubmissionModel = AppealSubmission(
        submittedBy = "client", penaltyId = "1234", reasonableExcuse = "crime", honestyDeclaration = true, appealInformation = CrimeAppealInformation(
          `type` = "crime", dateOfEvent = "2020-01-01T13:00:00.000Z", reportedIssue = true, statement = None, lateAppeal = false, lateAppealReason = None
        )
      )
      val result = intercept[Exception](await(connector.submitAppeal(appealSubmissionModel, "HMRC-MTD-VAT~VRN~123456789")))
      result.getMessage shouldBe "something went wrong."
    }
  }
}
