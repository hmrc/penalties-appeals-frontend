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

package utils

object SessionKeys {
  val appealType = "appealType"
  val startDateOfPeriod = "periodStart"
  val endDateOfPeriod = "periodEnd"
  val dueDateOfPeriod = "periodDueDate"
  val dateCommunicationSent = "dateCommunicationSent"
  val penaltyNumber = "penaltyNumber"
  val reasonableExcuse = "reasonableExcuse"
  val dateOfCrime = "dateOfCrime"
  val dateOfFireOrFlood = "dateOfFireOrFlood"
  val hasConfirmedDeclaration = "hasConfirmedDeclaration"
  val hasCrimeBeenReportedToPolice = "hasCrimeBeenReportedToPolice"
  val lateAppealReason = "lateAppealReason"
  val whenPersonLeftTheBusiness = "whenPersonLeftTheBusiness"
  val whenDidTechnologyIssuesBegin = "whenDidTechnologyIssuesBegin"
  val whenDidTechnologyIssuesEnd = "whenDidTechnologyIssuesEnd"
  val wasHospitalStayRequired = "wasHospitalStayRequired"
  val whenHealthIssueHappened = "whenHealthIssueHappened"
  val whenHealthIssueStarted = "whenHealthIssueStarted"
  val whenHealthIssueEnded = "whenHealthIssueEnded"
  val hasHealthEventEnded = "hasHealthEventEnded"
  val whenDidBecomeUnable = "whenDidBecomeUnable"
  val whyReturnSubmittedLate = "whyReturnSubmittedLate"
  val whoPlannedToSubmitVATReturn = "whoPlannedToSubmitVATReturn"
  val agentSessionVrn = "mtdVatvcClientVrn"
  val whatCausedYouToMissTheDeadline = "whatCausedYouToMissTheDeadline"
  val whenDidThePersonDie = "whenDidThePersonDie"
  val isObligationAppeal = "isObligationAppeal"
  val cancelVATRegistration = "cancelVATRegistration"
  val otherRelevantInformation = "otherRelevantInformation"
  val journeyId = "journeyId"
  val fileReference = "fileReference"
  val isAddingAnotherDocument = "isAddingAnotherDocument"
  val errorCodeFromUpscan = "errorCodeFromUpscan"
  val failureMessageFromUpscan = "failureMessageFromUpscan"
  val isUploadEvidence = "isUploadEvidence"
  val youCanAppealThisPenalty = "youCanAppealThisPenalty"
  val originatingChangePage = "originatingChangePage"
  val doYouWantToAppealBothPenalties = "doYouWantToAppealBothPenalties"
  val firstPenaltyChargeReference = "firstPenaltyChargeReference"
  val firstPenaltyAmount = "firstPenaltyAmount"
  val secondPenaltyChargeReference = "secondPenaltyChargeReference"
  val secondPenaltyAmount = "secondPenaltyAmount"
  val firstPenaltyCommunicationDate = "firstPenaltyCommunicationDate"
  val secondPenaltyCommunicationDate = "secondPenaltyCommunicationDate"
  val penaltiesHasSeenConfirmationPage = "penaltiesHasSeenConfirmationPage"
  val previouslySubmittedJourneyId = "previouslySubmittedJourneyId"
  val fileNames = "fileNames"

  val allKeys: Seq[String] = Seq(
    appealType,
    startDateOfPeriod,
    endDateOfPeriod,
    dueDateOfPeriod,
    penaltyNumber,
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
    whenDidBecomeUnable,
    whyReturnSubmittedLate,
    hasHealthEventEnded,
    whoPlannedToSubmitVATReturn,
    whatCausedYouToMissTheDeadline,
    whenDidThePersonDie,
    isObligationAppeal,
    cancelVATRegistration,
    otherRelevantInformation,
    errorCodeFromUpscan,
    failureMessageFromUpscan,
    fileReference,
    isAddingAnotherDocument,
    isUploadEvidence,
    youCanAppealThisPenalty,
    originatingChangePage,
    doYouWantToAppealBothPenalties,
    firstPenaltyChargeReference,
    firstPenaltyAmount,
    secondPenaltyChargeReference,
    secondPenaltyAmount,
    firstPenaltyCommunicationDate,
    secondPenaltyCommunicationDate
  )
}
