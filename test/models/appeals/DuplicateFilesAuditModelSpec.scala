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

package models.appeals

import base.SpecBase
import models.monitoring.DuplicateFilesAuditModel
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

class DuplicateFilesAuditModelSpec  extends SpecBase {
  val sampleDate: LocalDate = LocalDate.of(2020, 1, 1)
  val duplicateFilesPayload: JsValue = Json.arr(
    Json.obj(
      "upscanReference" -> "ref1",
      "uploadTimestamp" -> sampleDate,
      "name" -> "file1.txt",
      "mimeType" -> "text/plain",
      "size" -> 100,
      "checksum" -> "123456789"
    ),
    Json.obj(
      "upscanReference" -> "ref2",
      "uploadTimestamp" -> sampleDate,
      "name" -> "file1.txt",
      "mimeType" -> "text/plain",
      "size" -> 100,
      "checksum" -> "123456789"
    )
  )

  "DuplicateFilesAuditModel" should {
    "the audit type should be correct" in {
      val model: DuplicateFilesAuditModel = DuplicateFilesAuditModel(duplicateFilesPayload)(userRequestWithCorrectKeys)
      model.auditType shouldBe "PenaltyDuplicateFilesSubmitted"
    }

    "the transaction name should be correct" in {
      val model: DuplicateFilesAuditModel = DuplicateFilesAuditModel(duplicateFilesPayload)(userRequestWithCorrectKeys)
      model.auditType shouldBe "PenaltyDuplicateFilesSubmitted"
    }

    "the detail section should be correct (for client)" in {
      val model: DuplicateFilesAuditModel = DuplicateFilesAuditModel(duplicateFilesPayload)(userRequestWithCorrectKeys)
      model.detail shouldBe Json.obj(
        "submittedBy" -> "client",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "duplicateFiles" -> duplicateFilesPayload
      )
    }

    "the detail section should be correct (for agent)" in {
      val model: DuplicateFilesAuditModel = DuplicateFilesAuditModel(duplicateFilesPayload)(agentUserAgentSubmitButClientWasLateSessionKeys)
      model.detail shouldBe Json.obj(
        "submittedBy" -> "agent",
        "taxIdentifier" -> "123456789",
        "identifierType" -> "VRN",
        "agentReferenceNumber" -> "AGENT1",
        "duplicateFiles" -> duplicateFilesPayload
      )
    }
  }

}
