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
import forms.WhenDidFireOrFloodHappenForm
import helpers.FormProviderHelper
import javax.inject.Inject
import models.Mode
import models.pages.WhenDidFireOrFloodHappenPage
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.fireOrFlood.WhenDidFireOrFloodHappenPage

class FireOrFloodReasonController @Inject()(fireOrFloodPage: WhenDidFireOrFloodHappenPage,
                                            navigation: Navigation)
                                           (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      appConfig: AppConfig,
                                      mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidFireOrFloodHappenForm.whenFireOrFloodHappenedForm(),
      SessionKeys.dateOfFireOrFlood
    )
    val postAction = controllers.routes.FireOrFloodReasonController.onSubmit(mode)
    Ok(fireOrFloodPage(formProvider, postAction))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    val postAction = controllers.routes.FireOrFloodReasonController.onSubmit(mode)
    WhenDidFireOrFloodHappenForm.whenFireOrFloodHappenedForm().bindFromRequest().fold(
      formWithErrors => {
        BadRequest(fireOrFloodPage(formWithErrors, postAction))
      },
      dateOfFireOrFlood => {
        logger.debug(s"[FireOrFloodController][onSubmit] - Adding '$dateOfFireOrFlood' to session under key: ${SessionKeys.dateOfFireOrFlood}")
        Redirect(navigation.nextPage(WhenDidFireOrFloodHappenPage, mode))
          .addingToSession((SessionKeys.dateOfFireOrFlood, dateOfFireOrFlood.toString))
      }
    )
  }

}
