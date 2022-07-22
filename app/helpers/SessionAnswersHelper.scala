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

import models.appeals.CheckYourAnswersRow

import java.time.LocalDate
import javax.inject.Inject
import models.pages._
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import play.api.mvc.Request
import repositories.UploadJourneyRepository
import utils.SessionKeys
import viewtils.ImplicitDateFormatter

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

  private def getMultiplePenaltiesForThisPeriodRows()(implicit request: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val answer = request.session.get(SessionKeys.doYouWantToAppealBothPenalties).map(answer => messages(s"common.radioOption.$answer")).get
    Seq(
      CheckYourAnswersRow(
        messages("penaltySelection.form.heading"),
        answer,
        changeAnswerUrl(
          controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url,
          PenaltySelectionPage
        )
      )
    )
  }

  //scalastyle:off
  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String, fileNames: Option[String] = None, isLPP: Boolean = false)(implicit request: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val multiplePenaltiesContent = if (request.session.get(SessionKeys.doYouWantToAppealBothPenalties).isDefined) getMultiplePenaltiesForThisPeriodRows else Seq.empty
    val reasonableExcuseContent = reasonableExcuse match {
      case "bereavement" => Seq(
        CheckYourAnswersRow(
          messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("bereavementReason.headingAndTitle"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidThePersonDie).get)),
          changeAnswerUrl(
            controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
            WhenDidThePersonDiePage
          )
        )
      )

      case "crime" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("crimeReason.headingAndTitle"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfCrime).get)),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
            WhenDidCrimeHappenPage
          )
        ),
        CheckYourAnswersRow(messages("crimeReason.hasBeenReported.headingAndTitle"),
          messages(s"common.radioOption.${request.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get}"),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
            HasCrimeBeenReportedPage
          )
        )
      )

      case "fireOrFlood" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("fireOrFloodReason.headingAndTitle"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfFireOrFlood).get)),
          changeAnswerUrl(
            controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
            WhenDidFireOrFloodHappenPage
          )
        )
      )

      case "lossOfStaff" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("lossOfStaffReason.headingAndTitle"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenPersonLeftTheBusiness).get)),
          changeAnswerUrl(
            controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
            WhenDidPersonLeaveTheBusinessPage
          )
        )
      )

      case "technicalIssues" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("technicalIssues.begin.headingAndTitle"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidTechnologyIssuesBegin).get)),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
            WhenDidTechnologyIssuesBeginPage
          )
        ),
        CheckYourAnswersRow(messages("technicalIssues.end.headingAndTitle"),
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
            messages("otherReason.whyReturnSubmittedLate.lpp.headingAndTitle")
          } else {
            messages("otherReason.whyReturnSubmittedLate.headingAndTitle")
          }
        }
        val base = Seq(
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("whenDidBecomeUnable.other")),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidBecomeUnable).get)),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage
            )
          ),
          CheckYourAnswersRow(statementOfLatenessForLPPOrLSP,
            request.session.get(SessionKeys.whyReturnSubmittedLate).get,
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
              WhyWasReturnSubmittedLatePage
            )
          ),
          CheckYourAnswersRow(messages("otherReason.uploadEvidence.question.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.isUploadEvidence).get}"),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
              UploadEvidenceQuestionPage
            )
          )
        )

        if (request.session.get(SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
          base :+ CheckYourAnswersRow(
            messages("checkYourAnswers.other.fileEvidence"),
            if (fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          )
        } else {
          base
        }
    }

    request.session.get(SessionKeys.lateAppealReason).fold(
      multiplePenaltiesContent ++ reasonableExcuseContent
    )(
      reason => {
        multiplePenaltiesContent ++ reasonableExcuseContent :+ (
          CheckYourAnswersRow(messages("checkYourAnswers.whyYouDidNotAppealSooner"),
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

  def getHealthReasonAnswers()(implicit request: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    (request.session.get(SessionKeys.wasHospitalStayRequired), request.session.get(SessionKeys.hasHealthEventEnded)) match {
      //No hospital stay
      case (Some("no"), _) =>
        Seq(
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          CheckYourAnswersRow(messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
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
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.whenDidHospitalStayBegin.headingAndTitle"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.yes.heading"),
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
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${request.session.get(SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )),
          CheckYourAnswersRow(messages("healthReason.whenDidHospitalStayBegin.headingAndTitle"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.headingAndTitle"),
            messages(s"common.radioOption.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            ))
        )
      case _ => throw new MatchError("[SessionAnswersHelper][getHealthReasonAnswers] - Attempted to load CYA page but no valid health reason data found in session")
    }
  }

  def getContentForAgentsCheckYourAnswersPage()(implicit request: Request[_], messages: Messages): Seq[CheckYourAnswersRow] = {

    val seqWhoPlannedToSubmitVATReturn = Seq(
      CheckYourAnswersRow(messages("agents.whoPlannedToSubmitVATReturn.headingAndTitle"),
        messages(s"checkYourAnswers.agents.whoPlannedToSubmitVATReturn.${request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get}"),
        changeAnswerUrl(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage
        )
      )
    )

    val seqWhatCausedAgentToMissDeadline = if (request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get.equals("agent")) {
      Seq(
        CheckYourAnswersRow(messages("agents.whatCausedYouToMissTheDeadline.headingAndTitle"),
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

  def getAllTheContentForCheckYourAnswersPage(uploadFilenames: Option[String] = None)(implicit request: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {

    val reasonableExcuse = request.session.get(SessionKeys.reasonableExcuse)
    val agentSession = request.session.get(SessionKeys.agentSessionVrn).isDefined
    val appealType = request.session.get(SessionKeys.appealType)

    (request.session.get(SessionKeys.isObligationAppeal), reasonableExcuse.isDefined, agentSession) match {
      case (Some(_), _, _) => getContentForObligationAppealCheckYourAnswersPage(uploadFilenames)
      case (_, true, false) if isAllAnswerPresentForReasonableExcuse(reasonableExcuse.get) => getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case (_, true, true) if appealType.contains(PenaltyTypeEnum.Late_Payment.toString) || appealType.contains(PenaltyTypeEnum.Additional.toString) => getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames, isLPP = true)
      case (_, true, true) => getContentForAgentsCheckYourAnswersPage() ++ getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case _ => Seq.empty
    }
  }

  def getContentForObligationAppealCheckYourAnswersPage(fileNames: Option[String] = None)(implicit request: Request[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val base = Seq(
      CheckYourAnswersRow(messages("otherRelevantInformation.headingAndTitle"),
        request.session.get(SessionKeys.otherRelevantInformation).get,
        changeAnswerUrl(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage
        )
      ),
      CheckYourAnswersRow(messages("otherReason.uploadEvidence.question.headingAndTitle"),
        messages(s"common.radioOption.${request.session.get(SessionKeys.isUploadEvidence).get}"),
        changeAnswerUrl(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage
        )
      )
    )
    if (request.session.get(SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
      base :+ CheckYourAnswersRow(
        messages("checkYourAnswers.other.fileEvidence"),
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

  def getContentWithExistingUploadFileNames(reasonableExcuse: String)(implicit request: UserRequest[_], messages: Messages): Future[Seq[CheckYourAnswersRow]] = {
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
