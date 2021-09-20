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

package models.upscan

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads}

case class UpscanInitiateResponseModel(
                                   reference: Reference,
                                   uploadRequest: UploadFormTemplateRequest
                                 )
case class Reference(value: String)
case class UploadFormTemplateRequest(href: String, fields: Map[String, String])

object UpscanInitiateResponseModel {
  implicit val formats: Format[UpscanInitiateResponseModel] = Json.format[UpscanInitiateResponseModel]

  implicit val referenceFormat: Format[Reference] = new Format[Reference] {

    override def reads(json: JsValue): JsResult[Reference] = json.validate[String] match {
      case JsSuccess(reference, _) => JsSuccess(Reference(reference))
      case error: JsError => error
    }

    override def writes(reference: Reference): JsValue = JsString(reference.value)
  }

  implicit val jsonFormatUploadForm: Format[UploadFormTemplateRequest] = Json.format[UploadFormTemplateRequest]

  implicit val JsonReadsForModel: Reads[UpscanInitiateResponseModel] = new Reads[UpscanInitiateResponseModel] {
    override def reads(json: JsValue): JsResult[UpscanInitiateResponseModel] = {
      for {
        reference <- (json \ "reference").validate[Reference](referenceFormat)
        fieldsAndHref <- (json \ "uploadRequest").validate[UploadFormTemplateRequest](jsonFormatUploadForm)
      } yield {
        UpscanInitiateResponseModel(reference, fieldsAndHref)
      }
    }
  }
}