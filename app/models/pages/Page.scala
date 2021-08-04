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

package models.pages

trait Page

case object HonestyDeclarationPage extends Page
case object HasCrimeBeenReportedPage extends Page
case object WhenDidCrimeHappenPage extends Page
case object WhenDidFireOrFloodHappenPage extends Page
case object WhenDidPersonLeaveTheBusinessPage extends Page
case object WhenDidTechnologyIssuesBeginPage extends Page
case object WhenDidTechnologyIssuesEndPage extends Page
case object WasHospitalStayRequiredPage extends Page
case object WhenDidHealthIssueHappenPage extends Page
case object WhenDidHospitalStayBeginPage extends Page
case object DidHospitalStayEndPage extends Page
case object WhenDidBecomeUnablePage extends Page
case object WhyWasReturnSubmittedLatePage extends Page
case object EvidencePage extends Page
case object WhoPlannedToSubmitVATReturnAgentPage extends Page
case object ReasonableExcuseSelectionPage extends Page
case object WhyWasTheReturnSubmittedLateAgentPage extends Page
case object WhenDidThePersonDiePage extends Page
case object CancelVATRegistrationPage extends Page
case object AppealStartPage extends Page
case object OtherRelevantInformationPage extends Page
case object OtherPenaltiesForPeriodPage extends Page