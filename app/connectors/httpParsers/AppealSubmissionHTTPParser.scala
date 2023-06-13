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

import models.appeals.AppealSubmissionResponseModel
import play.api.http.Status.{CONFLICT, MULTI_STATUS, OK}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys.{INVALID_JSON_RECEIVED_FROM_PENALTIES, RECEIVED_4XX_FROM_PENALTIES, RECEIVED_5XX_FROM_PENALTIES}

object AppealSubmissionHTTPParser {
  type AppealSubmissionResponse = Either[ErrorResponse, AppealSubmissionResponseModel]

  implicit object AppealSubmissionReads extends HttpReads[AppealSubmissionResponse] {
    val startOfLogging = "[AppealSubmissionParser][AppealSubmissionReads] -"

    def read(method: String, url: String, response: HttpResponse): AppealSubmissionResponse = {
      response.status match {
        case OK | MULTI_STATUS =>
          logger.info(s"$startOfLogging Received ${response.status} Response from penalties backend")
          response.json.validate[AppealSubmissionResponseModel] match {
            case JsSuccess(model, _) => Right(model)
            case _ =>
              PagerDutyHelper.log("AppealSubmissionReads", INVALID_JSON_RECEIVED_FROM_PENALTIES)
              Left(InvalidJson)
          }
        case CONFLICT =>
          logger.warn(s"$startOfLogging Conflict status has been returned with body: ${response.body}")
          Left(UnexpectedFailure(CONFLICT, response.body))
        case status =>
          PagerDutyHelper.logStatusCode("AppealSubmissionReads", status)(RECEIVED_4XX_FROM_PENALTIES, RECEIVED_5XX_FROM_PENALTIES)
          logger.warn(s"$startOfLogging Unexpected response, status $status returned with body: ${response.body}")
          Left(UnexpectedFailure(status, response.body))
      }
    }
  }
}
