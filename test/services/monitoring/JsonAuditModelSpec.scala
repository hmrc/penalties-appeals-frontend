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

import base.SpecBase
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}

class JsonAuditModelSpec extends SpecBase with Matchers{
  val JsonAuditModelObject: JsonAuditModel = new JsonAuditModel {
    override val auditType: String = "PenaltyAppealSubmitted"
    override val detail: JsValue = Json.obj("detail" ->
      Json.obj("submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN"))
    override val transactionName: String = "penalties-appeal-submitted"
  }

    "Given an Audit Model, the data passed in the model JsonAuditModel" should {
      "have the values" in {
        val jsonAuditModelObject = new JsonAuditModel {
          override val auditType: String = "PenaltyAppealSubmitted"
          override val detail: JsValue = Json.obj("submittedBy" -> "client",
              "taxIdentifier" -> "123456789",
              "identifierType" -> "VRN")
          override val transactionName: String = "penalties-appeal-submitted"
        }

        jsonAuditModelObject.auditType shouldBe "PenaltyAppealSubmitted"
        jsonAuditModelObject.detail shouldBe Json.parse("""
                        {
                        "submittedBy" : "client",
                        "taxIdentifier" : "123456789",
                         "identifierType" : "VRN"
                        }
                        """)
        jsonAuditModelObject.transactionName shouldBe "penalties-appeal-submitted"

      }
  }
}
