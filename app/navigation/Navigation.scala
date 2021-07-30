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

package navigation

import config.AppConfig
import models.{CheckMode, Mode, NormalMode, UserRequest}
import models.pages._
import play.api.mvc.{Call, Request}
import controllers.routes
import helpers.DateTimeHelper
import utils.Logger.logger
import utils.{ReasonableExcuses, SessionKeys}
import java.time.LocalDateTime

import javax.inject.Inject

class Navigation @Inject()(dateTimeHelper: DateTimeHelper,
                           appConfig: AppConfig) {
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
    WhyWasTheReturnSubmittedLateAgentPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    ReasonableExcuseSelectionPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidThePersonDiePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    OtherRelevantInformationPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode))
  )

  lazy val normalRoutes: Map[Page, (Option[String], UserRequest[_]) => Call] = Map(
    HonestyDeclarationPage -> ((answer, request) => getNextURLBasedOnReasonableExcuse(answer, NormalMode)(request)),
    HasCrimeBeenReportedPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidCrimeHappenPage -> ((_, _) => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode)),
    WhenDidFireOrFloodHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidPersonLeaveTheBusinessPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidTechnologyIssuesBeginPage -> ((_, _) => routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode)),
    WhenDidTechnologyIssuesEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WasHospitalStayRequiredPage -> ((answer, request) => routingForHospitalStay(NormalMode, answer, request)),
    WhenDidHealthIssueHappenPage-> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidHospitalStayBeginPage-> ((_, _) => routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode)),
    DidHospitalStayEndPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidBecomeUnablePage -> ((_, _) => routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode)),
    WhyWasReturnSubmittedLatePage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode)),
    EvidencePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhoPlannedToSubmitVATReturnAgentPage -> ((answer, request) => routingForWhoPlannedToSubmitVATReturnAgentPage(answer, request, NormalMode)),
    WhyWasTheReturnSubmittedLateAgentPage -> ((_, _) => routes.ReasonableExcuseController.onPageLoad()),
    ReasonableExcuseSelectionPage -> ((_, _) => routes.ReasonableExcuseController.onPageLoad()),
    WhenDidThePersonDiePage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    AppealStartPage -> ((_, _) => routes.AppealStartController.onPageLoad()),
    OtherRelevantInformationPage -> ((_, _) => routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode)),
    CancelVATRegistrationPage -> ((answer, _) => routingForCancelVATRegistrationPage(answer))
  )

  def nextPage(page: Page, mode: Mode, answer: Option[String] = None)(implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode => {
        //Added answer here so that we can add custom routing to pages that require extra data when answers change
        checkingRoutes(page)(answer, userRequest)
      }
      case NormalMode => {
        normalRoutes(page)(answer, userRequest)
      }
    }
  }

  def routingForWhoPlannedToSubmitVATReturnAgentPage(answer: Option[String], request: UserRequest[_], mode: Mode): Call = {
    if(answer.get.toLowerCase == "agent") {
      routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(mode)
    } else if(mode == NormalMode) {
      routes.ReasonableExcuseController.onPageLoad()
    } else {
      routeToMakingALateAppealOrCYAPage(request, mode)
    }
  }

  def routingForCancelVATRegistrationPage(answer: Option[String]): Call = {
    if(answer.get.toLowerCase == "yes") {
      routes.OtherPenaltiesForPeriodController.onPageLoad()
    } else {
      //TODO: add route when option is 'no' for now it remains on same page
      routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration()
    }
  }

  def routingForHospitalStay(mode: Mode, answer: Option[String], request: UserRequest[_]): Call = {
    (mode, answer) match {
      case (CheckMode, Some(ans)) if ans.equalsIgnoreCase("no") && request.session.get(SessionKeys.whenHealthIssueHappened).isDefined => {
        routes.CheckYourAnswersController.onPageLoad()
      }
      case (_, Some(ans)) if ans.equalsIgnoreCase("no") => routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(mode)
      case (_, Some(ans)) if ans.equalsIgnoreCase("yes")=> routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(mode)
      case _ => {
        logger.debug("[Navigation][routingForHospitalStay]: unable to get answer - reloading 'WasHospitalStayRequiredPage'")
        routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(mode)
      }
    }
  }

  def getNextURLBasedOnReasonableExcuse(reasonableExcuse: Option[String], mode: Mode)(implicit request: Request[_]): Call = {
    reasonableExcuse.fold(
      //Route to Other Relevant Information page
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
    val dateSentParsed: LocalDateTime = LocalDateTime.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateTimeNow: LocalDateTime = dateTimeHelper.dateTimeNow
    if (dateSentParsed.isBefore(dateTimeNow.minusDays(daysResultingInLateAppeal))
      && (userRequest.session.get(SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)
      && userRequest.session.get(SessionKeys.isObligationAppeal).isEmpty) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - " +
        s"Date now: $dateTimeNow :: Date communication sent: $dateSentParsed - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - Date now: $dateTimeNow :: Date communication sent: $dateSentParsed - redirect to CYA page")
      controllers.routes.CheckYourAnswersController.onPageLoad()
    }
  }
}
