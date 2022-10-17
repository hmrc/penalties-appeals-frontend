/*
 * Copyright 2022 HM Revenue & Customs
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

import models.upload.UpscanInitiateResponseModel
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys._

object UpscanInitiateHttpParser {
  type UpscanInitiateResponse = Either[ErrorResponse, UpscanInitiateResponseModel]

  implicit object UpscanInitiateResponseReads extends HttpReads[UpscanInitiateResponse] {

    def read(method: String, url: String, response: HttpResponse): UpscanInitiateResponse = {
      response.status match {
        case OK =>
          response.json.validate[UpscanInitiateResponseModel](UpscanInitiateResponseModel.formats) match {
            case JsSuccess(model, _) => Right(model)
            case _ =>
              PagerDutyHelper.log("UpscanInitiateResponseReads",INVALID_JSON_RECEIVED_FROM_UPSCAN)
              Left(InvalidJson)
          }
        case BAD_REQUEST =>
          PagerDutyHelper.log("UpscanInitiateResponseReads",RECEIVED_4XX_FROM_UPSCAN)
          logger.debug(s"[UpScanInitiateResponseReads][read]: Bad request returned with reason: ${response.body}")
          Left(BadRequest)
        case status =>
          PagerDutyHelper.logStatusCode("UpscanInitiateResponseReads", status)(RECEIVED_4XX_FROM_UPSCAN, RECEIVED_5XX_FROM_UPSCAN)
          logger.warn(s"[UpScanInitiateResponseReads][read]: Unexpected response, status $status returned")
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
    }
  }
}
