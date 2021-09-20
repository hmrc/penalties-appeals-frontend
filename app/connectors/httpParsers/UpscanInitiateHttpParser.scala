
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

  trait ErrorResponse {
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

  case class UnexpectedFailure(override val status: Int, override val body: String) extends ErrorResponse
}
