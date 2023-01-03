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

import models.appeals.MultiplePenaltiesData
import play.api.http.Status.{OK, NO_CONTENT}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys._


object MultiplePenaltiesHttpParser {
  type MultiplePenaltiesResponse = Either[ErrorResponse, MultiplePenaltiesData]


  implicit object MultiplePenaltiesResponseReads extends HttpReads[MultiplePenaltiesResponse] {
    val startOfLogging = "[MultiplePenaltiesHttpParser][MultiplePenaltiesResponseReads] -"

    def read(method: String, url: String, response: HttpResponse): MultiplePenaltiesResponse = {
      response.status match {
        case OK =>
          logger.info(s"$startOfLogging Received $OK Response from penalties backend")
          response.json.validate[MultiplePenaltiesData](MultiplePenaltiesData.format) match {
            case JsSuccess(model, _) => Right(model)
            case _ =>
              PagerDutyHelper.log("MultiplePenaltiesResponseReads", INVALID_JSON_RECEIVED_FROM_PENALTIES)
              Left(InvalidJson)
          }
        case NO_CONTENT =>
          logger.debug(s"$startOfLogging $NO_CONTENT returned from penalties backend")
          Left(NoContent)
        case status =>
          PagerDutyHelper.logStatusCode("MultiplePenaltiesResponseReads", status)(RECEIVED_4XX_FROM_PENALTIES, RECEIVED_5XX_FROM_PENALTIES)
          logger.warn(s"$startOfLogging Unexpected response, status $status returned")
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
    }
  }
}
