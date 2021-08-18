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
import forms.{WhoPlannedToSubmitVATReturnAgentForm, WhyReturnWasSubmittedLateAgentForm}
import helpers.FormProviderHelper
import models.Mode
import models.pages.{ReasonableExcuseSelectionPage, WhoPlannedToSubmitVATReturnAgentPage}
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.agents.{WhoPlannedToSubmitVATReturnAgentPage, WhyWasTheReturnSubmittedLateAgentPage}
import viewtils.RadioOptionHelper

import javax.inject.Inject

class AgentsController @Inject()(navigation: Navigation,
                                          whyWasTheReturnSubmittedLatePage: WhyWasTheReturnSubmittedLateAgentPage,
                                          whoPlannedToSubmitVATReturnPage: WhoPlannedToSubmitVATReturnAgentPage,
                                          errorHandler: ErrorHandler)
                                         (implicit mcc: MessagesControllerComponents,
                                          appConfig: AppConfig,
                                          authorise: AuthPredicate,
                                          dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {
def onPageLoadForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      logger.debug("[AgentsController][onPageLoadForWhyReturnSubmittedLate] - Loaded 'why was return submitted late' page as user is agent")
      val postAction = controllers.routes.AgentsController.onSubmitForWhyReturnSubmittedLate(mode)
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(WhyReturnWasSubmittedLateAgentForm.whyReturnWasSubmittedLateAgentForm,
        SessionKeys.causeOfLateSubmissionAgent)
      Ok(whyWasTheReturnSubmittedLatePage(formProvider, RadioOptionHelper.radioOptionsForWhyReturnSubmittedLateAgent(formProvider), postAction))
    }
  }

  def onSubmitForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.AgentsController.onSubmitForWhyReturnSubmittedLate(mode)
      WhyReturnWasSubmittedLateAgentForm.whyReturnWasSubmittedLateAgentForm.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(whyWasTheReturnSubmittedLatePage(formWithErrors, RadioOptionHelper.radioOptionsForWhyReturnSubmittedLateAgent(formWithErrors), postAction))
        },
        causeOfLateSubmission => {
          Redirect(navigation.nextPage(ReasonableExcuseSelectionPage,mode))
            .addingToSession(SessionKeys.causeOfLateSubmissionAgent -> causeOfLateSubmission)
        }
      )
    }
  }

  def onPageLoadForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      logger.debug("[AgentsController][onPageLoadForWhoPlannedToSubmitVATReturn] - Loaded 'Who Planned ToSubmit VAT Return' page as user is agent")
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        WhoPlannedToSubmitVATReturnAgentForm.whoPlannedToSubmitVATReturnForm,
        SessionKeys.whoPlannedToSubmitVATReturn
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formProvider)
      val postAction = controllers.routes.AgentsController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
      Ok(whoPlannedToSubmitVATReturnPage(formProvider, radioOptionsToRender, postAction))
    }
  }

  def onSubmitForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      WhoPlannedToSubmitVATReturnAgentForm.whoPlannedToSubmitVATReturnForm.bindFromRequest().fold(
        formWithErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formWithErrors)
          val postAction = controllers.routes.AgentsController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
          BadRequest(whoPlannedToSubmitVATReturnPage(formWithErrors, radioOptionsToRender, postAction))
        },
        vatReturnSubmittedBy => {
          (vatReturnSubmittedBy.toLowerCase) match {
            case "agent" => {
              Redirect(navigation.nextPage(WhoPlannedToSubmitVATReturnAgentPage, mode, Some(vatReturnSubmittedBy)))
                .addingToSession((SessionKeys.whoPlannedToSubmitVATReturn, vatReturnSubmittedBy))
            }
            case "client" => {
              Redirect(navigation.nextPage(WhoPlannedToSubmitVATReturnAgentPage, mode, Some(vatReturnSubmittedBy)))
                .addingToSession((SessionKeys.whoPlannedToSubmitVATReturn, vatReturnSubmittedBy))
            }
            case _ => {
              logger.debug("[AgentsController][onSubmitForWhoPlannedToSubmitVATReturn]- Something went wrong with 'vatReturnSubmittedBy'")
              errorHandler.showInternalServerError
            }
          }
        }
      )
    }
  }
}
