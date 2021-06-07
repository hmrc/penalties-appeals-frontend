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

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.{HasCrimeBeenReportedForm, ReasonableExcuseForm, WhenDidCrimeHappenForm}
import helpers.FormProviderHelper
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.crime._
import viewtils.RadioOptionHelper

import java.time.LocalDate
import javax.inject.Inject

class CrimeReasonController @Inject()(whenDidCrimeHappenPage: WhenDidCrimeHappenPage,
                                      hasCrimeBeenReportedPage: HasCrimeBeenReportedToPolicePage)
                                     (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      appConfig: AppConfig,
                                      mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenCrimeHappened(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidCrimeHappenForm.whenCrimeHappenedForm(),
        SessionKeys.dateOfCrime
      )
      Ok(whenDidCrimeHappenPage(formProvider))
    }
  }

  def onSubmitForWhenCrimeHappened(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      WhenDidCrimeHappenForm.whenCrimeHappenedForm.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(whenDidCrimeHappenPage(formWithErrors))
        },
        dateOfCrime => {
          logger.debug(s"[CrimeReasonController][onSubmitForWhenCrimeHappened] - Adding '$dateOfCrime' to session under key: ${SessionKeys.dateOfCrime}")
          Redirect(routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported())
            .addingToSession((SessionKeys.dateOfCrime, dateOfCrime.toString))
        }
      )
    }
  }

  def onPageLoadForHasCrimeBeenReported(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        HasCrimeBeenReportedForm.hasCrimeBeenReportedForm,
        SessionKeys.hasCrimeBeenReportedToPolice
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formProvider)
      Ok(hasCrimeBeenReportedPage(formProvider, radioOptionsToRender))
    }
  }

  def onSubmitForHasCrimeBeenReported(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      HasCrimeBeenReportedForm.hasCrimeBeenReportedForm.bindFromRequest.fold(
        formHasErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formHasErrors)
          BadRequest(hasCrimeBeenReportedPage(formHasErrors, radioOptionsToRender))
        },
        reportedAnswer => {
          logger.debug(s"[CrimeReasonController][onSubmitForHasCrimeBeenReported] - Adding '$reportedAnswer' to session under key: ${SessionKeys.hasCrimeBeenReportedToPolice}")
          Redirect(controllers.routes.CheckYourAnswersController.onPageLoad())
            .addingToSession((SessionKeys.hasCrimeBeenReportedToPolice, reportedAnswer))
        }
      )
    }
  }

}
