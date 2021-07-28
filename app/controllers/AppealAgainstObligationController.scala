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
import forms.OtherRelevantInformationForm
import helpers.FormProviderHelper
import models.Mode
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.obligation.OtherRelevantInformationPage

import javax.inject.Inject

class AppealAgainstObligationController @Inject()(otherRelevantInformationPage: OtherRelevantInformationPage,
                                                  navigation: Navigation)
                                                 (implicit authorise: AuthPredicate,
                                                  dataRequired: DataRequiredAction,
                                                  appConfig: AppConfig,
                                                  mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        OtherRelevantInformationForm.otherRelevantInformationForm,
        SessionKeys.otherRelevantInformation
      )
      val postAction = controllers.routes.AppealAgainstObligationController.onSubmit(mode)
      Ok(otherRelevantInformationPage(formProvider, postAction))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      OtherRelevantInformationForm.otherRelevantInformationForm.bindFromRequest.fold(
        formWithErrors => {
          val postAction = controllers.routes.AppealAgainstObligationController.onSubmit(mode)
          BadRequest(otherRelevantInformationPage(formWithErrors, postAction))
        },
        otherInformationForAppealAgainstObligation => {
          Redirect("")
            .addingToSession(SessionKeys.otherRelevantInformation -> otherInformationForAppealAgainstObligation)
        }
      )
    }
  }
}
