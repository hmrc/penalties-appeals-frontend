
package services.internal

import models.internal.UploadStatus

import scala.concurrent.Future

trait UploadProgressTracker {

  def registerUploadResult(reference : String, uploadStatus : UploadStatus): Future[Unit]

}
