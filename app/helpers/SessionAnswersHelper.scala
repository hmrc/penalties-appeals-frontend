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

package helpers

import java.time.LocalDate
import javax.inject.Inject
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import play.api.mvc.Request
import repositories.UploadJourneyRepository
import utils.MessageRenderer.getMessage
import utils.SessionKeys
import viewtils.ImplicitDateFormatter

import scala.concurrent.{ExecutionContext, Future}

class SessionAnswersHelper @Inject() (uploadJourneyRepository: UploadJourneyRepository)(implicit ec: ExecutionContext)extends ImplicitDateFormatter {
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
    "other" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whyReturnSubmittedLate, SessionKeys.whenDidBecomeUnable)
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
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
        (messages("checkYourAnswers.bereavement.whenDidThePersonDie"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidThePersonDie).get)),
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url)
      )

      case "crime" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
        (messages("checkYourAnswers.crime.whenDidTheCrimeHappen"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfCrime).get)),
          controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url),
        (messages("checkYourAnswers.crime.hasCrimeBeenReported"),
          messages(s"checkYourAnswers.crime.${request.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get}"),
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url)
      )

      case "fireOrFlood" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
        (messages("checkYourAnswers.fireOrFlood.whenDidTheFireOrFloodHappen"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfFireOrFlood).get)),
          controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url)
      )

      case "lossOfStaff" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
        (messages("checkYourAnswers.lossOfStaff.whenPersonBecameUnavailable"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenPersonLeftTheBusiness).get)),
          controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url)
      )

      case "technicalIssues" => Seq(
        (messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
        (messages("checkYourAnswers.technicalIssues.whenDidTechIssuesBegin"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidTechnologyIssuesBegin).get)),
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url),
        (messages("checkYourAnswers.technicalIssues.whenDidTechIssuesEnd"),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidTechnologyIssuesEnd).get)),
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url)
      )

      case "health" => getHealthReasonAnswers

      case "other" =>

        val statementOfLatenessForLPPOrLSP: String = {
          if(request.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment.toString) || request.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Additional.toString)) {
            messages("checkYourAnswers.other.lpp.statementOfLateness")
          } else {
            messages("checkYourAnswers.other.statementOfLateness")
          }
        }
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (getMessage("checkYourAnswers.other.unableToManageAccount"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidBecomeUnable).get)),
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url),
          (statementOfLatenessForLPPOrLSP,
            request.session.get(SessionKeys.whyReturnSubmittedLate).get,
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url),
          (messages("checkYourAnswers.other.fileEvidence"),
            if(fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url)
        )
    }

    request.session.get(SessionKeys.lateAppealReason).fold(
      reasonableExcuseContent
    )(
      reason => {
        reasonableExcuseContent :+ (
          (messages("checkYourAnswers.whyYouDidNotAppealSooner"),
            reason,
            controllers.routes.MakingALateAppealController.onPageLoad().url)
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
            controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
          (getMessage("checkYourAnswers.health.unableToManageAccount"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueHappened).get)),
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url)
        )
      //Hospital stay ended
      case (Some("yes"), Some("yes")) =>
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
          (messages("checkYourAnswers.health.whenDidHospitalStayBegin"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url),
          (messages("checkYourAnswers.health.hasTheHospitalStayEnded"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url),
          (messages("checkYourAnswers.health.whenDidHospitalStayEnd"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueEnded).get)),
            controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url)
        )

      //Hospital stay ongoing
      case (Some("yes"), Some("no")) =>
        Seq((messages("checkYourAnswers.reasonableExcuse"),
          messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
          controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (messages("checkYourAnswers.health.hospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
          (messages("checkYourAnswers.health.whenDidHospitalStayBegin"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueStarted).get)),
            controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url),
          (messages("checkYourAnswers.health.hasTheHospitalStayEnded"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.hasHealthEventEnded).get}"),
            controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url)
        )
    }
  }

  def getContentForAgentsCheckYourAnswersPage()(implicit request: Request[_], messages: Messages): Seq[(String, String, String)] = {

    val seqWhoPlannedToSubmitVATReturn = Seq(
      (messages("checkYourAnswers.agents.whoPlannedToSubmitVATReturn"),
        messages(s"checkYourAnswers.agents.whoPlannedToSubmitVATReturn.${request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get}"),
        controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url))

    val seqWhyWasTheReturnSubmittedLate = if(request.session.get(SessionKeys.whoPlannedToSubmitVATReturn).get.equals("agent")) {
      Seq((messages("checkYourAnswers.agents.whyWasTheReturnSubmittedLate"),
        messages(s"checkYourAnswers.agents.whyWasTheReturnSubmittedLate.${request.session.get(SessionKeys.causeOfLateSubmissionAgent).get}"),
        controllers.routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url))
    }
    else Seq.empty

    seqWhoPlannedToSubmitVATReturn ++ seqWhyWasTheReturnSubmittedLate
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
    Seq(
      (messages("checkYourAnswers.obligation.whyYouWantToAppealPenalty"),
        request.session.get(SessionKeys.otherRelevantInformation).get,
        controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url),
      (messages("checkYourAnswers.obligation.fileEvidence"),
        if(fileNames.contains("") || fileNames.isEmpty) messages("checkYourAnswers.other.noFileUpload") else fileNames.get,
        controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      )
    )
  }

  def getPreviousUploadsFileNames()(implicit request: UserRequest[_]): Future[String]= {
    for {
      previousUploads <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
    } yield {
      val previousUploadsFileName = previousUploads.map(_.map(file => file.uploadDetails.map(details => details.fileName)))
      previousUploadsFileName.getOrElse(Seq.empty).collect {
        case Some(x) => x
      }.mkString(", ")
    }
  }

  def getContentWithExistingUploadFileNames(reasonableExcuse:String)(implicit request: UserRequest[_], messages: Messages): Future[Seq[(String, String, String)]]={
    if(!reasonableExcuse.equals("other")){
       Future(getAllTheContentForCheckYourAnswersPage()(request,messages))
    }
    else {
       for {
         fileNames <- getPreviousUploadsFileNames()(request)
       } yield {
         getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))(request,messages)
       }
    }
  }
}
