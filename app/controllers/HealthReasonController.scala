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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.{HasHospitalStayEndedForm, WasHospitalStayRequiredForm, WhenDidHealthIssueHappenForm, WhenDidHospitalStayBeginForm}
import javax.inject.Inject
import helpers.FormProviderHelper
import models.Mode
import models.appeals.HospitalStayEndInput
import models.pages._
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health._
import viewtils.{ConditionalRadioHelper, RadioOptionHelper}

class HealthReasonController @Inject()(navigation: Navigation,
                                       wasHospitalStayRequiredPage: WasHospitalStayRequiredPage,
                                       whenDidHealthReasonHappenPage: WhenDidHealthReasonHappenPage,
                                       whenDidHospitalStayBeginPage: WhenDidHospitalStayBeginPage,
                                       conditionalRadioHelper: ConditionalRadioHelper,
                                       hasTheHospitalStayEnded: HasTheHospitalStayEndedPage,
                                       errorHandler: ErrorHandler)
                                      (implicit authorise: AuthPredicate,
                                       dataRequired: DataRequiredAction,
                                       appConfig: AppConfig,
                                       mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {


  def onPageLoadForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
      WasHospitalStayRequiredForm.wasHospitalStayRequiredForm,
      SessionKeys.wasHospitalStayRequired
    )
    val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
    val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
    Ok(wasHospitalStayRequiredPage(formProvider, radioOptionsToRender, postAction))
  }

  def onSubmitForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    WasHospitalStayRequiredForm.wasHospitalStayRequiredForm.bindFromRequest.fold(
      formWithErrors => {
        val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formWithErrors)
        val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
        BadRequest(wasHospitalStayRequiredPage(formWithErrors, radioOptionsToRender, postAction))
      },
      wasStayRequiredAnswer => {
        Redirect(navigation.nextPage(WasHospitalStayRequiredPage, mode, Some(wasStayRequiredAnswer)))
          .addingToSession((SessionKeys.wasHospitalStayRequired, wasStayRequiredAnswer))
      }
    )
  }

  def onPageLoadForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm(),
      SessionKeys.whenHealthIssueHappened
    )
    val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
    Ok(whenDidHealthReasonHappenPage(formProvider, postAction))
  }

  def onSubmitForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm.bindFromRequest().fold(
      formWithErrors => {
        val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
        BadRequest(whenDidHealthReasonHappenPage(formWithErrors, postAction))
      },
      whenHealthIssueHappened => {
        Redirect(navigation.nextPage(WhenDidHealthIssueHappenPage, mode))
          .addingToSession((SessionKeys.whenHealthIssueHappened, whenHealthIssueHappened.toString))
      }
    )
  }


  def onPageLoadForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm(),
      SessionKeys.whenHealthIssueStarted
    )
    val postAction = controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(mode)
    Ok(whenDidHospitalStayBeginPage(formProvider, postAction))
  }

  def onSubmitForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(mode)
          BadRequest(whenDidHospitalStayBeginPage(formWithErrors, postAction))
        },
        whenHospitalStayBegin => {
          Redirect(navigation.nextPage(WhenDidHospitalStayBeginPage, mode))
            .addingToSession((SessionKeys.whenHealthIssueStarted, whenHospitalStayBegin.toString))
        }
      )
    }
  }

  def onPageLoadForHasHospitalStayEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    request.session.get(SessionKeys.whenHealthIssueStarted).fold(
      errorHandler.showInternalServerError
    )(
      healthIssueStartDate => {
        val parsedHealthIssueStartDate = LocalDate.parse(healthIssueStartDate)
        val formProvider: Form[HospitalStayEndInput] = FormProviderHelper.getSessionKeysAndAttemptToFillConditionalForm(
          HasHospitalStayEndedForm.hasHospitalStayEndedForm(parsedHealthIssueStartDate),
          (SessionKeys.hasHealthEventEnded, SessionKeys.whenHealthIssueEnded)
        )
        val radioOptionsToRender = conditionalRadioHelper.conditionalYesNoOptions(formProvider, "healthReason.hasTheHospitalStayEnded")
        val postAction = controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(mode)
        Ok(hasTheHospitalStayEnded(formProvider, radioOptionsToRender, postAction))
      }
    )
  }

  def onSubmitForHasHospitalStayEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    request.session.get(SessionKeys.whenHealthIssueStarted).fold(
      errorHandler.showInternalServerError
    )(
      healthIssueStartDate => {
        val parsedHealthIssueStartDate = LocalDate.parse(healthIssueStartDate)
        HasHospitalStayEndedForm.hasHospitalStayEndedForm(parsedHealthIssueStartDate).bindFromRequest().fold(
          formWithErrors => {
            val radioOptionsToRender = conditionalRadioHelper.conditionalYesNoOptions(formWithErrors, "healthReason.hasTheHospitalStayEnded")
            val postAction = controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(mode)
            BadRequest(hasTheHospitalStayEnded(formWithErrors, radioOptionsToRender, postAction))
          },
          formAnswers => {
            (formAnswers.hasStayEnded, formAnswers.stayEndDate) match {
              case (hasStayEnded, endDate) if endDate.isDefined && hasStayEnded.contains("yes") => {
                logger.debug(s"[HealthReasonController][onSubmitForHasHospitalStayEnded] - Adding answers to session:\n" +
                  s"- '${formAnswers.hasStayEnded}' under key: ${SessionKeys.hasHealthEventEnded}\n" +
                  s"- '${formAnswers.stayEndDate.get}' under key: ${SessionKeys.whenHealthIssueEnded}")
                Redirect(navigation.nextPage(DidHospitalStayEndPage, mode))
                  .addingToSession(
                    (SessionKeys.hasHealthEventEnded, formAnswers.hasStayEnded),
                    (SessionKeys.whenHealthIssueEnded, formAnswers.stayEndDate.get.toString)
                  )
              }
              case (hasStayEnded, endDate) if hasStayEnded.contains("no") && endDate.isEmpty => {
                logger.debug(s"[HealthReasonController][onSubmitForHasHospitalStayEnded]" +
                  s" - Adding answer '${formAnswers.hasStayEnded}' to session under key ${SessionKeys.hasHealthEventEnded}")
                Redirect(navigation.nextPage(DidHospitalStayEndPage, mode)).addingToSession((SessionKeys.hasHealthEventEnded, formAnswers.hasStayEnded))
              }
              case _ => {
                logger.debug("[HealthReasonController][onSubmitForHasHospitalStayEnded]- Something went wrong with 'valid' for answers")
                errorHandler.showInternalServerError
              }
            }
          }
        )
      }
    )
  }
}
