
package services.internal

import controllers.internal.{CallbackBody, FailedCallbackBody, ReadyCallbackBody}
import javax.inject.Inject
import models.internal.{Failed, UploadedSuccessfully}

import scala.concurrent.Future

class UpscanCallbackDispatcher @Inject()(sessionStorage: UploadProgressTracker) {

  def handleCallback(callBack: CallbackBody): Future[Unit] = {

    val uploadStatus = callBack match {
      case s: ReadyCallbackBody =>
        UploadedSuccessfully(
          s.reference
        )
      case _: FailedCallbackBody =>
        Failed
    }

    sessionStorage.registerUploadResult(callBack.reference, uploadStatus)
  }
}
