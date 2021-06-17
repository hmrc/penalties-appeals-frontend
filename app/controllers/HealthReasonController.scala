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

import java.time.LocalDate

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.{WasHospitalStayRequiredForm, WhenDidHealthIssueHappenForm}
import javax.inject.Inject
import helpers.FormProviderHelper
import models.Mode
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health.{WasHospitalStayRequiredPage, WhenDidHealthReasonHappenPage}
import viewtils.RadioOptionHelper

class HealthReasonController @Inject()(navigation: Navigation,
                                       wasHospitalStayRequiredPage: WasHospitalStayRequiredPage,
                                       whenDidHealthReasonHappenPage: WhenDidHealthReasonHappenPage)
                                      (implicit authorise: AuthPredicate,
                                       dataRequired: DataRequiredAction,
                                       appConfig: AppConfig,
                                       mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {


  def onPageLoadForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
      WasHospitalStayRequiredForm.wasHospitalStayRequiredForm,
      SessionKeys.wasHospitalStayRequired
    )
    val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForWasHospitalStayRequiredPage(formProvider)
    val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
    Ok(wasHospitalStayRequiredPage(formProvider, radioOptionsToRender, postAction))
  }

  def onSubmitForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    WasHospitalStayRequiredForm.wasHospitalStayRequiredForm.bindFromRequest.fold(
      formWithErrors => {
        val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForWasHospitalStayRequiredPage(formWithErrors)
        val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
        BadRequest(wasHospitalStayRequiredPage(formWithErrors, radioOptionsToRender, postAction))
      },
      //TODO - update redirect when routing added in PRM-329
      wasStayRequiredAnswer => {
        Redirect("#").addingToSession((SessionKeys.wasHospitalStayRequired, wasStayRequiredAnswer))
      }
    )
  }

  def onPageLoadForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm(),
      SessionKeys.whenHealthIssueHappened
    )
    val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
    Ok(whenDidHealthReasonHappenPage(formProvider,postAction))
  }

  def onSubmitForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm.bindFromRequest().fold(
      formWithErrors => {
        val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
        BadRequest(whenDidHealthReasonHappenPage(formWithErrors, postAction))
      },
      whenHealthIssueHappened => {
        //TODO - update redirect when routing added in PRM-329
        Redirect("#").addingToSession((SessionKeys.whenHealthIssueHappened, whenHealthIssueHappened.toString))
      }
    )
  }
}