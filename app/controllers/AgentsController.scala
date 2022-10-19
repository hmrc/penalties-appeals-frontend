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
import forms.{WhatCausedYouToMissTheDeadlineForm, WhoPlannedToSubmitVATReturnAgentForm}
import helpers.FormProviderHelper
import models.Mode
import models.pages.{Page, PageMode, WhatCausedYouToMissTheDeadlinePage, WhoPlannedToSubmitVATReturnAgentPage}
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.agents.{WhatCausedYouToMissTheDeadlinePage, WhoPlannedToSubmitVATReturnAgentPage}
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentsController @Inject()(navigation: Navigation,
                                 whatCausedYouToMissTheDeadlinePage: WhatCausedYouToMissTheDeadlinePage,
                                 whoPlannedToSubmitVATReturnPage: WhoPlannedToSubmitVATReturnAgentPage,
                                 errorHandler: ErrorHandler,
                                 sessionService: SessionService)
                                (implicit mcc: MessagesControllerComponents,
                                 ec: ExecutionContext,
                                 appConfig: AppConfig,
                                 authorise: AuthPredicate,
                                 dataRequired: DataRequiredAction,
                                 dataRetrieval: DataRetrievalAction) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForWhatCausedYouToMissTheDeadline(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      logger.debug("[AgentsController][onPageLoadForWhatCausedYouToMissTheDeadline] - Loaded 'what caused you to miss the deadline' page as user is agent")
      val postAction = controllers.routes.AgentsController.onSubmitForWhatCausedYouToMissTheDeadline(mode)
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(WhatCausedYouToMissTheDeadlineForm.whatCausedYouToMissTheDeadlineForm,
        SessionKeys.whatCausedYouToMissTheDeadline,
        userRequest.answers
      )
      Ok(whatCausedYouToMissTheDeadlinePage(formProvider,
        RadioOptionHelper.radioOptionsForWhatCausedAgentToMissDeadline(formProvider), postAction, pageMode(WhatCausedYouToMissTheDeadlinePage, mode)))
    }
  }

  def onSubmitForWhatCausedYouToMissTheDeadline(mode: Mode): Action[AnyContent] = (authorise  andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val postAction = controllers.routes.AgentsController.onSubmitForWhatCausedYouToMissTheDeadline(mode)
      WhatCausedYouToMissTheDeadlineForm.whatCausedYouToMissTheDeadlineForm.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(whatCausedYouToMissTheDeadlinePage(formWithErrors, RadioOptionHelper.radioOptionsForWhatCausedAgentToMissDeadline(formWithErrors), postAction, pageMode(WhatCausedYouToMissTheDeadlinePage, mode))))
        },
        causeOfLateSubmission => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline, causeOfLateSubmission)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(WhatCausedYouToMissTheDeadlinePage, mode))
          }
        }
      )
    }
  }

  def onPageLoadForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      logger.debug("[AgentsController][onPageLoadForWhoPlannedToSubmitVATReturn] - Loaded 'Who Planned To Submit VAT Return' page as user is agent")
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        WhoPlannedToSubmitVATReturnAgentForm.whoPlannedToSubmitVATReturnForm,
        SessionKeys.whoPlannedToSubmitVATReturn,
        userRequest.answers
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formProvider)
      val postAction = controllers.routes.AgentsController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
      Ok(whoPlannedToSubmitVATReturnPage(formProvider, radioOptionsToRender, postAction, pageMode(WhoPlannedToSubmitVATReturnAgentPage, mode)))
    }
  }

  def onSubmitForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise  andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      WhoPlannedToSubmitVATReturnAgentForm.whoPlannedToSubmitVATReturnForm.bindFromRequest().fold(
        formWithErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formWithErrors)
          val postAction = controllers.routes.AgentsController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
          Future(BadRequest(whoPlannedToSubmitVATReturnPage(formWithErrors, radioOptionsToRender, postAction, pageMode(WhoPlannedToSubmitVATReturnAgentPage, mode))))
        },
        whoPlannedToSubmitReturn => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn, whoPlannedToSubmitReturn)
            whoPlannedToSubmitReturn.toLowerCase match {
            case "agent" | "client" =>
              sessionService.updateAnswers(updatedAnswers).map {
                _ => Redirect(navigation.nextPage(WhoPlannedToSubmitVATReturnAgentPage, mode, Some(whoPlannedToSubmitReturn)))
              }
            case _ =>
              logger.debug("[AgentsController][onSubmitForWhoPlannedToSubmitVATReturn]- Something went wrong with 'vatReturnSubmittedBy'")
              Future(errorHandler.showInternalServerError)
          }
        }
      )
    }
  }
}
