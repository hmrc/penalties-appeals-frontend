
package controllers.internal

import java.time.Instant

import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{Action, MessagesControllerComponents}
import services.internal.UpscanCallbackDispatcher
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger.logger

import scala.concurrent.ExecutionContext


sealed trait CallbackBody {
  def reference: String
}

case class ReadyCallbackBody(
                          reference: String
                       ) extends CallbackBody

case class FailedCallbackBody(
                           reference: String
                         ) extends CallbackBody

object CallbackBody {

  implicit val uploadDetailsReads: Reads[UploadDetails] = Json.reads[UploadDetails]

  implicit val errorDetailsReads: Reads[ErrorDetails] = Json.reads[ErrorDetails]

  implicit val readyCallbackBodyReads: Reads[ReadyCallbackBody] = Json.reads[ReadyCallbackBody]

  implicit val failedCallbackBodyReads: Reads[FailedCallbackBody] = Json.reads[FailedCallbackBody]

  implicit val reads: Reads[CallbackBody] = new Reads[CallbackBody] {
    override def reads(json: JsValue): JsResult[CallbackBody] = json \ "fileStatus" match {
      case JsDefined(JsString("READY")) => implicitly[Reads[ReadyCallbackBody]].reads(json)
      case JsDefined(JsString("FAILED")) => implicitly[Reads[FailedCallbackBody]].reads(json)
      case JsDefined(value) => JsError(s"Invalid type distriminator: $value")
      case JsUndefined() => JsError(s"Missing type distriminator")
    }
  }
}

case class UploadDetails(reference: String)

case class ErrorDetails(reference: String)

class UpScanCallBackController @Inject()(upscanCallBackDispatcher: UpscanCallbackDispatcher)
                                        (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) {


  val callBackFromUpscan: Action[JsValue] = Action.async(parse.json) { implicit request =>
      logger.debug(s"[UpScanCallBackController][callBackFromUpscan] Callback from upscan data")
      withJsonBody[CallbackBody] { feedback: CallbackBody =>
        upscanCallBackDispatcher.handleCallback(feedback).map(_ => Ok)
      }
    }
}
