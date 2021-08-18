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

package controllers

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.HonestyDeclarationForm._
import helpers.HonestyDeclarationHelper
import models.NormalMode
import models.pages.HonestyDeclarationPage
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.HonestyDeclarationPage
import viewtils.ImplicitDateFormatter

import java.time.LocalDateTime
import javax.inject.Inject

class HonestyDeclarationController @Inject()(honestDeclarationPage: HonestyDeclarationPage,
                                             errorHandler: ErrorHandler,
                                             navigation: Navigation)
                                            (implicit mcc: MessagesControllerComponents,
                                             appConfig: AppConfig,
                                             authorise: AuthPredicate,
                                             dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      tryToGetExcuseDatesAndObligationFromSession(
        {
          (reasonableExcuse, dueDate, startDate, endDate, isObligation) => {
            val friendlyDueDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(dueDate))
            val friendlyStartDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(startDate))
            val friendlyEndDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(endDate))
            val reasonText: String = HonestyDeclarationHelper.getReasonText(reasonableExcuse)
            val extraBullets: Seq[String] = HonestyDeclarationHelper.getExtraText(reasonableExcuse)
            Ok(honestDeclarationPage(honestyDeclarationForm, reasonText,
              friendlyDueDate, friendlyStartDate, friendlyEndDate, extraBullets, isObligation))
          }
        }
      )
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      tryToGetExcuseDatesAndObligationFromSession({
        (reasonableExcuse, dueDate, startDate, endDate, isObligation) => {
          honestyDeclarationForm.bindFromRequest.fold(
            formWithErrors => {
              logger.debug(s"[HonestyDeclarationController][onSubmit] - Form had errors: ${formWithErrors.errors}")
              val friendlyDueDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(dueDate))
              val friendlyStartDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(startDate))
              val friendlyEndDate: String = ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(endDate))
              val reasonText: String = HonestyDeclarationHelper.getReasonText(reasonableExcuse)
              val extraBullets: Seq[String] = HonestyDeclarationHelper.getExtraText(reasonableExcuse)
              BadRequest(honestDeclarationPage(formWithErrors, reasonText,
                friendlyDueDate, friendlyStartDate, friendlyEndDate, extraBullets, isObligation))
            },
            _ => {
              logger.debug(s"[HonestyDeclarationController][onSubmit] - Adding 'true' to session for key: ${SessionKeys.hasConfirmedDeclaration}")
              if(isObligation) {
                Redirect(navigation.nextPage(HonestyDeclarationPage, NormalMode, None))
                  .addingToSession((SessionKeys.hasConfirmedDeclaration, "true"))
              } else {
                Redirect(navigation.nextPage(HonestyDeclarationPage, NormalMode, Some(reasonableExcuse)))
                  .addingToSession((SessionKeys.hasConfirmedDeclaration, "true"))
              }
            }
          )
        }
      })
    }
  }

  private def tryToGetExcuseDatesAndObligationFromSession(fOnSuccess: (String, String, String, String, Boolean) =>
    Result)(implicit request: Request[_]): Result = {
    (request.session.get(SessionKeys.reasonableExcuse), request.session.get(SessionKeys.dueDateOfPeriod),
      request.session.get(SessionKeys.startDateOfPeriod), request.session.get(SessionKeys.endDateOfPeriod),
      request.session.get(SessionKeys.isObligationAppeal).isDefined) match {
      case (Some(reasonableExcuse), Some(dueDate), Some(startDate), Some(endDate), isObligation: Boolean) => {
        fOnSuccess(reasonableExcuse, dueDate, startDate, endDate, isObligation)
      }
      case (None, Some(dueDate), Some(startDate), Some(endDate), isObligation: Boolean) if isObligation => {
        fOnSuccess("obligation", dueDate, startDate, endDate, isObligation)
      }
      case _ => {
        logger.error(s"[HonestyDeclarationController][tryToGetExcuseAndDueDateFromSession] - One or more session key was not in session. \n" +
          s"Reasonable excuse defined? ${request.session.get(SessionKeys.reasonableExcuse).isDefined} \n" +
          s"Due date defined? ${request.session.get(SessionKeys.dueDateOfPeriod).isDefined} \n" +
          s"Start date defined? ${request.session.get(SessionKeys.startDateOfPeriod)} \n" +
          s"End date defined? ${request.session.get(SessionKeys.endDateOfPeriod)} \n")
        errorHandler.showInternalServerError
      }
    }
  }
}
