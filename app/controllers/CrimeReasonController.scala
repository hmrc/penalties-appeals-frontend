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

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.{HasCrimeBeenReportedForm, WhenDidCrimeHappenForm}
import helpers.FormProviderHelper
import models.Mode
import models.pages._
import navigation.Navigation
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
                                      hasCrimeBeenReportedPage: HasCrimeBeenReportedToPolicePage,
                                      navigation: Navigation)
                                     (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      appConfig: AppConfig,
                                      mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForWhenCrimeHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidCrimeHappenForm.whenCrimeHappenedForm(),
        SessionKeys.dateOfCrime
      )
      val postAction = controllers.routes.CrimeReasonController.onSubmitForWhenCrimeHappened(mode)
      Ok(whenDidCrimeHappenPage(formProvider, postAction, pageMode(WhenDidCrimeHappenPage, mode)))
    }
  }

  def onSubmitForWhenCrimeHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val postAction = controllers.routes.CrimeReasonController.onSubmitForWhenCrimeHappened(mode)
      WhenDidCrimeHappenForm.whenCrimeHappenedForm.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(whenDidCrimeHappenPage(formWithErrors, postAction, pageMode(WhenDidCrimeHappenPage, mode)))
        },
        dateOfCrime => {
          logger.debug(s"[CrimeReasonController][onSubmitForWhenCrimeHappened] - Adding '$dateOfCrime' to session under key: ${SessionKeys.dateOfCrime}")
          Redirect(navigation.nextPage(WhenDidCrimeHappenPage, mode))
            .addingToSession((SessionKeys.dateOfCrime, dateOfCrime.toString))
        }
      )
    }
  }

  def onPageLoadForHasCrimeBeenReported(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        HasCrimeBeenReportedForm.hasCrimeBeenReportedForm,
        SessionKeys.hasCrimeBeenReportedToPolice
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formProvider)
      val postAction = controllers.routes.CrimeReasonController.onSubmitForHasCrimeBeenReported(mode)
      Ok(hasCrimeBeenReportedPage(formProvider, radioOptionsToRender, postAction, pageMode(HasCrimeBeenReportedPage, mode)))
    }
  }

  def onSubmitForHasCrimeBeenReported(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      HasCrimeBeenReportedForm.hasCrimeBeenReportedForm.bindFromRequest.fold(
        formHasErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForHasCrimeBeenReportedPage(formHasErrors)
          val postAction = controllers.routes.CrimeReasonController.onSubmitForHasCrimeBeenReported(mode)
          BadRequest(hasCrimeBeenReportedPage(formHasErrors, radioOptionsToRender, postAction, pageMode(HasCrimeBeenReportedPage, mode)))
        },
        reportedAnswer => {
          logger.debug(
            s"[CrimeReasonController][onSubmitForHasCrimeBeenReported] - Adding '$reportedAnswer' to session under key: ${SessionKeys.hasCrimeBeenReportedToPolice}")
          Redirect(navigation.nextPage(HasCrimeBeenReportedPage, mode))
            .addingToSession((SessionKeys.hasCrimeBeenReportedToPolice, reportedAnswer))
        }
      )
    }
  }

}
