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
import forms.WhenDidPersonLeaveTheBusinessForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.WhenDidPersonLeaveTheBusinessPage
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.lossOfStaff.WhenDidThePersonLeaveBusinessPage

import javax.inject.Inject

class LossOfStaffReasonController @Inject()(whenDidThePersonLeaveBusinessPage: WhenDidThePersonLeaveBusinessPage,
                                             navigation: Navigation)
                                           (implicit authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction,
                                            appConfig: AppConfig,
                                            mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidPersonLeaveTheBusinessForm.whenDidPersonLeaveTheBusinessForm(),
        SessionKeys.whenPersonLeftTheBusiness
      )
      val postAction = controllers.routes.LossOfStaffReasonController.onSubmit(mode)
      Ok(whenDidThePersonLeaveBusinessPage(formProvider, postAction))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.LossOfStaffReasonController.onSubmit(mode)
      WhenDidPersonLeaveTheBusinessForm.whenDidPersonLeaveTheBusinessForm().bindFromRequest().fold(
        formWithErrors => BadRequest(whenDidThePersonLeaveBusinessPage(formWithErrors, postAction)),
        whenPersonLeft => {
          Redirect(navigation.nextPage(WhenDidPersonLeaveTheBusinessPage, mode, None))
            .addingToSession(SessionKeys.whenPersonLeftTheBusiness -> whenPersonLeft.toString)
        }
      )
    }
  }
}
