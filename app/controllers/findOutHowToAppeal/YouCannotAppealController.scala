/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.findOutHowToAppeal

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import models.NormalMode
import models.pages.{AppealByLetterKickOutPage, PageMode, YouCannotAppealPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.findOutHowToAppeal.{AppealByLetterKickOutPage, YouCannotAppealPage}
import viewtils.YouCannotAppealHelper

import javax.inject.Inject

class YouCannotAppealController @Inject()(youCannotAppealPage: YouCannotAppealPage,
                                          appealByLetterPage: AppealByLetterKickOutPage,
                                          pageHelper: YouCannotAppealHelper)
                                         (implicit mcc: MessagesControllerComponents,
                                          appConfig: AppConfig, authorise: AuthPredicate,
                                          dataRequired: DataRequiredAction,
                                          dataRetrieval: DataRetrievalAction) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => Ok(youCannotAppealPage(pageHelper.getContent, pageHelper.getHeaderAndTitle, PageMode(YouCannotAppealPage, NormalMode)))
  }

  def onPageLoadAppealByLetter(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => Ok(appealByLetterPage(PageMode(AppealByLetterKickOutPage, NormalMode)))
  }
}
