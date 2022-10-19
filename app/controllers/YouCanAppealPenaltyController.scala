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
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.YouCanAppealPenaltyForm.youCanAppealPenaltyForm
import helpers.FormProviderHelper
import models.pages.{PageMode, YouCanAppealThisPenaltyPage}
import models.{Mode, NormalMode}
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.obligation.YouCanAppealPenaltyPage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YouCanAppealPenaltyController @Inject()(page: YouCanAppealPenaltyPage,
                                              navigation: Navigation,
                                              sessionService: SessionService)(
                                               implicit mcc: MessagesControllerComponents,
                                               appConfig: AppConfig,
                                               authorise: AuthPredicate,
                                               dataRequired: DataRequiredAction,
                                               dataRetrieval: DataRetrievalAction,
                                               executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(YouCanAppealThisPenaltyPage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request =>
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        youCanAppealPenaltyForm,
        SessionKeys.youCanAppealThisPenalty,
        request.answers
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.YouCanAppealPenaltyController.onSubmit()
      Ok(page(formProvider, radioOptionsToRender, postAction, pageMode(NormalMode)))
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async { implicit request =>
    youCanAppealPenaltyForm.bindFromRequest.fold(
      formWithErrors => {
        logger.debug(s"[YouCanAppealPenaltyController][onSubmit] - Form errors: ${formWithErrors.errors}")
        val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formWithErrors)
        val postAction = controllers.routes.YouCanAppealPenaltyController.onSubmit()
        Future(BadRequest(page(formWithErrors, radioOptionsToRender, postAction, pageMode(NormalMode))))
      },
      answer => {
        val updatedAnswers = request.answers.setAnswer[String](SessionKeys.youCanAppealThisPenalty, answer)
        sessionService.updateAnswers(updatedAnswers).map {
          _ => Redirect(navigation.nextPage(YouCanAppealThisPenaltyPage, NormalMode, Some(answer)))
        }
      }
    )
  }
}
