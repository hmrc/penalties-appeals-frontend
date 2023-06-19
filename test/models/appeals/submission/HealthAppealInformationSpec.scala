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

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

class HealthAppealInformationSpec extends SpecBase {
  "healthAppealWrites" must {
    "write the appeal to JSON" when {
      "there has been a hospital stay - and is no longer ongoing (both start and end date) - write the appeal model to JSON" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
          endDateOfEvent = Some(LocalDate.parse("2021-04-24").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS)),
          eventOngoing = false,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "health",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T00:00:00",
          "endDateOfEvent" -> "2021-04-24T00:00:01",
          "eventOngoing" -> false,
          "hospitalStayInvolved" -> true,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }

      "there has been a hospital stay AND it is ongoing (no end date) - write the appeal model to JSON" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
          endDateOfEvent = None,
          eventOngoing = true,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "health",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T00:00:00",
          "eventOngoing" -> true,
          "hospitalStayInvolved" -> true,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }

      "there has been NO hospital stay (dateOfEvent present, eventOngoing = false, hospitalStayInvolved = false) " +
        "write the appeal model to JSON" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
          endDateOfEvent = None,
          eventOngoing = false,
          hospitalStayInvolved = false,
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "health",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T00:00:00",
          "eventOngoing" -> false,
          "hospitalStayInvolved" -> false,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

}
