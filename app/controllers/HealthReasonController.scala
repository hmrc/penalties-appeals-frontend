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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.{HasHospitalStayEndedForm, WasHospitalStayRequiredForm, WhenDidHealthIssueHappenForm, WhenDidHospitalStayBeginForm}
import helpers.FormProviderHelper
import models.Mode
import models.appeals.HospitalStayEndInput
import models.pages._
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health._
import viewtils.{ConditionalRadioHelper, RadioOptionHelper}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HealthReasonController @Inject()(navigation: Navigation,
                                       wasHospitalStayRequiredPage: WasHospitalStayRequiredPage,
                                       whenDidHealthReasonHappenPage: WhenDidHealthReasonHappenPage,
                                       whenDidHospitalStayBeginPage: WhenDidHospitalStayBeginPage,
                                       conditionalRadioHelper: ConditionalRadioHelper,
                                       hasTheHospitalStayEnded: HasTheHospitalStayEndedPage,
                                       errorHandler: ErrorHandler,
                                       sessionService: SessionService)
                                      (implicit authorise: AuthPredicate,
                                       dataRequired: DataRequiredAction,
                                       dataRetrieval: DataRetrievalAction,
                                       appConfig: AppConfig,
                                       ec: ExecutionContext,
                                       mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        WasHospitalStayRequiredForm.wasHospitalStayRequiredForm,
        SessionKeys.wasHospitalStayRequired,
        userRequest.answers
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
      Ok(wasHospitalStayRequiredPage(formProvider, radioOptionsToRender, postAction, pageMode(WasHospitalStayRequiredPage, mode)))
    }
  }

  def onSubmitForWasHospitalStayRequired(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest =>
      WasHospitalStayRequiredForm.wasHospitalStayRequiredForm.bindFromRequest.fold(
        formWithErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formWithErrors)
          val postAction = controllers.routes.HealthReasonController.onSubmitForWasHospitalStayRequired(mode)
          Future(BadRequest(wasHospitalStayRequiredPage(formWithErrors, radioOptionsToRender, postAction, pageMode(WasHospitalStayRequiredPage, mode))))
        },
        wasStayRequiredAnswer => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.wasHospitalStayRequired, wasStayRequiredAnswer)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(WasHospitalStayRequiredPage, mode, Some(wasStayRequiredAnswer)))
          }
        }
      )
  }

  def onPageLoadForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm(),
        SessionKeys.whenHealthIssueHappened,
        userRequest.answers
      )
      val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
      Ok(whenDidHealthReasonHappenPage(formProvider, postAction, pageMode(WhenDidHealthIssueHappenPage, mode)))
    }
  }

  def onSubmitForWhenHealthReasonHappened(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(mode)
          Future(BadRequest(whenDidHealthReasonHappenPage(formWithErrors, postAction, pageMode(WhenDidHealthIssueHappenPage, mode))))
        },
        whenHealthIssueHappened => {
          val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenHealthIssueHappened, whenHealthIssueHappened)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhenDidHealthIssueHappenPage, mode)))
        }
      )
    }
  }


  def onPageLoadForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm(),
        SessionKeys.whenHealthIssueStarted,
        userRequest.answers
      )
      val postAction = controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(mode)
      Ok(whenDidHospitalStayBeginPage(formProvider, postAction, pageMode(WhenDidHospitalStayBeginPage, mode)))
    }
  }

  def onSubmitForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.HealthReasonController.onSubmitForWhenDidHospitalStayBegin(mode)
          Future(BadRequest(whenDidHospitalStayBeginPage(formWithErrors, postAction, pageMode(WhenDidHospitalStayBeginPage, mode))))
        },
        whenHospitalStayBegin => {
          val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenHealthIssueStarted, whenHospitalStayBegin)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhenDidHospitalStayBeginPage, mode)))
        }
      )
    }
  }

  def onPageLoadForHasHospitalStayEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest =>
      userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted).fold(
        errorHandler.showInternalServerError
      )(
        healthIssueStartDate => {
          val formProvider: Form[HospitalStayEndInput] = FormProviderHelper.getSessionKeysAndAttemptToFillConditionalForm(
            HasHospitalStayEndedForm.hasHospitalStayEndedForm(healthIssueStartDate),
            (SessionKeys.hasHealthEventEnded, SessionKeys.whenHealthIssueEnded),
            userRequest.answers
          )
          val radioOptionsToRender = conditionalRadioHelper.conditionalYesNoOptions(formProvider, "healthReason.hasTheHospitalStayEnded")
          val postAction = controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(mode)
          Ok(hasTheHospitalStayEnded(formProvider, radioOptionsToRender, postAction, pageMode(DidHospitalStayEndPage, mode)))
        }
      )
  }

  def onSubmitForHasHospitalStayEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      userRequest.answers.getAnswer[LocalDate](SessionKeys.whenHealthIssueStarted).fold(
        Future(errorHandler.showInternalServerError)
      )(
        healthIssueStartDate => {
          HasHospitalStayEndedForm.hasHospitalStayEndedForm(healthIssueStartDate).bindFromRequest().fold(
            formWithErrors => {
              val radioOptionsToRender = conditionalRadioHelper.conditionalYesNoOptions(formWithErrors, "healthReason.hasTheHospitalStayEnded")
              val postAction = controllers.routes.HealthReasonController.onSubmitForHasHospitalStayEnded(mode)
              Future(BadRequest(hasTheHospitalStayEnded(formWithErrors, radioOptionsToRender, postAction, pageMode(DidHospitalStayEndPage, mode))))
            },
            formAnswers => {
              (formAnswers.hasStayEnded, formAnswers.stayEndDate) match {
                case (hasStayEnded, endDate) if endDate.isDefined && hasStayEnded.contains("yes") =>
                  logger.debug(s"[HealthReasonController][onSubmitForHasHospitalStayEnded] - Adding answers to session:\n" +
                    s"- '${formAnswers.hasStayEnded}' under key: ${SessionKeys.hasHealthEventEnded}\n" +
                    s"- '${formAnswers.stayEndDate.get}' under key: ${SessionKeys.whenHealthIssueEnded}")
                  val updatedAnswers = userRequest.answers
                    .setAnswer[String](SessionKeys.hasHealthEventEnded, formAnswers.hasStayEnded)
                    .setAnswer[LocalDate](SessionKeys.whenHealthIssueEnded, formAnswers.stayEndDate.get)
                  sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(DidHospitalStayEndPage, mode)))
                case (hasStayEnded, endDate) if hasStayEnded.contains("no") && endDate.isEmpty =>
                  logger.debug(s"[HealthReasonController][onSubmitForHasHospitalStayEnded]" +
                    s" - Adding answer '${formAnswers.hasStayEnded}' to session under key ${SessionKeys.hasHealthEventEnded}")
                  val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.hasHealthEventEnded, formAnswers.hasStayEnded)
                  sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(DidHospitalStayEndPage, mode)))
                case _ =>
                  logger.debug("[HealthReasonController][onSubmitForHasHospitalStayEnded]- Something went wrong with 'valid' for answers")
                  Future(errorHandler.showInternalServerError)
              }
            }
          )
        }
      )
    }
  }
}
