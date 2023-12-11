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

import base.SpecBase
import connectors.httpParsers.BadRequest
import connectors.httpParsers.TimeToPayHttpParser.TimeToPayResponse
import models.ess.{TimeToPayRequestModel, TimeToPayResponseModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class TimeToPayConnectorSpec extends SpecBase with LogCapturing {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])

  val sampleRequestModel: TimeToPayRequestModel = TimeToPayRequestModel("http://url/return", "http://url/back")
  val sampleTTPResponse: TimeToPayResponseModel = TimeToPayResponseModel("1234", "http://url/next")

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val connector = new TimeToPayConnector(
      mockHttpClient,
      mockAppConfig
    )
  }

  "setupJourney" should {
    "return Right when parser returns right" in new Setup {
      when(mockAppConfig.essttpBackendUrl).thenReturn("http://url/url")

      when(mockHttpClient.POST[TimeToPayRequestModel, TimeToPayResponse](
        any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(Right(sampleTTPResponse)))

      val result: Future[TimeToPayResponse] = connector.setupJourney(sampleRequestModel)

      await(result) shouldBe Right(sampleTTPResponse)
    }

    "return left when the parser returns left" in new Setup {
      when(mockAppConfig.essttpBackendUrl).thenReturn("http://url/wrongurl")

      when(mockHttpClient.POST[TimeToPayRequestModel, TimeToPayResponse](
        any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(Left(BadRequest)))

      val result: Future[TimeToPayResponse] = connector.setupJourney(sampleRequestModel)

      await(result) shouldBe Left(BadRequest)
    }
  }

}
