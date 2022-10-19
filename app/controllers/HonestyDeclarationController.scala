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

package controllers

import java.time.LocalDate
import config.featureSwitches.FeatureSwitching
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import helpers.HonestyDeclarationHelper

import javax.inject.Inject
import models.{NormalMode, UserRequest}
import models.pages.{HonestyDeclarationPage, PageMode}
import navigation.Navigation
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.HonestyDeclarationPage
import viewtils.ImplicitDateFormatter

import scala.concurrent.{ExecutionContext, Future}

class HonestyDeclarationController @Inject()(honestDeclarationPage: HonestyDeclarationPage,
                                             errorHandler: ErrorHandler,
                                             navigation: Navigation,
                                             sessionService: SessionService)
                                            (implicit mcc: MessagesControllerComponents,
                                             appConfig: AppConfig,
                                             val config: Configuration,
                                             authorise: AuthPredicate,
                                             dataRequired: DataRequiredAction,
                                             dataRetrieval: DataRetrievalAction,
                                             executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      tryToGetExcuseDatesAndObligationFromSession(
        {
          (reasonableExcuse, dueDate, startDate, endDate, isObligation) => {
            val (friendlyDueDate, friendlyStartDate, friendlyEndDate) =
              (ImplicitDateFormatter.dateToString(dueDate),
                ImplicitDateFormatter.dateToString(startDate),
                ImplicitDateFormatter.dateToString(endDate))
            val reasonText: String = HonestyDeclarationHelper.getReasonText(reasonableExcuse)
            val extraBullets: Seq[String] = HonestyDeclarationHelper.getExtraText(reasonableExcuse)
            Future(Ok(honestDeclarationPage(reasonableExcuse, reasonText,
              friendlyDueDate, friendlyStartDate, friendlyEndDate, extraBullets, isObligation, PageMode(HonestyDeclarationPage, NormalMode))))
          }
        }
      )
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      tryToGetExcuseDatesAndObligationFromSession({
        (reasonableExcuse, _, _, _, isObligation) => {
          logger.debug(s"[HonestyDeclarationController][onSubmit] - Adding 'true' to session for key: ${SessionKeys.hasConfirmedDeclaration}")
          val updatedAnswers = request.answers.setAnswer[Boolean](SessionKeys.hasConfirmedDeclaration, true)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(HonestyDeclarationPage, NormalMode, if(!isObligation) Some(reasonableExcuse) else None))
          }
        }
      }
      )
    }
  }

  private def tryToGetExcuseDatesAndObligationFromSession(fOnSuccess: (String, LocalDate, LocalDate, LocalDate, Boolean) =>
    Future[Result])(implicit request: UserRequest[_]): Future[Result] = {
    (request.answers.getAnswer[String](SessionKeys.reasonableExcuse), request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod),
      request.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod), request.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod),
      request.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined) match {
      case (Some(reasonableExcuse), Some(dueDate), Some(startDate), Some(endDate), isObligation: Boolean) =>
        fOnSuccess(reasonableExcuse, dueDate, startDate, endDate, isObligation)
      case (None, Some(dueDate), Some(startDate), Some(endDate), isObligation: Boolean) if isObligation =>
        fOnSuccess("obligation", dueDate, startDate, endDate, isObligation)
      case _ =>
        logger.error(s"[HonestyDeclarationController][tryToGetExcuseAndDueDateFromSession] - One or more session key was not in session. \n" +
          s"Reasonable excuse defined? ${request.answers.getAnswer[String](SessionKeys.reasonableExcuse).isDefined} \n" +
          s"Due date defined? ${request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).isDefined} \n" +
          s"Start date defined? ${request.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod)} \n" +
          s"End date defined? ${request.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod)} \n")
        Future(errorHandler.showInternalServerError)
    }
  }
}
