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
import forms.WhenDidHospitalStayBeginForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.WhenDidHospitalStayBeginPage
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health.WhenDidHospitalStayBeginPage

import java.time.LocalDate
import javax.inject.Inject

class HospitalStayBeginController @Inject()(navigation: Navigation,
                                            whenDidHospitalStayBeginPage: WhenDidHospitalStayBeginPage)
                                           (implicit authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction,
                                            appConfig: AppConfig,
                                            mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm(),
      SessionKeys.whenHospitalStayBegin
    )
    val postAction = controllers.routes.HospitalStayBeginController.onSubmitForWhenDidHospitalStayBegin(mode)
    Ok(whenDidHospitalStayBeginPage(formProvider, postAction))
  }

  def onSubmitForWhenDidHospitalStayBegin(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      WhenDidHospitalStayBeginForm.whenHospitalStayBeginForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.HospitalStayBeginController.onSubmitForWhenDidHospitalStayBegin(mode)
          BadRequest(whenDidHospitalStayBeginPage(formWithErrors, postAction))
        },
        whenHospitalStayBegin => {
          Redirect(navigation.nextPage(WhenDidHospitalStayBeginPage, mode))
            .addingToSession((SessionKeys.whenHospitalStayBegin, whenHospitalStayBegin.toString))
        }
      )
    }
  }
}
