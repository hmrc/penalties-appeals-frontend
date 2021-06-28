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

package utils

object SessionKeys {
  val appealType: String = "appealType"
  val startDateOfPeriod: String = "periodStart"
  val endDateOfPeriod: String = "periodEnd"
  val dueDateOfPeriod: String = "periodDueDate"
  val dateCommunicationSent: String = "dateCommunicationSent"
  val penaltyId: String = "penaltyId"
  val reasonableExcuse: String = "reasonableExcuse"
  val dateOfCrime: String = "dateOfCrime"
  val dateOfFireOrFlood: String = "dateOfFireOrFlood"
  val hasConfirmedDeclaration: String = "hasConfirmedDeclaration"
  val hasCrimeBeenReportedToPolice = "hasCrimeBeenReportedToPolice"
  val lateAppealReason = "lateAppealReason"
  val whenPersonLeftTheBusiness = "whenPersonLeftTheBusiness"
  val whenDidTechnologyIssuesBegin = "whenDidTechnologyIssuesBegin"
  val whenDidTechnologyIssuesEnd = "whenDidTechnologyIssuesEnd"
  val wasHospitalStayRequired = "wasHospitalStayRequired"
  val whenHealthIssueHappened = "whenHealthIssueHappened"
  //TODO: use when implementing 'yes' journey for hospital stay
  val whenHealthIssueStarted = "whenHealthIssueStarted"
  val whenHealthIssueEnded = "whenHealthIssueEnded"
  val isHealthEventOngoing = "isHealthEventOngoing"
  val whyReturnSubmittedLate = "whyReturnSubmittedLate"
  val whenDidBecomeUnable = "whenDidBecomeUnable"
  val evidenceFileName = "evidenceFileName"
  val whyReturnSubmittedLate = "whyReturnSubmittedLate"
  val evidenceFileName = "evidenceFileName"

  val allKeys: Seq[String] = Seq(
    appealType,
    startDateOfPeriod,
    endDateOfPeriod,
    dueDateOfPeriod,
    penaltyId,
    reasonableExcuse,
    dateOfCrime,
    dateOfFireOrFlood,
    hasConfirmedDeclaration,
    hasCrimeBeenReportedToPolice,
    lateAppealReason,
    dateCommunicationSent,
    whenPersonLeftTheBusiness,
    whenDidTechnologyIssuesBegin,
    whenDidTechnologyIssuesEnd,
    wasHospitalStayRequired,
    whenHealthIssueHappened,
    whenHealthIssueStarted,
    whenHealthIssueEnded,
    isHealthEventOngoing,
    whyReturnSubmittedLate,
    whenDidBecomeUnable,
    whyReturnSubmittedLate,
    evidenceFileName
  )
}
