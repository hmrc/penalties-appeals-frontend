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

import config.AppConfig
import connectors.httpParsers.TimeToPayHttpParser.{TimeToPayResponse, TimeToPayResponseReads}
import javax.inject.Inject
import models.ess.TimeToPayRequestModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class TimeToPayConnector @Inject()(http: HttpClient, appConfig: AppConfig) {

  val postTTP = s"${appConfig.essttpBackendUrl}/essttp-backend/vat/vat-penalties/journey/start"

  def setupJourney(requestModel: TimeToPayRequestModel)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TimeToPayResponse] = {
    http.POST(postTTP, requestModel)(TimeToPayRequestModel.writes, TimeToPayResponseReads, hc, ec)
  }
}
