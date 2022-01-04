/*
 * Copyright 2022 HM Revenue & Customs
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

package models.monitoring

import models.UserRequest
import play.api.libs.json._
import services.monitoring.JsonAuditModel
import utils.JsonUtils

case class DuplicateFilesAuditModel(duplicateFiles: JsValue)
                                   (implicit request: UserRequest[_]) extends JsonAuditModel with JsonUtils {
  override val auditType: String = "PenaltyDuplicateFilesSubmitted"
  override val transactionName: String = "penalties-duplicate-files-submitted"
  override val detail: JsValue = jsonObjNoNulls(
    "submittedBy" -> (if(request.isAgent) "agent" else "client"),
    "taxIdentifier" -> request.vrn,
    "identifierType" -> "VRN",
    "agentReferenceNumber" -> request.arn,
    "duplicateFiles" -> duplicateFiles
  )
}
