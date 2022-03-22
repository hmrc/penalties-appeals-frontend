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

package helpers

import models.pages._
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import play.api.mvc.Request
import repositories.UploadJourneyRepository
import utils.MessageRenderer.getMessage
import utils.SessionKeys
import viewtils.ImplicitDateFormatter

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionAnswersHelper @Inject()(uploadJourneyRepository: UploadJourneyRepository)(implicit ec: ExecutionContext) extends ImplicitDateFormatter {
  val answersRequiredForReasonableExcuseJourney: Map[String, Seq[String]] = Map(
    "bereavement" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whenDidThePersonDie, SessionKeys.hasConfirmedDeclaration),
    "crime" -> Seq(SessionKeys.hasCrimeBeenReportedToPolice, SessionKeys.reasonableExcuse, SessionKeys.dateOfCrime, SessionKeys.hasConfirmedDeclaration),
    "lossOfStaff" -> Seq(SessionKeys.whenPersonLeftTheBusiness, SessionKeys.reasonableExcuse, SessionKeys.hasConfirmedDeclaration),
    "fireOrFlood" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.dateOfFireOrFlood, SessionKeys.hasConfirmedDeclaration),
    "technicalIssues" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whenDidTechnologyIssuesBegin, SessionKeys.whenDidTechnologyIssuesEnd),
    "healthIssueHospitalStayOngoing" ->
      Seq(SessionKeys.reasonableExcuse, SessionKeys.whenHealthIssueStarted, SessionKeys.hasHealthEventEnded, SessionKeys.wasHospitalStayRequired),
    "healthIssueHospitalStayEnded" ->
      Seq(SessionKeys.reasonableExcuse, SessionKeys.whenHealthIssueStarted, SessionKeys.whenHealthIssueEnded,
        SessionKeys.hasHealthEventEnded, SessionKeys.wasHospitalStayRequired),
    "healthIssueNoHospitalStay" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.wasHospitalStayRequired, SessionKeys.whenHealthIssueHappened),
    "other" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whyReturnSubmittedLate, SessionKeys.whenDidBecomeUnable, SessionKeys.isUploadEvidence)
  )

  def isAllAnswerPresentForReasonableExcuse(reasonableExcuse: String)(implicit request: Request[_]): Boolean = {
    val keysInSession = request.session.data.keys.toSet
    reasonableExcuse match {
      case "health" =>
        (request.session.get(SessionKeys.wasHospitalStayRequired), request.session.get(SessionKeys.hasHealthEventEnded)) match {
          //No hospital stay
          case (Some("no"), _) =>
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueNoHospitalStay").toSet
            answersRequired.subsetOf(keysInSession)
          //Hospital stay ongoing
          case (Some("yes"), Some("no")) =>
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueHospitalStayOngoing").toSet
            answersRequired.subsetOf(keysInSession)

          //Hospital stay ended
          case (Some("yes"), Some("yes")) =>
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueHospitalStayEnded").toSet
            answersRequired.subsetOf(keysInSession)

          //Wrong configuration of health answers
          case _ => false
        }
      case _ =>
        val answersRequired = answersRequiredForReasonableExcuseJourney(reasonableExcuse).toSet
        answersRequired.subsetOf(keysInSession)
    }

  }

  //scalastyle:off
  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String, fileNames: Option[String] = None)(implicit request: UserRequest[_], messages: Messages): Seq[(String, String, String)] = {
    val reasonableExcuseContent = reasonableExcuse match {
      case "bereavement" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        (messages("checkYourAnswers.bereavement.whenDidThePersonDie"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidThePersonDie).get)),
          changeAnswerUrl(
            controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
            WhenDidThePersonDiePage
          )
        )
      )

      case "crime" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        (messages("checkYourAnswers.crime.whenDidTheCrimeHappen"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfCrime).get)),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
            WhenDidCrimeHappenPage
          )
        ),
        (messages("checkYourAnswers.crime.hasCrimeBeenReported"),
          messages(s"checkYourAnswers.crime.${request.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get}"),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
            HasCrimeBeenReportedPage
          )
        )
      )

      case "fireOrFlood" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        (messages("checkYourAnswers.fireOrFlood.whenDidTheFireOrFloodHappen"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfFireOrFlood).get)),
          changeAnswerUrl(
            controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
            WhenDidFireOrFloodHappenPage
          )
        )
      )

      case "lossOfStaff" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        (messages("checkYourAnswers.lossOfStaff.whenPersonBecameUnavailable"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenPersonLeftTheBusiness).get)),
          changeAnswerUrl(
            controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
            WhenDidPersonLeaveTheBusinessPage
          )
        )
      )

      case "technicalIssues" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        (messages("checkYourAnswers.technicalIssues.whenDidTechIssuesBegin"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidTechnologyIssuesBegin).get)),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
            WhenDidTechnologyIssuesBeginPage
          )
        ),
        (messages("checkYourAnswers.technicalIssues.whenDidTechIssuesEnd"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidTechnologyIssuesEnd).get)),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
            WhenDidTechnologyIssuesEndPage
          )
        )
      )

      case "health" => getHealthReasonAnswers

      case "other" =>
        val statementOfLatenessForLPPOrLSP: String = {
          if (request.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment.toString) || request.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Additional.toString)) {
            messages("checkYourAnswers.other.lpp.statementOfLateness")
          } else {
            messages("checkYourAnswers.other.statementOfLateness")
          }
        }
        val base = Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          (messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("whenDidBecomeUnable.other")),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidBecomeUnable).get)),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage
            )
          ),
          (statementOfLatenessForLPPOrLSP,
            request.session.get(SessionKeys.whyReturnSubmittedLate).get,
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
              WhyWasReturnSubmittedLatePage
            )
           ),
          (messages("checkYourAnswers.other.uploadEvidenceQuestion"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.isUploadEvidence).get}"),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
              UploadEvidenceQuestionPage
            )
          )
        )

        if (request.session.get(SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
          base :+ (
            messages("checkYourAnswers.other.fileEvidence"),
            if (fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          )
        } else {
          base
        }
    }

    request.session.get(SessionKeys.lateAppealReason).fold(
      reasonableExcuseContent
    )(
      reason => {
        reasonableExcuseContent :+ (
          (messages("checkYourAnswers.whyYouDidNotAppealSooner"),
            reason,
            changeAnswerUrl(
              controllers.routes.MakingALateAppealController.onPageLoad().url,
              MakingALateAppealPage
            )
          )
        )
      }
    )
  }

  def getHealthReasonAnswers()(implicit request: UserRequest[_], messages: Messages): Seq[(String, String, String)] = {
    (request.session.get(SessionKeys.wasHospitalStayRequired), request.session.get(SessionKeys.hasHealthEventEnded)) match {
      //No hospital stay
      case (Some("no"), _) =>
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          (messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueHappened).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage
            )
          )
        )
      //Hospital stay ended
      case (Some("yes"), Some("yes")) =>
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          (messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )
          ),
          (messages("checkYourAnswers.health.hasTheHospitalStayEnded"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            )
          ),
          (messages("checkYourAnswers.health.whenDidHospitalStayEnd"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueEnded).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            )
          )
        )

      //Hospital stay ongoing
      case (Some("yes"), Some("no")) =>
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )),
          (messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )),
          (messages("checkYourAnswers.health.hasTheHospitalStayEnded"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            ))
        )
    }
  }

  def getContentForAgentsCheckYourAnswersPage()(implicit request: Request[_], messages: Messages): Seq[(String, String, String)] = {

    val seqWhoPlannedToSubmitVATReturn = Seq(
      (messages("checkYourAnswers.agents.whoPlannedToSubmitVATReturn"),
        messages(s"checkYourAnswers.agents.whoPlannedToSubmitVATReturn.${request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get}"),
        changeAnswerUrl(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage
        )
      )
    )

    val seqWhatCausedAgentToMissDeadline = if (request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get.equals("agent")) {
      Seq((messages("checkYourAnswers.agents.whatCausedYouToMissTheDeadline"),
        messages(s"checkYourAnswers.agents.whatCausedYouToMissTheDeadline.${request.session.get(SessionKeys.whatCausedYouToMissTheDeadline).get}"),
        changeAnswerUrl(
          controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
          WhatCausedYouToMissTheDeadlinePage
        )
      ))
    }
    else Seq.empty

    seqWhoPlannedToSubmitVATReturn ++ seqWhatCausedAgentToMissDeadline
  }

  def getAllTheContentForCheckYourAnswersPage(uploadFilenames: Option[String] = None)(implicit request: UserRequest[_], messages: Messages): Seq[(String, String, String)] = {

    val reasonableExcuse = request.session.get(SessionKeys.reasonableExcuse)
    val agentSession = request.session.get(SessionKeys.agentSessionVrn).isDefined
    val appealType = request.session.get(SessionKeys.appealType)

    (request.session.get(SessionKeys.isObligationAppeal), reasonableExcuse.isDefined, agentSession) match {
      case (Some(_), _, _) => getContentForObligationAppealCheckYourAnswersPage(uploadFilenames)
      case (_, true, false) if isAllAnswerPresentForReasonableExcuse(reasonableExcuse.get) => getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case (_, true, true) if appealType.contains(PenaltyTypeEnum.Late_Payment.toString) || appealType.contains(PenaltyTypeEnum.Additional.toString) =>
        getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case (_, true, true) => getContentForAgentsCheckYourAnswersPage() ++ getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case _ => Seq.empty
    }
  }

  def getContentForObligationAppealCheckYourAnswersPage(fileNames: Option[String] = None)(implicit request: Request[_], messages: Messages): Seq[(String, String, String)] = {
    val base = Seq(
      (messages("checkYourAnswers.obligation.whyYouWantToAppealPenalty"),
        request.session.get(SessionKeys.otherRelevantInformation).get,
        changeAnswerUrl(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage
        )
      ),
      (messages("checkYourAnswers.other.uploadEvidenceQuestion"),
        messages(s"common.radioOption.${request.session.get(SessionKeys.isUploadEvidence).get}"),
        changeAnswerUrl(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage
        )
      )
    )
    if (request.session.get(SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
      base :+ (
        messages("checkYourAnswers.obligation.fileEvidence"),
        if (fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
        controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      )
    } else {
      base
    }
  }

  def getPreviousUploadsFileNames()(implicit request: UserRequest[_]): Future[String] = {
    for {
      previousUploads <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
    } yield {
      val previousUploadsFileName = previousUploads.map(_.map(file => file.uploadDetails.map(details => details.fileName)))
      previousUploadsFileName.getOrElse(Seq.empty).collect {
        case Some(x) => x
      }.mkString(", ")
    }
  }

  def getContentWithExistingUploadFileNames(reasonableExcuse: String)(implicit request: UserRequest[_], messages: Messages): Future[Seq[(String, String, String)]] = {
    if (!reasonableExcuse.equals("other") && !request.session.get(SessionKeys.isObligationAppeal).contains("true")) {
      Future(getAllTheContentForCheckYourAnswersPage()(request, messages))
    }
    else {
      for {
        fileNames <- getPreviousUploadsFileNames()(request)
      } yield {
        getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))(request, messages)
      }
    }
  }

  def changeAnswerUrl(continueUrl: String, page: Page): String = {
    controllers.routes.CheckYourAnswersController.changeAnswer(continueUrl, page.toString).url
  }
}
