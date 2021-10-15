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

package models.upload

import play.api.libs.json._

case class UpscanInitiateResponseModel(
    reference: String,
    uploadRequest: UploadFormTemplateRequest
)

case class UploadFormTemplateRequest(href: String, fields: Map[String, String])

object UploadFormTemplateRequest {
  implicit val formats: Format[UploadFormTemplateRequest] =
    Json.format[UploadFormTemplateRequest]
}

object UpscanInitiateResponseModel {
  implicit val formats: Format[UpscanInitiateResponseModel] =
    Json.format[UpscanInitiateResponseModel]

  implicit val jsonReadsForModel: Reads[UpscanInitiateResponseModel] =
    new Reads[UpscanInitiateResponseModel] {
      override def reads(
          json: JsValue
      ): JsResult[UpscanInitiateResponseModel] = {
        for {
          reference <- (json \ "reference").validate[String]
          fieldsAndHref <-
            (json \ "uploadRequest").validate[UploadFormTemplateRequest](
              UploadFormTemplateRequest.formats
            )
        } yield {
          UpscanInitiateResponseModel(reference, fieldsAndHref)
        }
      }
    }
}
