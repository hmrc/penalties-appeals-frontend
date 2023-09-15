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

package helpers

import config.AppConfig
import models.appeals.CheckYourAnswersRow
import models.pages._
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import repositories.UploadJourneyRepository
import utils.SessionKeys
import viewtils.{ImplicitDateFormatter, PenaltyTypeHelper}
import java.time.LocalDate

import javax.inject.Inject
import models.session.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class SessionAnswersHelper @Inject()(uploadJourneyRepository: UploadJourneyRepository,
                                     appConfig: AppConfig,
                                     dateTimeHelper: DateTimeHelper)(implicit ec: ExecutionContext) extends ImplicitDateFormatter {
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

  def isAllAnswerPresentForReasonableExcuse(reasonableExcuse: String)(implicit userRequest: UserRequest[_]): Boolean = {
    val keysInSession = userRequest.answers.data.keys.toSet
    reasonableExcuse match {
      case "health" =>
        (userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired), userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded)) match {
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

  private def getMultiplePenaltiesForThisPeriodRows()(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val answer = userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).map(answer => messages(s"common.radioOption.$answer")).get
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
  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String, fileNames: Option[String] = None, isLPP: Boolean = false)(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val multiplePenaltiesContent = if (userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).isDefined) getMultiplePenaltiesForThisPeriodRows() else Seq.empty
    val reasonableExcuseContent = reasonableExcuse match {
      case "bereavement" => Seq(
        CheckYourAnswersRow(
          messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("bereavementReason.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidThePersonDie).get),
          changeAnswerUrl(
            controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
            WhenDidThePersonDiePage
          )
        )
      )

      case "crime" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("crimeReason.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.dateOfCrime).get),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
            WhenDidCrimeHappenPage
          )
        ),
        CheckYourAnswersRow(messages("crimeReason.hasBeenReported.headingAndTitle"),
          messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.hasCrimeBeenReportedToPolice).get}"),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
            HasCrimeBeenReportedPage
          )
        )
      )

      case "fireOrFlood" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("fireOrFloodReason.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.dateOfFireOrFlood).get),
          changeAnswerUrl(
            controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
            WhenDidFireOrFloodHappenPage
          )
        )
      )

      case "lossOfStaff" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("lossOfStaffReason.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenPersonLeftTheBusiness).get),
          changeAnswerUrl(
            controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
            WhenDidPersonLeaveTheBusinessPage
          )
        )
      )

      case "technicalIssues" => Seq(
        CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
          messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
          changeAnswerUrl(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage
          )
        ),
        CheckYourAnswersRow(messages("technicalIssues.begin.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesBegin).get),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
            WhenDidTechnologyIssuesBeginPage
          )
        ),
        CheckYourAnswersRow(messages("technicalIssues.end.headingAndTitle"),
          dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesEnd).get),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
            WhenDidTechnologyIssuesEndPage
          )
        )
      )

      case "health" => getHealthReasonAnswers()

      case "other" =>
        val statementOfLatenessForLPPOrLSP: String = {
          if (userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment) || userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Additional)) {
            messages("otherReason.whyReturnSubmittedLate.lpp.headingAndTitle")
          } else {
            messages("otherReason.whyReturnSubmittedLate.headingAndTitle")
          }
        }
        val base = Seq(
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("whenDidBecomeUnable.other")),
            dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidBecomeUnable).get),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage
            )
          ),
          CheckYourAnswersRow(statementOfLatenessForLPPOrLSP,
            userRequest.answers.getAnswer[String](SessionKeys.whyReturnSubmittedLate).get,
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
              WhyWasReturnSubmittedLatePage
            )
          ),
          CheckYourAnswersRow(messages("otherReason.uploadEvidence.question.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).get}"),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
              UploadEvidenceQuestionPage
            )
          )
        )

        if (userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
          base :+ CheckYourAnswersRow(
            messages("checkYourAnswers.other.fileEvidence"),
            if (fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, false).url,
            isUploadRow = true
          )
        } else {
          base
        }
    }

    userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).fold(
      multiplePenaltiesContent ++ reasonableExcuseContent
    )(
      reason => {
        if(userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).isEmpty ||
          userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes") ||
          isAppealingOnlySinglePenalty()) {
          multiplePenaltiesContent ++ reasonableExcuseContent :+ (
            CheckYourAnswersRow(messages("checkYourAnswers.whyYouDidNotAppealSooner"),
              reason,
              changeAnswerUrl(
                controllers.routes.MakingALateAppealController.onPageLoad().url,
                MakingALateAppealPage
              )
            )
            )
        } else {
          multiplePenaltiesContent ++ reasonableExcuseContent
        }
      }
    )
  }

  def getHealthReasonAnswers()(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    (userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired), userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded)) match {
      //No hospital stay
      case (Some("no"), _) =>
        Seq(
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          CheckYourAnswersRow(messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
            dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueHappened).get),
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
            messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.whenDidHospitalStayBegin.headingAndTitle"),
            dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted).get),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            )
          ),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.yes.heading"),
            dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueEnded).get),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(CheckMode).url,
              WhenDidHospitalStayEndPage
            )
          )
        )

      //Hospital stay ongoing
      case (Some("yes"), Some("no")) =>
        Seq(
          CheckYourAnswersRow(messages("checkYourAnswers.reasonableExcuse"),
            messages(s"reasonableExcuses.${userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).get}Reason"),
            changeAnswerUrl(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage
            )),
          CheckYourAnswersRow(messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage
            )),
          CheckYourAnswersRow(messages("healthReason.whenDidHospitalStayBegin.headingAndTitle"),
            dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted).get),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
              WhenDidHospitalStayBeginPage
            )),
          CheckYourAnswersRow(messages("healthReason.hasTheHospitalStayEnded.headingAndTitle"),
            messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded).get}"),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
              DidHospitalStayEndPage
            ))
        )
      case _ => throw new MatchError("[SessionAnswersHelper][getHealthReasonAnswers] - Attempted to load CYA page but no valid health reason data found in session")
    }
  }

  def getContentForAgentsCheckYourAnswersPage()(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {

    val seqWhoPlannedToSubmitVATReturn = Seq(
      CheckYourAnswersRow(messages("agents.whoPlannedToSubmitVATReturn.headingAndTitle"),
        messages(s"checkYourAnswers.agents.whoPlannedToSubmitVATReturn.${userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).get}"),
        changeAnswerUrl(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage
        )
      )
    )

    val seqWhatCausedAgentToMissDeadline = if (userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).get.equals("agent")) {
      Seq(
        CheckYourAnswersRow(messages("agents.whatCausedYouToMissTheDeadline.headingAndTitle"),
          messages(s"checkYourAnswers.agents.whatCausedYouToMissTheDeadline.${userRequest.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).get}"),
          changeAnswerUrl(
            controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
            WhatCausedYouToMissTheDeadlinePage
          )
        ))
    }
    else Seq.empty

    seqWhoPlannedToSubmitVATReturn ++ seqWhatCausedAgentToMissDeadline
  }

  def getAllTheContentForCheckYourAnswersPage(uploadFilenames: Option[String] = None)(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {

    val reasonableExcuse = userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse)
    val agentSession = userRequest.session.get(SessionKeys.agentSessionVrn).isDefined
    val appealType = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType)

    (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal), reasonableExcuse.isDefined, agentSession) match {
      case (Some(_), _, _) => getContentForObligationAppealCheckYourAnswersPage(uploadFilenames)
      case (_, true, false) if isAllAnswerPresentForReasonableExcuse(reasonableExcuse.get) => getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case (_, true, true) if appealType.contains(PenaltyTypeEnum.Late_Payment) || appealType.contains(PenaltyTypeEnum.Additional) => getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames, isLPP = true)
      case (_, true, true) => getContentForAgentsCheckYourAnswersPage() ++ getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse.get, uploadFilenames)
      case _ => Seq.empty
    }
  }

  def getContentForObligationAppealCheckYourAnswersPage(fileNames: Option[String] = None)(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
    val base = Seq(
      CheckYourAnswersRow(messages("otherRelevantInformation.headingAndTitle"),
        userRequest.answers.getAnswer[String](SessionKeys.otherRelevantInformation).get,
        changeAnswerUrl(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage
        )
      ),
      CheckYourAnswersRow(messages("otherReason.uploadEvidence.question.headingAndTitle"),
        messages(s"common.radioOption.${userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).get}"),
        changeAnswerUrl(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage
        )
      )
    )
    if (userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).get.equalsIgnoreCase("yes")) {
      base :+ CheckYourAnswersRow(
        messages("checkYourAnswers.other.fileEvidence"),
        if (fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
        controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, false).url,
        isUploadRow = true
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

  def getContentWithExistingUploadFileNames(reasonableExcuse: String)(implicit userRequest: UserRequest[_], messages: Messages): Future[Seq[CheckYourAnswersRow]] = {
    if (!reasonableExcuse.equals("other") && !userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).contains(true)) {
      Future(getAllTheContentForCheckYourAnswersPage()(userRequest, messages))
    }
    else {
      for {
        fileNames <- getPreviousUploadsFileNames()(userRequest)
      } yield {
        getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))(userRequest, messages)
      }
    }
  }

  def changeAnswerUrl(continueUrl: String, page: Page): String = {
    controllers.routes.CheckYourAnswersController.changeAnswer(continueUrl, page.toString).url
  }

  private def isAppealingOnlySinglePenalty()(implicit userRequest: UserRequest[_]) = {
    val dateTimeNow: LocalDate = dateTimeHelper.dateNow
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("no") &&
      userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).exists(_.isBefore(dateTimeNow.minusDays(appConfig.daysRequiredForLateAppeal)))
  }

  def viewAppealRows()(implicit userRequest: UserRequest[_], messages: Messages): Seq[CheckYourAnswersRow] = {
     Seq(CheckYourAnswersRow(
       messages("viewAppealDetails.vrn"),
       userRequest.vrn,
       ""
     ),
    CheckYourAnswersRow(
      messages("viewAppealDetails.penaltyAppealed"),
      messages("penaltyInformation.headerText",
        PenaltyTypeHelper.getKeysFromSession().get.head,
        PenaltyTypeHelper.getKeysFromSession().get(1),
        PenaltyTypeHelper.getKeysFromSession().get.last),
      ""
    ))
  }
}
