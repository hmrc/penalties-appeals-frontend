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
import config.featureSwitches.FeatureSwitching
import controllers.routes
import helpers.DateTimeHelper
import models._
import models.pages._
import play.api.Configuration
import play.api.mvc.Call
import utils.Logger.logger
import utils.{ReasonableExcuses, SessionKeys}

import java.time.LocalDate
import javax.inject.Inject

class Navigation @Inject()(dateTimeHelper: DateTimeHelper,
                           appConfig: AppConfig)(implicit val config: Configuration) extends FeatureSwitching {

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
    AppealByLetterKickOutPage -> ((_, _) => routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration())
  )

  def reverseCheckingRoutes(page: Page, userRequest: UserRequest[_]): Call = {
    page match {
      case page if userRequest.session.get(SessionKeys.originatingChangePage).contains(page.toString) => routes.CheckYourAnswersController.onPageLoad()
      case DidHospitalStayEndPage => routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode)
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
    DidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidBecomeUnablePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhyWasReturnSubmittedLatePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    EvidencePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request, _) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, CheckMode)),
    WhatCausedYouToMissTheDeadlinePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    ReasonableExcuseSelectionPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidThePersonDiePage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    OtherRelevantInformationPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    UploadFirstDocumentPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)),
    UploadAnotherDocumentPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)),
    FileListPage -> ((answer, request, _) => routeForUploadList(answer, request, CheckMode)),
    UploadEvidenceQuestionPage -> ((answer, request, optJsEnabled) => routeForUploadEvidenceQuestion(answer, request, CheckMode, optJsEnabled)),
    AppealSinglePenaltyPage -> ((_, _, _) => routes.CheckYourAnswersController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    PenaltySelectionPage -> ((answer, _, _) => routingForPenaltySelectionPage(answer, CheckMode))
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
    DidHospitalStayEndPage -> ((_, request, _) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
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
    UploadAnotherDocumentPage -> ((_, _, _) => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    FileListPage -> ((answer, request, _) => routeForUploadList(answer, request, NormalMode)),
    UploadEvidenceQuestionPage -> ((answer, request, optJsEnabled) => routeForUploadEvidenceQuestion(answer, request, NormalMode, optJsEnabled)),
    YouCanAppealThisPenaltyPage -> ((answer, _, _) => routeForYouCanAppealPenalty(answer)),
    AppealSinglePenaltyPage -> ((_, _, _) => routes.ReasonableExcuseController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, _, _) => routes.ReasonableExcuseController.onPageLoad()),
    PenaltySelectionPage -> ((answer, _, _) => routingForPenaltySelectionPage(answer, NormalMode))
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

  def routingForWhoPlannedToSubmitVATReturnAgentPage(answer: Option[String], request: UserRequest[_], mode: Mode): Call = {
    if (answer.get.toLowerCase == "agent") {
      routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(mode)
    } else if (mode == NormalMode) {
      routes.ReasonableExcuseController.onPageLoad()
    } else {
      routeToMakingALateAppealOrCYAPage(request, mode)
    }
  }

  def routingForCancelVATRegistrationPage(answer: Option[String], request: UserRequest[_]): Call = {
    if (answer.get.toLowerCase == "yes" && request.answers.getAnswer[String](SessionKeys.penaltyNumber).contains("NA")) {
      routes.YouCannotAppealController.onPageLoadAppealByLetter()
    } else if(answer.get.toLowerCase == "yes") {
      routes.YouCanAppealPenaltyController.onPageLoad()
    } else {
      routes.YouCannotAppealController.onPageLoad
    }
  }

  def routingForPenaltySelectionPage(answer: Option[String], mode: Mode): Call = {
    if (answer.get.toLowerCase == "yes") {
      routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(mode)
    } else {
      routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(mode)
    }
  }

  def routingForHospitalStay(mode: Mode, answer: Option[String], userRequest: UserRequest[_]): Call = {
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

  def getNextURLBasedOnReasonableExcuse(reasonableExcuse: Option[String], mode: Mode): Call = {
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
    }
  }

  def routeToMakingALateAppealOrCYAPage(userRequest: UserRequest[_], mode: Mode): Call = {
    if (isAppealLate()(userRequest)
      && (userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to CYA page")
      controllers.routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
    val dateNow: LocalDate = dateTimeHelper.dateNow
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties) match {
      case Some("yes") => {
        val dateOfFirstComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate).get
        val dateOfSecondComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate).get
        dateOfFirstComms.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal)) ||
          dateOfSecondComms.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal))
      }
      case _ => {
        val dateOfComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).get
        dateOfComms.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal))
      }
    }
  }

  def routeForUploadList(answer: Option[String], request: UserRequest[_], mode: Mode): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.OtherReasonController.onPageLoadForAnotherFileUpload(mode)
      case Some(ans) if ans.equalsIgnoreCase("no") => routeToMakingALateAppealOrCYAPage(request, mode)
      case None => routeToMakingALateAppealOrCYAPage(request, mode)
      case _ =>
        logger.debug("[Navigation][routeForUploadList]: unable to get answer - reloading 'UploadListPage'")
        routes.OtherReasonController.onPageLoadForUploadComplete(mode)
    }
  }

  def routeForUploadEvidenceQuestion(isUploadEvidence: Option[String], request: UserRequest[_], mode: Mode, jsEnabled: Option[Boolean]): Call = {
    val isJsEnabled = jsEnabled.getOrElse(false)
    isUploadEvidence match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.OtherReasonController.onPageLoadForUploadEvidence(mode, isJsEnabled)
      case Some(ans) if ans.equalsIgnoreCase("no") => routeToMakingALateAppealOrCYAPage(request, mode)
      case _ =>
        logger.debug("[Navigation][routeForUploadEvidenceQuestion]: unable to get answer - reloading 'UploadEvidenceQuestionPage'")
        routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(mode)
    }
  }

  def routeForYouCanAppealPenalty(answer: Option[String]): Call = {
    answer match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.AppealStartController.onPageLoad()
      case Some(ans) if ans.equalsIgnoreCase("no") => Call("GET", appConfig.penaltiesFrontendUrl)
      case _ =>
        logger.debug("[Navigation][routeForYouCanAppealPenalty]: unable to get answer - reloading 'YouCanAppealPenaltyPage'")
        routes.YouCanAppealPenaltyController.onPageLoad()
    }
  }

  private def reverseRouteForAppealStartPage(userRequest: UserRequest[_]): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.YouCanAppealPenaltyController.onPageLoad()
    } else {
      Call("GET", appConfig.penaltiesFrontendUrl)
    }
  }

  private def reverseRouteForHonestyDeclaration(userRequest: UserRequest[_]): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealStartController.onPageLoad()
    } else {
      routes.ReasonableExcuseController.onPageLoad()
    }
  }

  private def reverseRouteForUploadEvidenceQuestion(userRequest: UserRequest[_], mode: Mode): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealAgainstObligationController.onPageLoad(mode)
    } else {
      routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode)
    }
  }

  private def reverseRouteForMakingALateAppealPage(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
    if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) {
      reverseRoutingForUpload(userRequest, mode, jsEnabled)
    } else {
      userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get match {
        case "bereavement" => routes.BereavementReasonController.onPageLoadForWhenThePersonDied(mode)
        case "crime" => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(mode)
        case "fireOrFlood" => routes.FireOrFloodReasonController.onPageLoad(mode)
        case "health" => if (userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).contains("yes")) routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(mode) else routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(mode)
        case "lossOfStaff" => routes.LossOfStaffReasonController.onPageLoad(mode)
        case "technicalIssues" => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(mode)
        case "other" => reverseRoutingForUpload(userRequest, mode, jsEnabled)
      }
    }
  }

  private def reverseRoutingForUpload(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
    if (userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).contains("yes")) {
      routes.OtherReasonController.onPageLoadForUploadEvidence(mode, jsEnabled)
    } else {
      routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(mode)
    }
  }

  private def reverseRouteForReasonableExcuseSelectionPage(userRequest: UserRequest[_], mode: Mode): Call = {
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

  private def reverseRouteForCYAPage(userRequest: UserRequest[_], mode: Mode, jsEnabled: Boolean): Call = {
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
}
