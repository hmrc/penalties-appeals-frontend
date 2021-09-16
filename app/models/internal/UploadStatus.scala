
package models.internal

sealed trait UploadStatus
case object InProgress extends UploadStatus
case object Failed extends UploadStatus
case class UploadedSuccessfully(reference: String) extends UploadStatus

case class UploadIs(value: String) extends AnyVal
