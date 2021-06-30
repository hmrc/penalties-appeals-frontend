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

import models.{CheckMode, UserRequest}
import play.api.i18n.Messages
import play.api.mvc.Request
import utils.SessionKeys
import viewtils.ImplicitDateFormatter

import java.time.LocalDate

object SessionAnswersHelper extends ImplicitDateFormatter {
  val answersRequiredForReasonableExcuseJourney: Map[String, Seq[String]] = Map(
    "crime" -> Seq(SessionKeys.hasCrimeBeenReportedToPolice, SessionKeys.reasonableExcuse, SessionKeys.dateOfCrime, SessionKeys.hasConfirmedDeclaration),
    "lossOfStaff" -> Seq(SessionKeys.whenPersonLeftTheBusiness, SessionKeys.reasonableExcuse, SessionKeys.hasConfirmedDeclaration),
    "fireOrFlood" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.dateOfFireOrFlood, SessionKeys.hasConfirmedDeclaration),
    "technicalIssues" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whenDidTechnologyIssuesBegin, SessionKeys.whenDidTechnologyIssuesEnd),
    "healthIssueHospitalStayOngoing" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whenHealthIssueStarted, SessionKeys.hasHealthEventEnded, SessionKeys.wasHospitalStayRequired),
    "healthIssueHospitalStayEnded" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whenHealthIssueStarted, SessionKeys.whenHealthIssueEnded, SessionKeys.hasHealthEventEnded, SessionKeys.wasHospitalStayRequired),
    "healthIssueNoHospitalStay" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.wasHospitalStayRequired, SessionKeys.whenHealthIssueHappened),
    "other" -> Seq(SessionKeys.reasonableExcuse, SessionKeys.whyReturnSubmittedLate, SessionKeys.whenDidBecomeUnable)
  )

  def isAllAnswerPresentForReasonableExcuse(reasonableExcuse: String)(implicit request: Request[_]): Boolean = {
    val keysInSession = request.session.data.keys.toSet
    reasonableExcuse match {
      case "health" => {
        (request.session.get(SessionKeys.wasHospitalStayRequired), request.session.get(SessionKeys.hasHealthEventEnded)) match {
          //No hospital stay
          case (Some("no"), _) => {
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueNoHospitalStay").toSet
            answersRequired.subsetOf(keysInSession)
          }
          //Hospital stay ongoing
          case (Some("yes"), Some("no")) => {
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueHospitalStayOngoing").toSet
            answersRequired.subsetOf(keysInSession)
          }

          //Hospital stay ended
          case (Some("yes"), Some("yes")) => {
            val answersRequired = answersRequiredForReasonableExcuseJourney("healthIssueHospitalStayEnded").toSet
            answersRequired.subsetOf(keysInSession)
          }

          //Wrong configuration of health answers
          case _ => false
        }
      }
      case _ => {
        val answersRequired = answersRequiredForReasonableExcuseJourney(reasonableExcuse).toSet
        answersRequired.subsetOf(keysInSession)
      }
    }

  }

  //scalastyle:off
  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String)(implicit request: Request[_],
                                                                                  messages: Messages): Seq[(String, String, String)] = {
    val reasonableExcuseContent = reasonableExcuse match {
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

      case "other" => {
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (messages("checkYourAnswers.other.unableToManageAccount"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenDidBecomeUnable).get)),
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url),
          (messages("checkYourAnswers.other.statementOfLateness"),
            request.session.get(SessionKeys.whyReturnSubmittedLate).get,
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url),
          (messages("checkYourAnswers.other.fileEvidence"),
            //TODO: replace with default message
            request.session.get(SessionKeys.evidenceFileName).getOrElse(""),
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url)
        )
      }
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

  def getHealthReasonAnswers()(implicit request: Request[_], messages: Messages): Seq[(String, String, String)] = {
    (request.session.get(SessionKeys.wasHospitalStayRequired), request.session.get(SessionKeys.hasHealthEventEnded)) match {
      //No hospital stay
      case (Some("no"), _) => {
        Seq(
          (messages("checkYourAnswers.reasonableExcuse"),
            messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"),
            controllers.routes.ReasonableExcuseController.onPageLoad().url),
          (messages("checkYourAnswers.health.unexpectedHospitalStay"),
            messages(s"checkYourAnswers.health.${request.session.get(SessionKeys.wasHospitalStayRequired).get}"),
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
          (messages("checkYourAnswers.health.unableToManageAccount"),
            dateToString(LocalDate.parse(request.session.get(SessionKeys.whenHealthIssueHappened).get)),
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url)
        )
      }
      //Hospital stay ongoing
      case (Some("yes"), Some("yes")) => {
        //TODO: implement
        Seq()
      }

      //Hospital stay ended
      case (Some("yes"), Some("no")) => {
        //TODO: implement
        Seq()
      }
    }
  }
}
