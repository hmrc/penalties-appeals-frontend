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

package base

import models.PenaltyTypeEnum
import play.api.libs.json.{JsObject, Json}
import utils.SessionKeys

import java.time.LocalDate

trait UserAnswersBase {

  val correctUserAnswers: JsObject = Json.obj(
    SessionKeys.penaltyNumber -> "123",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
    SessionKeys.journeyId -> "1234"
  )

  val correctLPPUserAnswers: JsObject = correctUserAnswers ++ Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment
  )

  val correctAdditionalLPPUserAnswers: JsObject = correctUserAnswers ++ Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Additional
  )

  val agentAnswers: JsObject = Json.obj(
    SessionKeys.agentSessionVrn -> "VRN123456789",
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
  )

  val hasConfirmedDeclaration: JsObject = correctUserAnswers ++ Json.obj(
    SessionKeys.hasConfirmedDeclaration -> true
  )

  val crimeAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.dateOfCrime -> "2022-01-01"
  ) ++ hasConfirmedDeclaration

  val fireOrFloodAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "fireOrFlood",
    SessionKeys.hasConfirmedDeclaration -> true,
    SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
  ) ++ hasConfirmedDeclaration

  val lossOfStaffAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "lossOfStaff",
    SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
  ) ++ hasConfirmedDeclaration

  val techIssuesAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "technicalIssues",
    SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
    SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
  ) ++ hasConfirmedDeclaration

  val noHospitalStayAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "health",
    SessionKeys.wasHospitalStayRequired -> "no",
    SessionKeys.whenHealthIssueHappened -> "2022-01-02"
  ) ++ hasConfirmedDeclaration

  val hospitalOngoingAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "health",
    SessionKeys.wasHospitalStayRequired -> "yes",
    SessionKeys.hasHealthEventEnded -> "no",
    SessionKeys.whenHealthIssueStarted -> "2022-01-02"
  ) ++ hasConfirmedDeclaration

  val hospitalEndedAnswers: JsObject = hospitalOngoingAnswers ++ Json.obj(
    SessionKeys.hasHealthEventEnded -> "yes",
    SessionKeys.whenHealthIssueEnded -> "2022-01-03"
  )

  val otherAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "other",
    SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
    SessionKeys.whenDidBecomeUnable -> "2022-01-02",
    SessionKeys.journeyId -> "4321",
    SessionKeys.isUploadEvidence -> "yes"
  ) ++ hasConfirmedDeclaration

  val crimeNoReasonAnswers: JsObject = Json.obj(
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.dateOfCrime -> "2022-01-01"
  ) ++ hasConfirmedDeclaration

  val crimeMissingAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.dateOfCrime -> "2022-01-01"
  ) ++ hasConfirmedDeclaration

  val bereavementAnswers: JsObject = Json.obj(
    SessionKeys.reasonableExcuse -> "bereavement",
    SessionKeys.whenDidThePersonDie -> "2021-01-01"
  ) ++ hasConfirmedDeclaration

  val agentLPPAnswers: JsObject = Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
    SessionKeys.agentSessionVrn -> "VRN1234",
    SessionKeys.reasonableExcuse -> "bereavement",
    SessionKeys.whenDidThePersonDie -> "2021-01-01"
  ) ++ hasConfirmedDeclaration

  val noLateAppealAnswers: JsObject = Json.obj(
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.dateOfCrime -> "2022-01-01"
  ) ++ hasConfirmedDeclaration

  val findOutHowToAppealLPPNonCaAnswers: JsObject = Json.obj(
    SessionKeys.vatAmount -> BigDecimal(123.45),
    SessionKeys.principalChargeReference -> "123456789",
    SessionKeys.isCaLpp -> false
  ) ++ correctLPPUserAnswers

  val appealAfterVatIsPaidAnswers: JsObject = Json.obj(
    SessionKeys.principalChargeReference -> "123456789",
    SessionKeys.vatAmount -> BigDecimal(100)
  )

  val appealAfterVatIsFullyPaidAnswers: JsObject = Json.obj(
    SessionKeys.principalChargeReference -> "123456789",
    SessionKeys.vatAmount -> BigDecimal(100)
  )

  val appealAfterPaymentPlanSetupAnswers: JsObject = Json.obj(
    SessionKeys.principalChargeReference -> "123456789",
    SessionKeys.vatAmount -> BigDecimal(100)
  )
}
