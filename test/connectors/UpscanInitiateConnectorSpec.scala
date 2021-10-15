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
import connectors.httpParsers.UpscanInitiateHttpParser.{BadRequest, UpscanInitiateResponse}
import models.upload.{UploadFormTemplateRequest, UpscanInitiateRequest, UpscanInitiateResponseModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class UpscanInitiateConnectorSpec extends SpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockHttpClient: HttpClient = mock(classOf[HttpClient])
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  val upscanInitiateDataModel: UpscanInitiateRequest = UpscanInitiateRequest("")
  val sampleUpscanResponse: UpscanInitiateResponseModel =
    UpscanInitiateResponseModel(
      "foo",
      UploadFormTemplateRequest("bar", Map("doo" -> "dar"))
    )

  class Setup {
    reset(mockHttpClient)
    reset(mockAppConfig)
    val connector = new UpscanConnector(
      mockHttpClient,
      mockAppConfig
    )
  }

  "initiateToUpscan" should {

    "return Right when parser returns right" in new Setup {
      when(mockAppConfig.upscanInitiateBaseUrl)
        .thenReturn("http://url/url")

      when(
        mockHttpClient.POST[UpscanInitiateRequest, UpscanInitiateResponse](
          any(),
          any(),
          any()
        )(any(), any(), any(), any())
      ).thenReturn(Future.successful(Right(sampleUpscanResponse)))

      val result = connector.initiateToUpscan(upscanInitiateDataModel)

      await(result) shouldBe Right(sampleUpscanResponse)
    }

    "return left when the parser returns left" in new Setup {
      when(mockAppConfig.upscanInitiateBaseUrl)
        .thenReturn("http://url/wrongurl")
      when(
        mockHttpClient.POST[UpscanInitiateRequest, UpscanInitiateResponse](
          any(),
          any(),
          any()
        )(any(), any(), any(), any())
      ).thenReturn(Future.successful(Left(BadRequest)))

      val result = connector.initiateToUpscan(upscanInitiateDataModel)

      await(result) shouldBe Left(BadRequest)
    }
  }

}
