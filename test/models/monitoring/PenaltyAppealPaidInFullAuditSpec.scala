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
import play.api.libs.json.{Json}
import play.api.mvc.AnyContent

class PenaltyAppealPaidInFullAuditSpec extends SpecBase {
  val userRequest = new UserRequest[AnyContent]("123456789", answers = userAnswers(Json.obj()))
  val sampleAuditModel: PenaltyAppealPaidInFullAudit = PenaltyAppealPaidInFullAudit("10000", "123456789")(userRequest)

  "have the correct auditType" in {
    sampleAuditModel.auditType shouldBe "PenaltyFindOutHowToAppealAlreadyPaid"
  }

  "have the correct transactionName" in {
    sampleAuditModel.transactionName shouldBe "penalties-find-out-how-to-appeal-already-paid"
  }

  def auditTestAlreadyPaid(): Unit = {
    "The user goes to pay their vat return" in {
      val auditModel = PenaltyAppealPaidInFullAudit("10000", "123456789")(userRequest)
      auditModel.detail shouldBe Json.obj(
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "chargeReference" -> "123456789",
        "amountToBePaidInPence" -> "10000",
        "canUserPay" -> "already-paid"
      )
    }
  }
auditTestAlreadyPaid()
}
