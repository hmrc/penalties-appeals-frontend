/*
 * Copyright 2022 HM Revenue & Customs
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

  lazy val reverseNormalRoutes: Map[Page, UserRequest[_] => Call] = Map(
    CancelVATRegistrationPage -> (_ => Call("GET", appConfig.penaltiesFrontendUrl)),
    YouCannotAppealPage -> (_ => routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()),
    YouCanAppealThisPenaltyPage -> (_ => routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()),
    AppealStartPage -> (request => reverseRouteForAppealStartPage(request)),
    HonestyDeclarationPage -> (request => reverseRouteForHonestyDeclaration(request)),
    OtherRelevantInformationPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    UploadEvidenceQuestionPage -> (request => reverseRouteForUploadEvidenceQuestion(request, NormalMode)),
    FileListPage -> (_ => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    UploadFirstDocumentPage -> (_ => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    UploadAnotherDocumentPage -> (_ => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    EvidencePage -> (_ => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    WhenDidThePersonDiePage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    HasCrimeBeenReportedPage -> (_ => routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode)),
    WhenDidCrimeHappenPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidFireOrFloodHappenPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WasHospitalStayRequiredPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidHealthIssueHappenPage -> (_ => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode)),
    WhenDidHospitalStayBeginPage -> (_ => routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode)),
    DidHospitalStayEndPage -> (_ => routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode)),
    WhenDidPersonLeaveTheBusinessPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidTechnologyIssuesBeginPage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidTechnologyIssuesEndPage -> (_ => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(NormalMode)),
    WhenDidBecomeUnablePage -> (_ => routes.HonestyDeclarationController.onPageLoad()),
    WhyWasReturnSubmittedLatePage -> (_ => routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(NormalMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> (_ => routes.AppealStartController.onPageLoad()),
    WhatCausedYouToMissTheDeadlinePage -> (_ => routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode)),
    ReasonableExcuseSelectionPage -> (request => reverseRouteForReasonableExcuseSelectionPage(request, NormalMode)),
    MakingALateAppealPage -> (request => reverseRouteForMakingALateAppealPage(request, NormalMode)),
    CheckYourAnswersPage -> (request => reverseRouteForCYAPage(request, NormalMode)),
    PenaltySelectionPage -> (_ => routes.AppealStartController.onPageLoad()),
    AppealSinglePenaltyPage -> (_ => routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode)),
    AppealCoverBothPenaltiesPage -> (_ => routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode))
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

  lazy val checkingRoutes: Map[Page, (Option[String], UserRequest[_]) => Call] = Map(
    HasCrimeBeenReportedPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidCrimeHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidFireOrFloodHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidTechnologyIssuesBeginPage -> ((_, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode)),
    WhenDidTechnologyIssuesEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WasHospitalStayRequiredPage -> ((answer, request) => routingForHospitalStay(CheckMode, answer, request)),
    WhenDidHealthIssueHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidHospitalStayBeginPage -> ((_, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode)),
    DidHospitalStayEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidBecomeUnablePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhyWasReturnSubmittedLatePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    EvidencePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, CheckMode)),
    WhatCausedYouToMissTheDeadlinePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    ReasonableExcuseSelectionPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidThePersonDiePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    OtherRelevantInformationPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    UploadFirstDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)),
    UploadAnotherDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode)),
    FileListPage -> ((answer, request) => routeForUploadList(answer, request, CheckMode)),
    UploadEvidenceQuestionPage -> ((answer, request) => routeForUploadEvidenceQuestion(answer, request, CheckMode)),
    AppealSinglePenaltyPage -> ((_, _) => routes.CheckYourAnswersController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, _) => routes.CheckYourAnswersController.onPageLoad()),
    PenaltySelectionPage -> ((answer, _) => routingForPenaltySelectionPage(answer, CheckMode))
  )

  lazy val normalRoutes: Map[Page, (Option[String], UserRequest[_]) => Call] = Map(
    HonestyDeclarationPage -> ((answer, _) => getNextURLBasedOnReasonableExcuse(answer, NormalMode)),
    HasCrimeBeenReportedPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidCrimeHappenPage -> ((_, _) => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode)),
    WhenDidFireOrFloodHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidTechnologyIssuesBeginPage -> ((_, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)),
    WhenDidTechnologyIssuesEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WasHospitalStayRequiredPage -> ((answer, request) => routingForHospitalStay(NormalMode, answer, request)),
    WhenDidHealthIssueHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidHospitalStayBeginPage -> ((_, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode)),
    DidHospitalStayEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidBecomeUnablePage -> ((_, _) => routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode)),
    WhyWasReturnSubmittedLatePage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    EvidencePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, NormalMode)),
    WhatCausedYouToMissTheDeadlinePage -> ((_, _) => routes.ReasonableExcuseController.onPageLoad()),
    ReasonableExcuseSelectionPage -> ((_, _) => routes.HonestyDeclarationController.onPageLoad()),
    WhenDidThePersonDiePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    AppealStartPage -> ((_, _) => routes.AppealStartController.onPageLoad()),
    OtherRelevantInformationPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode)),
    CancelVATRegistrationPage -> ((answer, _) => routingForCancelVATRegistrationPage(answer)),
    UploadFirstDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    UploadAnotherDocumentPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode)),
    FileListPage -> ((answer, request) => routeForUploadList(answer, request, NormalMode)),
    UploadEvidenceQuestionPage -> ((answer, request) => routeForUploadEvidenceQuestion(answer, request, NormalMode)),
    YouCanAppealThisPenaltyPage -> ((answer, _) => routeForYouCanAppealPenalty(answer)),
    AppealSinglePenaltyPage -> ((_, _) => routes.ReasonableExcuseController.onPageLoad()),
    AppealCoverBothPenaltiesPage -> ((_, _) => routes.ReasonableExcuseController.onPageLoad()),
    PenaltySelectionPage -> ((answer, _) => routingForPenaltySelectionPage(answer, NormalMode))
  )

  def nextPage(page: Page, mode: Mode, answer: Option[String] = None)
              (implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode =>
        //Added answer here so that we can add custom routing to pages that require extra data when answers change
        checkingRoutes(page)(answer, userRequest)
      case NormalMode =>
        normalRoutes(page)(answer, userRequest)
    }
  }

  def previousPage(page: Page, mode: Mode)(implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode =>
        reverseCheckingRoutes(page, userRequest)
      case NormalMode if userRequest.session.get(SessionKeys.originatingChangePage).contains(page.toString) =>
        reverseCheckingRoutes(page, userRequest)
      case NormalMode =>
        reverseNormalRoutes(page)(userRequest)
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

  def routingForCancelVATRegistrationPage(answer: Option[String]): Call = {
    if (answer.get.toLowerCase == "yes") {
      routes.YouCanAppealPenaltyController.onPageLoad()
    } else {
      routes.YouCannotAppealController.onPageLoad
    }
  }

  def routingForPenaltySelectionPage(answer: Option[String], mode: Mode): Call = {
    if(answer.get.toLowerCase == "yes") {
      routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(mode)
    } else {
      routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(mode)
    }
  }

  def routingForHospitalStay(mode: Mode, answer: Option[String], request: UserRequest[_]): Call = {
    (mode, answer) match {
      case (CheckMode, Some(ans)) if ans.equalsIgnoreCase("no") && request.session.get(SessionKeys.whenHealthIssueHappened).isDefined =>
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
      && (userRequest.session.get(SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && userRequest.session.get(SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - redirect to CYA page")
      controllers.routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
      val dateNow: LocalDate = dateTimeHelper.dateNow
      val dateSentParsed: LocalDate = LocalDate.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
      dateSentParsed.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal))
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

  def routeForUploadEvidenceQuestion(isUploadEvidence: Option[String], request: UserRequest[_], mode: Mode): Call = {
    isUploadEvidence match {
      case Some(ans) if ans.equalsIgnoreCase("yes") => routes.OtherReasonController.onPageLoadForUploadEvidence(mode)
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

  private def reverseRouteForAppealStartPage(request: UserRequest[_]): Call = {
    if(request.session.get(SessionKeys.isObligationAppeal).isDefined) {
      routes.YouCanAppealPenaltyController.onPageLoad()
    } else {
      Call("GET", appConfig.penaltiesFrontendUrl)
    }
  }

  private def reverseRouteForHonestyDeclaration(request: UserRequest[_]): Call = {
    if(request.session.get(SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealStartController.onPageLoad()
    } else {
      routes.ReasonableExcuseController.onPageLoad()
    }
  }

  private def reverseRouteForUploadEvidenceQuestion(request: UserRequest[_], mode: Mode): Call = {
    if(request.session.get(SessionKeys.isObligationAppeal).isDefined) {
      routes.AppealAgainstObligationController.onPageLoad(mode)
    } else {
      routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode)
    }
  }

  private def reverseRouteForMakingALateAppealPage(request: UserRequest[_], mode: Mode): Call = {
    if (request.session.get(SessionKeys.isObligationAppeal).isDefined) {
      reverseRoutingForUpload(request, mode)
    } else {
      request.session.get(SessionKeys.reasonableExcuse).get match {
        case "bereavement" => routes.BereavementReasonController.onPageLoadForWhenThePersonDied(mode)
        case "crime" => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(mode)
        case "fireOrFlood" => routes.FireOrFloodReasonController.onPageLoad(mode)
        case "health" => if (request.session.get(SessionKeys.wasHospitalStayRequired).contains("yes")) routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(mode) else routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(mode)
        case "lossOfStaff" => routes.LossOfStaffReasonController.onPageLoad(mode)
        case "technicalIssues" => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(mode)
        case "other" => reverseRoutingForUpload(request, mode)
      }
    }
  }

  private def reverseRoutingForUpload(request: UserRequest[_], mode: Mode): Call = {
    if(request.session.get(SessionKeys.isUploadEvidence).contains("yes")) {
      routes.OtherReasonController.onPageLoadForUploadEvidence(mode)
    } else {
      routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(mode)
    }
  }

  private def reverseRouteForReasonableExcuseSelectionPage(request: UserRequest[_], mode: Mode): Call = {
    if(request.isAgent && request.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString)) {
      if(request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).contains("agent")) {
        routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(mode)
      } else {
        routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(mode)
      }
    } else {
      routes.AppealStartController.onPageLoad()
    }
  }

  private def reverseRouteForCYAPage(request: UserRequest[_], mode: Mode): Call = {
    val dateSentParsed: LocalDate = LocalDate.parse(request.session.get(SessionKeys.dateCommunicationSent).get)
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateNow: LocalDate = dateTimeHelper.dateNow
    if (dateSentParsed.isBefore(dateNow.minusDays(daysResultingInLateAppeal))
      && (request.session.get(SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && request.session.get(SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - " +
        s"Date now: $dateNow :: Date communication sent: $dateSentParsed - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      reverseRouteForMakingALateAppealPage(request, mode)
    }
  }
}
