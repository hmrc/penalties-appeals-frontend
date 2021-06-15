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
import forms.WhenDidTechnologyIssuesBeginForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.WhenDidTechnologyIssuesBeginPage
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.technicalIssues.WhenDidTechnologyIssuesBeginPage

import javax.inject.Inject

class TechnicalIssuesReasonController @Inject()(whenDidTechnologyIssuesBegin: WhenDidTechnologyIssuesBeginPage,
                                                navigation: Navigation)
                                               (implicit authorise: AuthPredicate,
                                                dataRequired: DataRequiredAction,
                                                appConfig: AppConfig,
                                                mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm,
        SessionKeys.whenDidTechnologyIssuesBegin
      )
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      Ok(whenDidTechnologyIssuesBegin(formProvider, postAction))
    }
  }

  def onSubmitForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm().bindFromRequest().fold(
        formWithErrors => BadRequest(whenDidTechnologyIssuesBegin(formWithErrors, postAction)),
        dateTechnicalIssuesBegan => {
          Redirect(navigation.nextPage(WhenDidTechnologyIssuesBeginPage, mode))
            .addingToSession(SessionKeys.whenDidTechnologyIssuesBegin -> dateTechnicalIssuesBegan.toString)
        }
      )
    }
  }
}
