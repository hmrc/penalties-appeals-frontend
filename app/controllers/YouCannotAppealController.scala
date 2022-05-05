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
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import models.NormalMode
import models.pages.{PageMode, YouCannotAppealPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.obligation.YouCannotAppealPage

import javax.inject.Inject

class YouCannotAppealController @Inject()(youCannotAppealPage: YouCannotAppealPage)(implicit mcc: MessagesControllerComponents,
                                                                                    appConfig: AppConfig,
                                                                                    authorise: AuthPredicate,
                                                                                    dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => Ok(youCannotAppealPage(PageMode(YouCannotAppealPage, NormalMode)))
  }
}
