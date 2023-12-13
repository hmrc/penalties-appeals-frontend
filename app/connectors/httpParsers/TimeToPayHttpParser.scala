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

package connectors.httpParsers

import models.ess.TimeToPayResponseModel
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger

object TimeToPayHttpParser {

  type TimeToPayResponse = Either[ErrorResponse, TimeToPayResponseModel]

  implicit object TimeToPayResponseReads extends HttpReads[TimeToPayResponse] {
    def read(method: String, url: String, response: HttpResponse): TimeToPayResponse = {
      response.status match {
        case CREATED =>
          logger.debug(s"[TimeToPayHttpParser][read] - Response body: ${response.body}")
          Right(response.json.as[TimeToPayResponseModel])
        case _ =>
          logger.warn(s"[TimeToPayHttpParser][read] - Unexpected error received: ${response.body}")
          Left(UnexpectedFailure(response.status, response.body))
      }
    }
  }

}
