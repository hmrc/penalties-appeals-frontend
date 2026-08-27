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
import models.appeals.QuestionAnswerRow
import models.pages._
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import repositories.UploadJourneyRepository
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import utils.Logger.logger
import utils.SessionKeys
import viewtils.{ImplicitDateFormatter, PenaltyTypeHelper}

import java.time.LocalDate
import javax.inject.Inject
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

  private def answer[A](key: String, caller: String)(implicit userRequest: UserRequest[_], reads: Reads[A]): Option[A] = {
    val maybeAnswer = userRequest.answers.getAnswer[A](key)
    if (maybeAnswer.isEmpty) {
      logger.warn(s"[SessionAnswersHelper][$caller] - Expected answer for session key '$key' but none was found")
    }
    maybeAnswer
  }

  private def stringAnswer(key: String, caller: String, default: String = "")(implicit userRequest: UserRequest[_]): String =
    answer[String](key, caller).getOrElse(default)

  private def dateAnswer(key: String, caller: String)(implicit userRequest: UserRequest[_], messages: Messages): String =
    answer[LocalDate](key, caller).fold("")(dateToString)

  def isAllAnswerPresentForReasonableExcuse(reasonableExcuse: String)(implicit userRequest: UserRequest[_]): Boolean = {
    val keysInSession = userRequest.answers.data.decryptedValue.keys.toSet
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

  private def getMultiplePenaltiesForThisPeriodRows()(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).fold(Seq.empty[QuestionAnswerRow]) { answer =>
      Seq(
        QuestionAnswerRow(
          messages("penaltySelection.form.heading"),
          messages(s"common.radioOption.$answer"),
          changeAnswerUrl(
            controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url,
            PenaltySelectionPage
          )
        )
      )
    }
  }

  //scalastyle:off
  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String, fileNames: Option[String] = None, isLPP: Boolean = false)(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    val multiplePenaltiesContent = getMultiplePenaltiesForThisPeriodRows()
    val ctx = "getContentForReasonableExcuseCheckYourAnswersPage"
    lazy val reasonRow = QuestionAnswerRow(
      messages("checkYourAnswers.reasonableExcuse"),
      messages(s"reasonableExcuses.${stringAnswer(SessionKeys.reasonableExcuse, ctx)}Reason"),
      changeAnswerUrl(
        controllers.routes.ReasonableExcuseController.onPageLoad().url,
        ReasonableExcuseSelectionPage
      )
    )

    val reasonableExcuseContent = reasonableExcuse match {
      case "bereavement" => Seq(
        reasonRow,
        QuestionAnswerRow(messages("bereavementReason.headingAndTitle"),
          dateAnswer(SessionKeys.whenDidThePersonDie, ctx),
          changeAnswerUrl(
            controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
            WhenDidThePersonDiePage
          )
        )
      )

      case "crime" => Seq(
        reasonRow,
        QuestionAnswerRow(messages("crimeReason.headingAndTitle"),
          dateAnswer(SessionKeys.dateOfCrime, ctx),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
            WhenDidCrimeHappenPage
          )
        ),
        QuestionAnswerRow(messages("crimeReason.hasBeenReported.headingAndTitle"),
          messages(s"common.radioOption.${stringAnswer(SessionKeys.hasCrimeBeenReportedToPolice, ctx)}"),
          changeAnswerUrl(
            controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
            HasCrimeBeenReportedPage
          )
        )
      )

      case "fireOrFlood" => Seq(
        reasonRow,
        QuestionAnswerRow(messages("fireOrFloodReason.headingAndTitle"),
          dateAnswer(SessionKeys.dateOfFireOrFlood, ctx),
          changeAnswerUrl(
            controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
            WhenDidFireOrFloodHappenPage
          )
        )
      )

      case "lossOfStaff" => Seq(
        reasonRow,
        QuestionAnswerRow(messages("lossOfStaffReason.headingAndTitle"),
          dateAnswer(SessionKeys.whenPersonLeftTheBusiness, ctx),
          changeAnswerUrl(
            controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
            WhenDidPersonLeaveTheBusinessPage
          )
        )
      )

      case "technicalIssues" => Seq(
        reasonRow,
        QuestionAnswerRow(messages("technicalIssues.begin.headingAndTitle"),
          dateAnswer(SessionKeys.whenDidTechnologyIssuesBegin, ctx),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
            WhenDidTechnologyIssuesBeginPage
          )
        ),
        QuestionAnswerRow(messages("technicalIssues.end.headingAndTitle"),
          dateAnswer(SessionKeys.whenDidTechnologyIssuesEnd, ctx),
          changeAnswerUrl(
            controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
            WhenDidTechnologyIssuesEndPage
          )
        )
      )

      case "health" => getHealthReasonAnswers()

      case "other" =>
        val appealType = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType)
        val isLPPOrAdditional = appealType.exists(t => t == PenaltyTypeEnum.Late_Payment || t == PenaltyTypeEnum.Additional)
        val statementOfLatenessForLPPOrLSP: String =
          if (isLPPOrAdditional) messages("otherReason.whyReturnSubmittedLate.lpp.headingAndTitle")
          else messages("otherReason.whyReturnSubmittedLate.headingAndTitle")

        val isUploadEvidence = stringAnswer(SessionKeys.isUploadEvidence, ctx)

        val base = Seq(
          reasonRow,
          QuestionAnswerRow(messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("whenDidBecomeUnable.other")),
            dateAnswer(SessionKeys.whenDidBecomeUnable, ctx),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage
            )
          ),
          QuestionAnswerRow(statementOfLatenessForLPPOrLSP,
            stringAnswer(SessionKeys.whyReturnSubmittedLate, ctx),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
              WhyWasReturnSubmittedLatePage
            )
          ),
          QuestionAnswerRow(messages("otherReason.uploadEvidence.question.headingAndTitle"),
            messages(s"common.radioOption.$isUploadEvidence"),
            changeAnswerUrl(
              controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
              UploadEvidenceQuestionPage
            )
          )
        )

        if (isUploadEvidence.equalsIgnoreCase("yes")) {
          base :+ QuestionAnswerRow(
            messages("checkYourAnswers.other.fileEvidence"),
            fileNames.filter(_.nonEmpty).getOrElse(messages("checkYourAnswers.other.noFileUpload")),
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, false).url,
            isUploadRow = true
          )
        } else {
          base
        }
    }

    val baseContent = multiplePenaltiesContent ++ reasonableExcuseContent
    userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).fold(baseContent) { reason =>
      val appealBoth = userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties)
      val shouldShowLateReason =
        appealBoth.isEmpty || appealBoth.contains("yes") || isAppealingOnlySinglePenaltyAndIsLateAppealing()

      if (shouldShowLateReason) {
        baseContent :+ QuestionAnswerRow(
          messages("checkYourAnswers.whyYouDidNotAppealSooner"),
          reason,
          changeAnswerUrl(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage
          )
        )
      } else {
        baseContent
      }
    }
  }

  def getHealthReasonAnswers()(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    val ctx = "getHealthReasonAnswers"

    lazy val reasonRow = QuestionAnswerRow(
      messages("checkYourAnswers.reasonableExcuse"),
      messages(s"reasonableExcuses.${stringAnswer(SessionKeys.reasonableExcuse, ctx)}Reason"),
      changeAnswerUrl(
        controllers.routes.ReasonableExcuseController.onPageLoad().url,
        ReasonableExcuseSelectionPage
      )
    )

    lazy val hospitalStayRow = QuestionAnswerRow(
      messages("healthReason.wasHospitalStayRequired.headingAndTitle"),
      messages(s"common.radioOption.${stringAnswer(SessionKeys.wasHospitalStayRequired, ctx)}"),
      changeAnswerUrl(
        controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
        WasHospitalStayRequiredPage
      )
    )

    lazy val hospitalStartedRow = QuestionAnswerRow(
      messages("healthReason.whenDidHospitalStayBegin.headingAndTitle"),
      dateAnswer(SessionKeys.whenHealthIssueStarted, ctx),
      changeAnswerUrl(
        controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
        WhenDidHospitalStayBeginPage
      )
    )

    lazy val hospitalEndedRow = QuestionAnswerRow(
      messages("healthReason.hasTheHospitalStayEnded.headingAndTitle"),
      messages(s"common.radioOption.${stringAnswer(SessionKeys.hasHealthEventEnded, ctx)}"),
      changeAnswerUrl(
        controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
        DidHospitalStayEndPage
      )
    )

    (userRequest.answers.getAnswer[String](SessionKeys.wasHospitalStayRequired),
      userRequest.answers.getAnswer[String](SessionKeys.hasHealthEventEnded)) match {
      //No hospital stay
      case (Some("no"), _) =>
        Seq(
          reasonRow,
          hospitalStayRow,
          QuestionAnswerRow(
            messages(WhenDidYouBecomeUnableHelper.getMessageKeyForPage("health.whenHealthIssueHappened.headingAndTitle")),
            dateAnswer(SessionKeys.whenHealthIssueHappened, ctx),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage
            )
          )
        )

      //Hospital stay ended
      case (Some("yes"), Some("yes")) =>
        Seq(
          reasonRow,
          hospitalStayRow,
          hospitalStartedRow,
          hospitalEndedRow,
          QuestionAnswerRow(
            messages("healthReason.hasTheHospitalStayEnded.yes.heading"),
            dateAnswer(SessionKeys.whenHealthIssueEnded, ctx),
            changeAnswerUrl(
              controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(CheckMode).url,
              WhenDidHospitalStayEndPage
            )
          )
        )

      //Hospital stay ongoing
      case (Some("yes"), Some("no")) =>
        Seq(reasonRow, hospitalStayRow, hospitalStartedRow, hospitalEndedRow)

      case _ =>
        throw new MatchError(
          "[SessionAnswersHelper][getHealthReasonAnswers] - Attempted to load CYA page but no valid health reason data found in session"
        )
    }
  }

  def getContentForAgentsCheckYourAnswersPage()(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    val ctx = "getContentForAgentsCheckYourAnswersPage"
    val whoPlannedToSubmit = stringAnswer(SessionKeys.whoPlannedToSubmitVATReturn, ctx)

    val seqWhoPlannedToSubmitVATReturn = Seq(
      QuestionAnswerRow(messages("agents.whoPlannedToSubmitVATReturn.headingAndTitle"),
        messages(s"checkYourAnswers.agents.whoPlannedToSubmitVATReturn.$whoPlannedToSubmit"),
        changeAnswerUrl(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage
        )
      )
    )

    val seqWhatCausedAgentToMissDeadline = if (whoPlannedToSubmit == "agent") {
      Seq(
        QuestionAnswerRow(messages("agents.whatCausedYouToMissTheDeadline.headingAndTitle"),
          messages(s"checkYourAnswers.agents.whatCausedYouToMissTheDeadline.${stringAnswer(SessionKeys.whatCausedYouToMissTheDeadline, ctx)}"),
          changeAnswerUrl(
            controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
            WhatCausedYouToMissTheDeadlinePage
          )
        ))
    }
    else Seq.empty

    seqWhoPlannedToSubmitVATReturn ++ seqWhatCausedAgentToMissDeadline
  }

  def getAllTheContentForCheckYourAnswersPage(uploadFilenames: Option[String] = None)(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    val agentSession = userRequest.session.get(SessionKeys.agentSessionVrn).isDefined
    val appealType = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType)
    val isLPPOrAdditional = appealType.exists(t => t == PenaltyTypeEnum.Late_Payment || t == PenaltyTypeEnum.Additional)

    userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse) match {
      case Some(reason) if !agentSession && isAllAnswerPresentForReasonableExcuse(reason) =>
        getContentForReasonableExcuseCheckYourAnswersPage(reason, uploadFilenames)
      case Some(reason) if agentSession && isLPPOrAdditional =>
        getContentForReasonableExcuseCheckYourAnswersPage(reason, uploadFilenames, isLPP = true)
      case Some(reason) if agentSession =>
        getContentForAgentsCheckYourAnswersPage() ++ getContentForReasonableExcuseCheckYourAnswersPage(reason, uploadFilenames)
      case _ => Seq.empty
    }
  }

  def getPreviousUploadsFileNames(journeyId: String): Future[String] = {
    uploadJourneyRepository.getUploadsForJourney(Some(journeyId)).map { uploads =>
      uploads.getOrElse(Seq.empty)
        .flatMap(_.uploadDetails.map(_.fileName))
        .mkString(", ")
    }
  }

  def getContentWithExistingUploadFileNames(reasonableExcuse: String)(implicit userRequest: UserRequest[_], messages: Messages): Future[Seq[QuestionAnswerRow]] = {
    if (reasonableExcuse != "other") {
      Future.successful(getAllTheContentForCheckYourAnswersPage()(userRequest, messages))
    } else {
      userRequest.session.get(SessionKeys.journeyId) match {
        case Some(journeyId) =>
          getPreviousUploadsFileNames(journeyId).map { fileNames =>
            val maybeFileNames = Option(fileNames).filter(_.nonEmpty)
            getAllTheContentForCheckYourAnswersPage(maybeFileNames)(userRequest, messages)
          }
        case None =>
          logger.warn("[SessionAnswersHelper][getContentWithExistingUploadFileNames] - No journey ID found in session, falling back to empty file names")
          Future.successful(getAllTheContentForCheckYourAnswersPage()(userRequest, messages))
      }
    }
  }

  def changeAnswerUrl(continueUrl: String, page: Page): String = {
    val safeContinueUrl = RedirectUrl(continueUrl)
    controllers.routes.CheckYourAnswersController.changeAnswer(safeContinueUrl, page.toString).url
  }

  private def isAppealingOnlySinglePenaltyAndIsLateAppealing()(implicit userRequest: UserRequest[_]) = {
    val dateTimeNow: LocalDate = dateTimeHelper.dateNow
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("no") &&
      userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).exists(_.isBefore(dateTimeNow.minusDays(appConfig.daysRequiredForLateAppeal)))
  }

  def getSubmittedAnswers(dateNow: LocalDate)(implicit userRequest: UserRequest[_], messages: Messages): Seq[QuestionAnswerRow] = {
    val isAppealingMultiplePenalties: Boolean =
      userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes")

    val penaltyHeaderText = PenaltyTypeHelper.getKeysFromSession() match {
      case Some(keys) if keys.size >= 3 =>
        messages("penaltyInformation.headerText", keys.head, keys(1), keys.last, "")
      case _ =>
        logger.warn("[SessionAnswersHelper][getSubmittedAnswers] - Could not retrieve penalty type/period keys from session")
        ""
    }

    Seq(
      QuestionAnswerRow(messages("viewAppealDetails.vrn"), userRequest.vrn, ""),
      QuestionAnswerRow(
        if (isAppealingMultiplePenalties) messages("viewAppealDetails.penaltyAppealed.multiple")
        else messages("viewAppealDetails.penaltyAppealed"),
        penaltyHeaderText,
        ""
      ),
      QuestionAnswerRow(messages("viewAppealDetails.appealDate"), dateNow, ""),
      QuestionAnswerRow(messages("viewAppealDetails.reviewPeriod"), dateNow.plusDays(44), "")
    )
  }
}
