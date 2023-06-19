/*
 * Copyright 2023 HM Revenue & Customs
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

import models.AuthRequest
import play.api.libs.json.JsValue
import services.monitoring.JsonAuditModel
import utils.JsonUtils

case class PenaltyAppealStartedAuditModel(penaltyNumber: String, penaltyType: AuditPenaltyTypeEnum.Value)
                                    (implicit request: AuthRequest[_]) extends JsonAuditModel with JsonUtils {
  override val auditType: String = "PenaltyAppealStarted"
  override val transactionName: String = "penalties-appeal-started"
  override val detail: JsValue = jsonObjNoNulls(
    "startedBy" -> (if(request.isAgent) "agent" else "client"),
    "taxIdentifier" -> request.vrn,
    "identifierType" -> "VRN",
    "agentReferenceNumber" -> request.arn,
    "penaltyNumber" -> penaltyNumber,
    "penaltyType" -> penaltyType
  )
}
