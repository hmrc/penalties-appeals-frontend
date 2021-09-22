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

import config.AppConfig
import connectors.httpParsers.UpscanInitiateHttpParser.{
  UpscanInitiateResponse,
  UpscanInitiateResponseReads
}
import javax.inject.Inject
import models.upscan.UpscanInitiateRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig) {

  val postInitiateUrl: String =
    s"${appConfig.upscanInitiateBaseUrl}/upscan/v2/initiate"

  def initiateToUpscan(request: UpscanInitiateRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[UpscanInitiateResponse] = {
    httpClient.POST[UpscanInitiateRequest, UpscanInitiateResponse](
      postInitiateUrl,
      request
    )(UpscanInitiateRequest.writes, UpscanInitiateResponseReads, hc, ec)
  }
}
