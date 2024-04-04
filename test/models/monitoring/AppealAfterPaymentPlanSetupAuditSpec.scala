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
import models.UserRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent

class AppealAfterPaymentPlanSetupAuditSpec extends SpecBase {
  val userRequest = new UserRequest[AnyContent]("123456789", answers = userAnswers(Json.obj()))
  val sampleAuditModel: AppealAfterPaymentPlanSetupAudit = AppealAfterPaymentPlanSetupAudit("10000", "123456789", "yes")(userRequest)

  "have the correct auditType" in {
    sampleAuditModel.auditType shouldBe "PenaltyFindOutHowTOAppealSetUpTimeToPay"
  }

  "have the correct transactionName" in {
    sampleAuditModel.transactionName shouldBe "penalties-find-out-how-to-appeal-set-up-ttp"
  }

  def auditTestDidPay(userWentToSetUpTTP: String): Unit = {
    "The user goes to pay their vat return" in {
      val auditModel = AppealAfterPaymentPlanSetupAudit("10000", "123456789", userWentToSetUpTTP)(userRequest)
      auditModel.detail shouldBe Json.obj(
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "chargeReference" -> "123456789",
        "amountTobePaidinPence" -> "10000",
        "canUserPay" -> "yes",
        "userWentToSetUpTTP" -> "yes"
      )
    }
  }

  def auditTestDidNotPay(userWentToSetUpTTP: String): Unit = {
    "The user does not go to pay their vat return" in {
      val auditModel = AppealAfterPaymentPlanSetupAudit("10000", "123456789", userWentToSetUpTTP)(userRequest)
      auditModel.detail shouldBe Json.obj(
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "chargeReference" -> "123456789",
        "amountTobePaidinPence" -> "10000",
        "canUserPay" -> "yes",
        "userWentToSetUpTTP" -> "no"
      )
    }
  }

  auditTestDidPay("yes")
  auditTestDidNotPay("no")
}