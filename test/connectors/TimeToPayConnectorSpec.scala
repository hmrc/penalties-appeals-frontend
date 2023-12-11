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
import connectors.httpParsers.TimeToPayHttpParser.TimeToPayResponse
import models.ess.{TTPRequestModel, TTPResponseModel}
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

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val connector = new TimeToPayConnector(
      mockHttpClient,
      mockAppConfig
    )
  }

  "setupJourney" should {
    "return HTTP response back to caller" in new Setup {
      val requestModel: TTPRequestModel = TTPRequestModel("/return-url", "/back-url")
      when(mockHttpClient.POST[TTPRequestModel, TTPResponseModel](any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(TTPResponseModel("1234", "/next-url")))
      val result: TimeToPayResponse = await(connector.setupJourney(requestModel)(hc, ec))
      result.isRight shouldBe true
      result.toOption.get.nextUrl shouldBe "/next-url"
      result.toOption.get.journeyId shouldBe "1234"
    }
  }

}
