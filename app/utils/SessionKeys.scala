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
  val hasConfirmedDeclaration: String = "hasConfirmedDeclaration"
  val hasCrimeBeenReportedToPolice = "hasCrimeBeenReportedToPolice"

  val allKeys: Seq[String] = Seq(
    appealType,
    startDateOfPeriod,
    endDateOfPeriod,
    dueDateOfPeriod,
    penaltyId,
    reasonableExcuse,
    dateOfCrime,
    hasConfirmedDeclaration,
    hasCrimeBeenReportedToPolice,
    dateCommunicationSent
  )
}
