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

package connectors

import java.time.LocalDate

import base.SpecBase
import connectors.httpParsers.PayNowHttpParser.PayNowResponse
import connectors.httpParsers.BadRequest

import models.payApi.{PayNowRequestModel, PayNowResponseModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class PayNowConnectorSpec extends SpecBase with LogCapturing {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val chargeReference="123456789"
  val vatAmount = 123
  val dueDate = LocalDate.now()


  val sampleRequestModel: PayNowRequestModel = PayNowRequestModel(vrn, chargeReference, vatAmount, dueDate,"http://url/return", "http://url/back")
  val samplePayNowResponse: PayNowResponseModel = PayNowResponseModel("1234", "http://url/next")

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val connector = new PayNowConnector(
      mockHttpClient,
      mockAppConfig
    )
  }

  "setupJourney" should {
    "return Right when parser returns right" in new Setup {
      when(mockAppConfig.payApiUrl).thenReturn("http://url/url")

      when(mockHttpClient.POST[PayNowRequestModel, PayNowResponse](
        any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(Right(samplePayNowResponse)))

      val result: Future[PayNowResponse] = connector.setupJourney(sampleRequestModel)

      await(result) shouldBe Right(samplePayNowResponse)
    }

    "return left when the parser returns left" in new Setup {
      when(mockAppConfig.payApiUrl).thenReturn("http://url/wrongurl")

      when(mockHttpClient.POST[PayNowRequestModel, PayNowResponse](
        any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(Left(BadRequest)))

      val result: Future[PayNowResponse] = connector.setupJourney(sampleRequestModel)

      await(result) shouldBe Left(BadRequest)
    }
  }

}

