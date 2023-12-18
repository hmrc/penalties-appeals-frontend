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

package navigation

import config.AppConfig
import config.featureSwitches.{FeatureSwitching, ShowFindOutHowToAppealLSPJourney, ShowFullAppealAgainstTheObligation}
import controllers.routes
import helpers.{DateTimeHelper, IsLateAppealHelper}
import models.pages._
import models._
import play.api.Configuration
import play.api.mvc.Call
import utils.Logger.logger
import utils.{ReasonableExcuses, SessionKeys}

import java.time.LocalDate
import javax.inject.Inject

class Navigation @Inject()(dateTimeHelper: DateTimeHelper,
                           appConfig: AppConfig,
                           isLateAppealHelper: IsLateAppealHelper)(implicit val config: Configuration) extends FeatureSwitching {

  lazy val reverseNormalRoutes: Map[Page, (UserRequest[_], Boolean) => Call] = Map(
    CancelVATRegistrationPage -> ((_, _) => Call("GET", appConfig.penaltiesFrontendUrl)),
    YouCannotAppealPage -> ((_, _) => routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()),
    YouCanAppealThisPenaltyPage -> ((_, _) => routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()),
    AppealStartPage -> ((request, _) => reverseRouteForAppealStartPage(request)),
    HonestyDeclarationPage -> ((request, _) => reverseRouteForHonestyDeclaration(request)),
    OtherRelevantInformationPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    UploadEvidenceQuestionPage -> ((request, _) => reverseRouteForUploadEvidenceQuestion(request, NormalMode)),
    FileListPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    UploadFirstDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    UploadAnotherDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    EvidencePage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    WhenDidThePersonDiePage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    HasCrimeBeenReportedPage -> ((_, _) => routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode)),
    WhenDidCrimeHappenPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidFireOrFloodHappenPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WasHospitalStayRequiredPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidHealthIssueHappenPage -> ((_, _) => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode)),
    WhenDidHospitalStayBeginPage -> ((_, _) => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode)),
    DidHospitalStayEndPage -> ((_, _) => routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode)),
    WhenDidHospitalStayEndPage -> ((_, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidTechnologyIssuesBeginPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidTechnologyIssuesEndPage -> ((_, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)),
    WhenDidBecomeUnablePage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhyWasReturnSubmittedLatePage -> ((_, _) => routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(NormalMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((_, _) => routes.AppealStartController.onPageLoad()),
    WhatCausedYouToMissTheDeadlinePage -> ((_, _) => routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)),
    ReasonableExcuseSelectionPage -> ((request, _) => reverseRouteForReasonableExcuseSelectionPage(request, NormalMode)),
    MakingALateAppealPage -> ((request, jsEnabled) => reverseRouteForMakingALateAppealPage(request, NormalMode, jsEnabled)),
    CheckYourAnswersPage -> ((request, jsEnabled) => reverseRouteForCYAPage(request, NormalMode, jsEnabled)),
    PenaltySelectionPage -> ((_, _) => routes.AppealStartController.onPageLoad()),
    AppealSinglePenaltyPage -> ((_, _) => routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode)),
    AppealCoverBothPenaltiesPage -> ((_, _) => routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode)),
    AppealByLetterKickOutPage -> ((_, _) => reverseRouteForAppealByLetterPage()),
    OtherWaysToAppealPage -> ((request, _) => reverseRouteForOtherWaysToAppeal(request)),
    HasBusinessAskedHMRCToCancelRegistrationPage -> ((_, _) => Call("GET", appConfig.penaltiesFrontendUrl)),
    HasHMRCConfirmedRegistrationCancellationPage -> ((_, _) => controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad()),
    ActionsToTakeBeforeAppealingOnlinePage -> ((request, _) => reverseRouteForActionsToTakeBeforeAppealingOnlinePage(request)),
    OtherWaysToAppealPage -> ((request, _) => reverseRouteForOtherWaysToAppeal(request)),
    CanYouPayPage -> ((_, _) => Call("GET", appConfig.penaltiesFrontendUrl)),
    YouCanAppealOnlinePage -> ((_, _) => controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad()),
    AppealAfterPaymentPlanSetUpPage -> ((_, _) => controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad()),
    IfYouvePaidYourVATPage -> ((_, _) => controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad()),
    HowToAppealPage -> ((_, _) => Call("GET", appConfig.penaltiesFrontendUrl))
  )

  def reverseCheckingRoutes(page: Page, userRequest: UserRequest[_]): Call = {
    page match {
      case page if userRequest.session.get(SessionKeys.originatingChangePage).contains(page.toString) => routes.CheckYourAnswersController.onPageLoad()
      case DidHospitalStayEndPage => routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode)
      case WhenDidHospitalStayEndPage => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode)
      case WhenDidHospitalStayBeginPage => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode)
      case WhenDidHealthIssueHappenPage => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode)
      case UploadFirstDocumentPage => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode)
      case UploadAnotherDocumentPage => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)
      case EvidencePage => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode)
      case FileListPage => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode)
      case UploadEvidenceQuestionPage => routes.CheckYourAnswersController.onPageLoad()
      case WhenDidTechnologyIssuesEndPage => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode)
      case WhatCausedYouToMissTheDeadlinePage => routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode)
      case AppealSinglePenaltyPage => routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode)
      case AppealCoverBothPenaltiesPage => routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode)
      case _ => throw new MatchError(s"[Navigation][reverseCheckingRoutes] - Unknown page $page")
    }
  }

  lazy val checkingRoutes: Map[Page, (Option[String], UserRequest[_], Option[Boolean]) => Call] = Map(
    HasCrimeBeenReportedPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidCrimeHappenPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidFireOrFloodHappenPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidTechnologyIssuesBeginPage -> ((_, _, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode)),
    WhenDidTechnologyIssuesEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WasHospitalStayRequiredPage -> ((answer, request, _) => routingForHospitalStay(CheckMode, answer, request)),
    WhenDidHealthIssueHappenPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidHospitalStayBeginPage -> ((_, _, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode)),
    DidHospitalStayEndPage -> ((answer, request, _) => routingForHospitalStayEnded(CheckMode, answer, request)),
    WhenDidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidBecomeUnablePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhyWasReturnSubmittedLatePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    EvidencePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request, _) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, CheckMode)),
    WhatCausedYouToMissTheDeadlinePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    ReasonableExcuseSelectionPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidThePersonDiePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    OtherRelevantInformationPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    UploadFirstDocumentPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)),
    FileListPage -> ((answer, request, _) => routeForUploadList(answer, request, CheckMode)),
    UploadEvidenceQuestionPage -> ((answer, request, optJsEnabled) => routeForUploadEvidenceQuestion(answer, request, CheckMode, optJsEnabled)),
    AppealSinglePenaltyPage -> ((_, _, _) => routes.CheckYourAnswersController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    PenaltySelectionPage -> ((answer, _, _) => routingForPenaltySelectionPage(answer, CheckMode)),
    WhenDidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode))
  )

  lazy val normalRoutes: Map[Page, (Option[String], UserRequest[_], Option[Boolean]) => Call] = Map(
    HonestyDeclarationPage -> ((answer, _, _) => getNextURLBasedOnReasonableExcuse(answer, NormalMode)),
    HasCrimeBeenReportedPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidCrimeHappenPage -> ((_, _, _) => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode)),
    WhenDidFireOrFloodHappenPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidTechnologyIssuesBeginPage -> ((_, _, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)),
    WhenDidTechnologyIssuesEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WasHospitalStayRequiredPage -> ((answer, request, _) => routingForHospitalStay(NormalMode, answer, request)),
    WhenDidHealthIssueHappenPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidHospitalStayBeginPage -> ((_, _, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode)),
    DidHospitalStayEndPage -> ((answer, request, _) => routingForHospitalStayEnded(NormalMode, answer, request)),
    WhenDidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidBecomeUnablePage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode)),
    WhyWasReturnSubmittedLatePage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    EvidencePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request, _) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, NormalMode)),
    WhatCausedYouToMissTheDeadlinePage -> ((_, _, _) => routes.ReasonableExcuseController.onPageLoad()),
    ReasonableExcuseSelectionPage -> ((_, _, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidThePersonDiePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    AppealStartPage -> ((_, _, _) => routes.AppealStartController.onPageLoad()),
    OtherRelevantInformationPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    CancelVATRegistrationPage -> ((answer, request, _) => routingForCancelVATRegistrationPage(answer, request)),
    UploadFirstDocumentPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    FileListPage -> ((answer, request, _) => routeForUploadList(answer, request, NormalMode)),
    UploadEvidenceQuestionPage -> ((answer, request, optJsEnabled) => routeForUploadEvidenceQuestion(answer, request, NormalMode, optJsEnabled)),
    YouCanAppealThisPenaltyPage -> ((answer, _, _) => routeForYouCanAppealPenalty(answer)),
    AppealSinglePenaltyPage -> ((_, _, _) => routes.ReasonableExcuseController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, _, _) => routes.ReasonableExcuseController.onPageLoad()),
    PenaltySelectionPage -> ((answer, _, _) => routingForPenaltySelectionPage(answer, NormalMode)),
    WhenDidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    AppealAfterPaymentPlanSetUpPage -> ((answer, _, _) => routingForSetUpPaymentPlanPage(answer)),
    YouCanAppealOnlinePage -> ((answer, _, _) => routingForYouCanAppealOnlinePage(answer)),
    CanYouPayPage -> ((answer, _, _) => routingForCanYouPayPage(answer)),
    HasBusinessAskedHMRCToCancelRegistrationPage -> ((answer, _, _) => routingForHasBusinessAskedHMRCToCancelRegistrationPage(answer)),
    HasHMRCConfirmedRegistrationCancellationPage -> ((answer, _, _) => routingForHasHMRCConfirmedRegistrationCancellationPage(answer)),
    YouCanAppealOnlinePage -> ((answer, _, _) => routingForYouCanAppealOnlinePage(answer))

  )

  def nextPage(page: Page, mode: Mode, answer: Option[String] = None, jsEnabled: Option[Boolean] = None)
              (implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode =>
        //Added answer here so that we can add custom routing to pages that require extra data when answers change
        checkingRoutes(page)(answer, userRequest, jsEnabled)
      case NormalMode =>
        normalRoutes(page)(answer, userRequest, jsEnabled)
    }
  }

  def previousPage(page: Page, mode: Mode, isJsEnabled: Boolean)(implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode =>
        reverseCheckingRoutes(page, userRequest)
      case NormalMode if userRequest.session.get(SessionKeys.originatingChangePage).contains(page.toString) =>
        reverseCheckingRoutes(page, userRequest)
      case NormalMode =>
        reverseNormalRoutes(page)(userRequest, isJsEnabled)
    }
  }

  protected[navigation] def routingForWhoPlannedToSubmitVATReturnAgentPage(answer: Option[String], request: UserRequest[_], mode: Mode): Call = {
    if (answer.get.toLowerCase == "agent") {
      routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(mode)
    } else if (mode == NormalMode) {
      routes.ReasonableExcuseController.onPageLoad()
    } else {
      routeToMakingALateAppealOrCYAPage(request, mode)
    }
  }

  protected[navigation] def routingForCancelVATRegistrationPage(answer: Option[String], request: UserRequest[_]): Call = {
    if (answer.get.toLowerCase == "yes") {
      if (isEnabled(ShowFullAppealAgainstTheObligation)) {
        routes.YouCanAppealPenaltyController.onPageLoad()
      } else {
        routes.YouCannotAppealController.onPageLoadAppealByLetter()
      }
    } else {
      routes.YouCannotAppealController.onPageLoad
    }
  }

  protected[navigation] def routingForPenaltySelectionPage(answer: Option[String], mode: Mode): Call = {
    if (answer.get.toLowerCase == "yes") {
      routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(mode)
    } else {
      routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(mode)
    }
  }

  protected[navigation] def routingForHospitalStayEnded(mode: Mode, answer: Option[String], userRequest: UserRequest[_]): Call = {
    if (answer.get.toLowerCase == "yes") {
      routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(mode)
    } else {
      routeToMakingALateAppealOrCYAPage(userRequest, mode)
    }
  }

  protected[navigation] def routingForHospitalStay(mode: Mode, answer: Option[String], userRequest: UserRequest[_]): Call = {
    (mode, answer) match {
      case (CheckMode, Some(ans)) if ans.equalsIgnoreCase("no") && userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueHappened).isDefined =>
        routes.CheckYourAnswersController.onPageLoad()
      case (_, Some(ans)) if ans.equalsIgnoreCase("no") => routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(mode)
      case (_, Some(ans)) if ans.equalsIgnoreCase("yes") => routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(mode)
      case _ =>
        logger.debug("[Navigation][routingForHospitalStay]: unable to get answer - reloading 'WasHospitalStayRequiredPage'")
        routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(mode)
    }
  }

  protected[navigation] def getNextURLBasedOnReasonableExcuse(reasonableExcuse: Option[String], mode: Mode): Call = {
    reasonableExcuse.fold(
      controllers.routes.AppealAgainstObligationController.onPageLoad(mode)
    ) {
      case ReasonableExcuses.bereavement => controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(mode)
      case ReasonableExcuses.crime => controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(mode)
      case ReasonableExcuses.fireOrFlood => controllers.routes.FireOrFloodReasonController.onPageLoad(mode)
      case ReasonableExcuses.lossOfStaff => controllers.routes.LossOfStaffReasonController.onPageLoad(mode)
      case ReasonableExcuses.technicalIssues => controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(mode)
      case ReasonableExcuses.health => controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(mode)
      case ReasonableExcuses.other => controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(mode)
      case _ => throw new MatchError(s"[Navigation][getNextURLBasedOnReasonableExcuse] - Unknown reasonable excuse $reasonableExcuse")
    }
  }

  protected[navigation] def routeToMakingALateAppealOrCYAPage(userRequest: UserRequest[_], mode: Mode): Call = {
    if (isLateAppealHelper.isAppealLate()(userRequest)
      && (userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to CYA page")
      controllers.routes.CheckYourAnswersController.onPageLoad()
    }
  }


  protected[navigation] def routeForUploadList(answer: Option[String], request: UserRequest[_], mode: Mode): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.OtherReasonController.onPageLoadForAnotherFileUpload(mode)
      case Some(ans) if ans.equalsIgnoreCase("no") => routeToMakingALateAppealOrCYAPage(request, mode)
      case None => routeToMakingALateAppealOrCYAPage(request, mode)
      case _ =>
        logger.debug("[Navigation][routeForUploadList]: unable to get answer - reloading 'UploadListPage'")
        routes.OtherReasonController.onPageLoadForUploadComplete(mode)
    }
  }

  protected[navigation] def routeForUploadEvidenceQuestion(isUploadEvidence: Option[String], request: UserRequest[_], mode: Mode, jsEnabled: Option[Boolean]): Call = {
    val isJsEnabled = jsEnabled.getOrElse(false)
    isUploadEvidence match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.OtherReasonController.onPageLoadForUploadEvidence(mode, isJsEnabled)
      case Some(ans) if ans.equalsIgnoreCase("no") => routeToMakingALateAppealOrCYAPage(request, mode)
      case _ =>
        logger.debug("[Navigation][routeForUploadEvidenceQuestion]: unable to get answer - reloading 'UploadEvidenceQuestionPage'")
        routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(mode)
    }
  }

  protected[navigation] def routeForYouCanAppealPenalty(answer: Option[String]): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.AppealStartController.onPageLoad()
      case Some(ans) if ans.equalsIgnoreCase("no") => Call("GET", appConfig.penaltiesFrontendUrl)
      case _ =>
        logger.debug("[Navigation][routeForYouCanAppealPenalty]: unable to get answer - reloading 'YouCanAppealPenaltyPage'")
        routes.YouCanAppealPenaltyController.onPageLoad()
    }
  }

  protected[navigation] def routingForSetUpPaymentPlanPage(answer: Option[String]): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => controllers.findOutHowToAppeal.routes.TimeToPayController.redirect
      case Some(ans) if ans.equalsIgnoreCase("no") => controllers.findOutHowToAppeal.routes.OtherWaysToAppealController.onPageLoad()
      case _ =>
        logger.debug("[Navigation][routingForSetUpPaymentPlanPage]: unable to get answer - reloading 'AppealAfterPaymentPlanSetUpPage'")
        controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad()
    }
  }

  protected[navigation] def routingForYouCanAppealOnlinePage(answer: Option[String]): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => controllers.findOutHowToAppeal.routes.PayNowController.redirect
      case Some(ans) if ans.equalsIgnoreCase("no") => controllers.findOutHowToAppeal.routes.OtherWaysToAppealController.onPageLoad()
      case _ =>
        logger.debug("[Navigation][routingForYouCanAppealOnlinePage]: unable to get answer - reloading 'YouCanAppealOnlinePage'")
        controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad()
    }
  }

  protected[navigation] def routingForCanYouPayPage(answer: Option[String]): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad()
      case Some(ans) if ans.equalsIgnoreCase("no") => controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad()
      case Some(ans) if ans.equalsIgnoreCase("paid") => controllers.findOutHowToAppeal.routes.IfYouvePaidYourVATController.onPageLoad()

      case _ =>
        logger.debug("[Navigation][CanYouPayPage]: unable to get answer - reloading 'CanYouPayPage'")
        controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad()
    }
  }

  protected[navigation] def routingForHasBusinessAskedHMRCToCancelRegistrationPage(optAnswer: Option[String]): Call = {
    optAnswer match {
      case Some(answer) if answer.equalsIgnoreCase("yes") =>
        controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad()
      case Some(answer) if answer.equalsIgnoreCase("no") =>
        controllers.findOutHowToAppeal.routes.ActionsToTakeBeforeAppealingOnlineController.onPageLoad()
      case _ =>
        logger.debug("[Navigation][routingForHasBusinessAskedHMRCToCancelRegistrationPage]: unable to get answer " +
          "- reloading 'HasBusinessAskedHMRCToCancelRegistrationPage'")
        controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad()
    }
  }

  protected[navigation] def routingForHasHMRCConfirmedRegistrationCancellationPage(optAnswer: Option[String]): Call = {
    optAnswer match {
      case Some(answer) if answer.equalsIgnoreCase("yes") =>
        controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter()
      case Some(answer) if answer.equalsIgnoreCase("no") =>
        controllers.findOutHowToAppeal.routes.ActionsToTakeBeforeAppealingOnlineController.onPageLoad()
      case _ =>
        logger.debug("[Navigation][routingForHasBusinessAskedHMRCToCancelRegistrationPage]: unable to get answer " +
          "- reloading 'HasBusinessAskedHMRCToCancelRegistrationPage'")
        controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad()
    }
  }

  protected[navigation] def reverseRouteForAppealStartPage(userRequest: UserRequest[_]): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.YouCanAppealPenaltyController.onPageLoad()
    } else {
      Call("GET", appConfig.penaltiesFrontendUrl)
    }
  }

  protected[navigation] def reverseRouteForHonestyDeclaration(userRequest: UserRequest[_]): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealStartController.onPageLoad()
    } else {
      routes.ReasonableExcuseController.onPageLoad()
    }
  }

  protected[navigation] def reverseRouteForUploadEvidenceQuestion(userRequest: UserRequest[_], mode: Mode): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealAgainstObligationController.onPageLoad(mode)
    } else {
      routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode)
    }
  }

  protected[navigation] def reverseRouteForMakingALateAppealPage(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      reverseRoutingForUpload(userRequest, mode, jsEnabled)
    } else {
      userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get match {
        case "bereavement" => routes.BereavementReasonController.onPageLoadForWhenThePersonDied(mode)
        case "crime" => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(mode)
        case "fireOrFlood" => routes.FireOrFloodReasonController.onPageLoad(mode)
        case "health" => if (userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).contains("yes")) reverseRoutingForHospitalStayEnded(userRequest, mode) else routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(mode)
        case "lossOfStaff" => routes.LossOfStaffReasonController.onPageLoad(mode)
        case "technicalIssues" => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(mode)
        case "other" => reverseRoutingForUpload(userRequest, mode, jsEnabled)
      }
    }
  }

  protected[navigation] def reverseRoutingForHospitalStayEnded(userRequest: UserRequest[_], mode: Mode) = {
    if (userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded).contains("yes")) {
      routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(mode)
    } else {
      routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(mode)
    }
  }

  protected[navigation] def reverseRoutingForUpload(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
    if (userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).contains("yes")) {
      routes.OtherReasonController.onPageLoadForUploadEvidence(mode, jsEnabled)
    } else {
      routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(mode)
    }
  }

  protected[navigation] def reverseRouteForReasonableExcuseSelectionPage(userRequest: UserRequest[_], mode: Mode): Call = {
    (userRequest.isAgent, userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission),
      userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).isDefined) match {
      case (true, true, _) if userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).contains("agent") => {
        routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(mode)
      }
      case (true, true, _) => routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(mode)
      case (_, _, true) if userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes") => {
        routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(mode)
      }
      case (_, _, true) => routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(mode)
      case _ => routes.AppealStartController.onPageLoad()
    }
  }

  protected[navigation] def reverseRouteForCYAPage(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
    val dateSentParsed: LocalDate = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).get
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateNow: LocalDate = dateTimeHelper.dateNow
    if (dateSentParsed.isBefore(dateNow.minusDays(daysResultingInLateAppeal))
      && (userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - " +
        s"Date now: $dateNow :: Date communication sent: $dateSentParsed - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      reverseRouteForMakingALateAppealPage(userRequest, mode, jsEnabled)
    }
  }

  protected[navigation] def reverseRouteForOtherWaysToAppeal(userRequest: UserRequest[_]): Call = {
    if (userRequest.answers.getAnswer[String](SessionKeys.willUserPay).contains("yes")) {
      controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad()
    } else {
      controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad()
    }
  }

  protected[navigation] def reverseRouteForAppealByLetterPage(): Call = {
    if(isEnabled(ShowFindOutHowToAppealLSPJourney)) {
      controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad()
    } else {
      routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()
    }
  }

  protected[navigation] def reverseRouteForActionsToTakeBeforeAppealingOnlinePage(userRequest: UserRequest[_]): Call = {
    val isCALPP = userRequest.answers.getAnswer[Boolean](SessionKeys.isCaLpp).getOrElse(false)
    (userRequest.answers.getAnswer[String](SessionKeys.hasBusinessAskedHMRCToCancelRegistration),
      userRequest.answers.getAnswer[String](SessionKeys.hasHMRCConfirmedRegistrationCancellation),
      isCALPP) match {
      case (_, Some(_), false) => controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad()
      case (Some(_), _, false) => controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad()
      case (_, _, true) => Call("GET", appConfig.penaltiesFrontendUrl)
      case _ => {
        logger.debug("[Navigation][reverseRouteForActionsToTakeBeforeAppealingOnlinePage]: unable to get answer " +
          "- reloading 'ActionsToTakeBeforeAppealingOnlinePage'")
        controllers.findOutHowToAppeal.routes.ActionsToTakeBeforeAppealingOnlineController.onPageLoad()
      }
    }
  }
}
