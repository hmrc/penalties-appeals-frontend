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

import base.SpecBase
import models.AuthRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent

class PenaltyAppealStartedAuditModelSpec extends SpecBase {
  val authRequest = new AuthRequest[AnyContent]("123456789")
  val agentAuthRequest = new AuthRequest[AnyContent]("123456789", arn = Some("BARN123456789"))
  val sampleAuditModel: PenaltyAppealStartedAuditModel = PenaltyAppealStartedAuditModel("123456789000", AuditPenaltyTypeEnum.FirstLPP)(authRequest)

  "have the correct auditType" in {
    sampleAuditModel.auditType shouldBe "PenaltyAppealStarted"
  }

  "have the correct transactionName" in {
    sampleAuditModel.transactionName shouldBe "penalties-appeal-started"
  }

  def auditTest(penaltyType: AuditPenaltyTypeEnum.Value): Unit = {
    s"have the correct details for $penaltyType - Trader" in {
      val auditModel = PenaltyAppealStartedAuditModel("123456789000", penaltyType)(authRequest)
      auditModel.detail shouldBe Json.obj(
        "startedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "penaltyNumber" -> "123456789000",
        "penaltyType" -> penaltyType
      )
    }
  }

  def auditAgentTest(penaltyType: AuditPenaltyTypeEnum.Value): Unit = {
    s"have the correct details for $penaltyType - Agent" in {
      val auditModel = PenaltyAppealStartedAuditModel("123456789000", penaltyType)(agentAuthRequest)
      auditModel.detail shouldBe Json.obj(
        "startedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "agentReferenceNumber" -> "BARN123456789",
        "identifierType" -> "VRN",
        "penaltyNumber" -> "123456789000",
        "penaltyType" -> penaltyType
      )
    }
  }

  auditTest(AuditPenaltyTypeEnum.LSP)
  auditTest(AuditPenaltyTypeEnum.FirstLPP)
  auditTest(AuditPenaltyTypeEnum.SecondLPP)

  auditAgentTest(AuditPenaltyTypeEnum.LSP)
  auditAgentTest(AuditPenaltyTypeEnum.FirstLPP)
  auditAgentTest(AuditPenaltyTypeEnum.SecondLPP)
}