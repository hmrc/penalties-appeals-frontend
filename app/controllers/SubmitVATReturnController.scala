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
import forms.WhoPlannedToSubmitVATReturnForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.WhoPlannedToSubmitVATReturnPage
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.agents.WhoPlannedToSubmitVATReturnPage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmitVATReturnController @Inject()(navigation: Navigation,
                                          whoPlannedToSubmitVATReturnPage: WhoPlannedToSubmitVATReturnPage)
                                         (implicit mcc: MessagesControllerComponents,
                                          ec: ExecutionContext,
                                          appConfig: AppConfig,
                                          authorise: AuthPredicate,
                                          dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        WhoPlannedToSubmitVATReturnForm.whoPlannedToSubmitForm,
        SessionKeys.whoPlannedToSubmitVATReturn
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formProvider)
      val postAction = controllers.routes.SubmitVATReturnController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
      Ok(whoPlannedToSubmitVATReturnPage(formProvider, radioOptionsToRender, postAction))
    }
  }

  def onSubmitForWhoPlannedToSubmitVATReturn(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      WhoPlannedToSubmitVATReturnForm.whoPlannedToSubmitForm.bindFromRequest().fold(
        formWithErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.radioOptionsForSubmitVATReturnPage(formWithErrors)
          val postAction = controllers.routes.SubmitVATReturnController.onSubmitForWhoPlannedToSubmitVATReturn(mode)
          BadRequest(whoPlannedToSubmitVATReturnPage(formWithErrors, radioOptionsToRender, postAction))
        },
        vatReturnSubmittedBy => {
          Redirect(navigation.nextPage(WhoPlannedToSubmitVATReturnPage, mode, Some(vatReturnSubmittedBy)))
            .addingToSession((SessionKeys.whoPlannedToSubmitVATReturn, vatReturnSubmittedBy))
        }
      )
    }
  }
}
