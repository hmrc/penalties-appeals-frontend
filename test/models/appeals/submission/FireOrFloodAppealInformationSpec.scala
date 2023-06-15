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

package models.appeals.submission

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDate

class FireOrFloodAppealInformationSpec extends SpecBase {
  "fireOrFloodAppealWrites" must {
    "write the appeal model to Json" in {
      val model = FireOrFloodAppealInformation(
        reasonableExcuse = "fireandflood",
        honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
        statement = None,
        lateAppeal = true,
        lateAppealReason = Some("Reason"),
        isClientResponsibleForSubmission = Some(false),
        isClientResponsibleForLateSubmission = Some(true)
      )
      val result = Json.toJson(model)(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
      result shouldBe Json.obj(
        "reasonableExcuse" -> "fireandflood",
        "honestyDeclaration" -> true,
        "startDateOfEvent" -> "2021-04-23T00:00:00",
        "lateAppeal" -> true,
        "lateAppealReason" -> "Reason",
        "isClientResponsibleForSubmission" -> false,
        "isClientResponsibleForLateSubmission" -> true
      )
    }
  }
}
