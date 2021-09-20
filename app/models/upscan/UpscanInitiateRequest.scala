
package models.upscan

import play.api.libs.json.{Format, Json, Writes}

case class UpscanInitiateRequest(
                                callbackUrl: String,
                                successRedirect: Option[String] = None,
                                errorRedirect: Option[String] = None,
                                minimumFileSize: Option[Int] = None,
                                maximumFileSize: Option[Int] = None,
                                )
object UpscanInitiateRequest {
  implicit val writes: Writes[UpscanInitiateRequest] = Json.writes[UpscanInitiateRequest]
}