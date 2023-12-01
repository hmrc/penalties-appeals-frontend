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

case object WhenDidHospitalStayEndPage extends Page

case object DidHospitalStayEndPage extends Page

case object WhenDidBecomeUnablePage extends Page

case object WhyWasReturnSubmittedLatePage extends Page

case object EvidencePage extends Page

case object WhoPlannedToSubmitVATReturnAgentPage extends Page

case object ReasonableExcuseSelectionPage extends Page

case object WhatCausedYouToMissTheDeadlinePage extends Page

case object WhenDidThePersonDiePage extends Page

case object CancelVATRegistrationPage extends Page

case object AppealStartPage extends Page

case object OtherRelevantInformationPage extends Page

case object YouCannotAppealPage extends Page

case object UploadFirstDocumentPage extends Page

case object UploadAnotherDocumentPage extends Page

case object FileListPage extends Page

case object UploadEvidenceQuestionPage extends Page

case object RemoveFilePage extends Page

case object YouCanAppealThisPenaltyPage extends Page

case object MakingALateAppealPage extends Page

case object CheckYourAnswersPage extends Page

case object PenaltySelectionPage extends Page

case object AppealSinglePenaltyPage extends Page

case object AppealCoverBothPenaltiesPage extends Page

case object AppealByLetterKickOutPage extends Page

case object YouCannotGoBackToAppealPage extends Page

case object ViewAppealDetailsPage extends Page

case object AppealAfterPaymentPlanSetUpPage extends Page

case object YouCanAppealOnlinePage extends Page

case object OtherWaysToAppealPage extends Page

case object HowToAppealPage extends Page

case object CanYouPayPage extends Page

case object IfYouvePaidYourVATPage extends Page

case object HasBusinessAskedHMRCToCancelRegistrationPage extends Page

case object HasHMRCConfirmedRegistrationCancellationPage extends Page

case object ActionsToTakeBeforeAppealingOnlinePage extends Page

object Page {
  val allPages: Seq[Page] = Seq(
    HonestyDeclarationPage,
    HasCrimeBeenReportedPage,
    WhenDidCrimeHappenPage,
    WhenDidFireOrFloodHappenPage,
    WhenDidPersonLeaveTheBusinessPage,
    WhenDidTechnologyIssuesBeginPage,
    WhenDidTechnologyIssuesEndPage,
    WasHospitalStayRequiredPage,
    WhenDidHealthIssueHappenPage,
    WhenDidHospitalStayBeginPage,
    DidHospitalStayEndPage,
    WhenDidBecomeUnablePage,
    WhyWasReturnSubmittedLatePage,
    EvidencePage,
    WhoPlannedToSubmitVATReturnAgentPage,
    ReasonableExcuseSelectionPage,
    WhatCausedYouToMissTheDeadlinePage,
    WhenDidThePersonDiePage,
    CancelVATRegistrationPage,
    AppealStartPage,
    OtherRelevantInformationPage,
    YouCannotAppealPage,
    UploadFirstDocumentPage,
    UploadAnotherDocumentPage,
    FileListPage,
    UploadEvidenceQuestionPage,
    YouCanAppealThisPenaltyPage,
    MakingALateAppealPage,
    CheckYourAnswersPage,
    RemoveFilePage,
    PenaltySelectionPage,
    AppealSinglePenaltyPage,
    AppealCoverBothPenaltiesPage,
    AppealByLetterKickOutPage,
    WhenDidHospitalStayEndPage,
    YouCannotGoBackToAppealPage,
    ViewAppealDetailsPage,
    AppealAfterPaymentPlanSetUpPage,
    OtherWaysToAppealPage,
    YouCanAppealOnlinePage,
    HowToAppealPage,
    CanYouPayPage,
    IfYouvePaidYourVATPage,
    HasBusinessAskedHMRCToCancelRegistrationPage,
    HasHMRCConfirmedRegistrationCancellationPage,
    ActionsToTakeBeforeAppealingOnlinePage
  )

  def find(pageName: String): Page = allPages.find(_.toString == pageName).get
}
