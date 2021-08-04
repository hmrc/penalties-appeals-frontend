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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.OtherPenaltiesForPeriodForm._
import models.NormalMode
import models.pages.OtherPenaltiesForPeriodPage

import javax.inject.Inject
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import views.html.OtherPenaltiesForPeriodPage

class OtherPenaltiesForPeriodController @Inject()(page: OtherPenaltiesForPeriodPage,
                                                  errorHandler: ErrorHandler,
                                                  navigation: Navigation)(
                                                   implicit mcc: MessagesControllerComponents,
                                                   appConfig: AppConfig,
                                                   authorise: AuthPredicate,
                                                   dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    // check session answer from 'Cancelled VAT registration' page is 'yes' when routing is added - so user cant skip to this page
    val formProvider = otherPenaltiesForPeriodForm
    Ok(page(formProvider))
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired) { implicit request =>
    otherPenaltiesForPeriodForm.bindFromRequest.fold(
      formWithErrors => {
        logger.debug(s"[OtherPenaltiesForPeriodController][onSubmit] - Form errors: ${formWithErrors.errors}")
        BadRequest(page(formWithErrors))
      },
      _ => {
        Redirect(navigation.nextPage(OtherPenaltiesForPeriodPage,NormalMode))
      }
    )
  }
}
