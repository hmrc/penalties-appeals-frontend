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

package connectors.httpParsers

import models.upscan.UpscanInitiateResponseModel
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logger.logger

object UpscanInitiateHttpParser {
  type UpscanInitiateResponse = Either[ErrorResponse, UpscanInitiateResponseModel]

  implicit object UpscanInitiateResponseReads extends HttpReads[UpscanInitiateResponse] {

    def read(method: String, url: String, response: HttpResponse): UpscanInitiateResponse = {
      response.status match {
        case OK =>
          response.json.validate[UpscanInitiateResponseModel](UpscanInitiateResponseModel.formats) match {
            case JsSuccess(model, _) => Right(model)
            case _ => Left(InvalidJson)
          }
        case BAD_REQUEST =>
          logger.debug(s"[UpScanInitiateResponseReads][read]: Bad request returned with reason: ${response.body}")
          Left(BadRequest)
        case status =>
          logger.warn(s"[UpScanInitiateResponseReads][read]: Unexpected response, status $status returned")
          Left(UnexpectedFailure(status, s"Unexpected response, status $status returned"))
      }
    }
  }

  sealed trait ErrorResponse {
    val status: Int
    val body: String
  }

  case object InvalidJson extends ErrorResponse {
    override val status: Int = BAD_REQUEST
    override val body: String = "Invalid JSON received"
  }

  case object BadRequest extends ErrorResponse {
    override val status: Int = BAD_REQUEST
    override val body: String = "incorrect Json body sent"
  }

  case class UnexpectedFailure(
      override val status: Int,
      override val body: String
  ) extends ErrorResponse
}
