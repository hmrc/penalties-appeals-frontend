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
import forms.WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm
import forms.{MakingALateAppealForm, WhyReturnSubmittedLateForm}
import helpers.FormProviderHelper
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.other.WhyReturnSubmittedLatePage

import javax.inject.Inject

class OtherReasonController @Inject()(whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage)
                                     (implicit authorise: AuthPredicate,
                                       dataRequired: DataRequiredAction,
                                       appConfig: AppConfig,
                                       mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(whyReturnSubmittedLateForm, SessionKeys.whyReturnSubmittedLate)
      val postAction = controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode)
      Ok(whyReturnSubmittedLatePage(formProvider,postAction))
    }
  }

  def onSubmitForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      whyReturnSubmittedLateForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode)
          BadRequest(whyReturnSubmittedLatePage(formWithErrors,postAction))
        },
        whyReturnSubmittedLateReason => {
          Redirect(routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(mode: Mode))
            .addingToSession(SessionKeys.whyReturnSubmittedLate -> whyReturnSubmittedLateReason)
        }
      )
    }
  }
}
