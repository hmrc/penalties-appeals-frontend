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
import forms.WhenDidThePersonDieForm
import helpers.FormProviderHelper
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.bereavement.WhenDidThePersonDiePage

import javax.inject.Inject

class BereavementReasonController @Inject()(whenDidThePersonDiePage: WhenDidThePersonDiePage)
                                           (implicit authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction,
                                            appConfig: AppConfig,
                                            mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenThePersonDied(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidThePersonDieForm.whenDidThePersonDieForm(),
        SessionKeys.whenDidThePersonDie
      )
      val postAction = controllers.routes.BereavementReasonController.onSubmitForWhenThePersonDied(mode)
      Ok(whenDidThePersonDiePage(formProvider, postAction))
    }
  }

  def onSubmitForWhenThePersonDied(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.BereavementReasonController.onSubmitForWhenThePersonDied(mode)
      WhenDidThePersonDieForm.whenDidThePersonDieForm().bindFromRequest().fold(
        formWithErrors => BadRequest(whenDidThePersonDiePage(formWithErrors, postAction)),
        whenPersonDied => {
          //TODO: update redirect as per navigation
          Redirect("#")
            .addingToSession(SessionKeys.whenDidThePersonDie -> whenPersonDied.toString)
        }
      )
    }
  }
}