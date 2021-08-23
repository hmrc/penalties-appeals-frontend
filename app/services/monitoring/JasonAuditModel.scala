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

package services.monitoring

import utils.JsonUtils
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsNull, JsString, JsValue}

trait JsonAuditModel extends JsonUtils {
  val auditType: String
  val transactionName: String
  val detail: JsValue

  implicit def booleanToJson(bool: Boolean): JsValueWrapper = if (bool) JsString("Yes") else JsString("No")

  implicit def oBooleanToJson(oBool: Option[Boolean]): JsValueWrapper = (oBool map booleanToJson).fold[JsValueWrapper](JsNull)(x => x)

}