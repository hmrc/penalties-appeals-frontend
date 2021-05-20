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
import utils.Logger.logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.AppealStartPage

import javax.inject.Inject
import scala.concurrent.Future

class AppealStartController @Inject()(appealStartPage: AppealStartPage)(implicit mcc: MessagesControllerComponents,
                                                                        appConfig: AppConfig,
                                                                        authorise: AuthPredicate,
                                                                        dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired).async { implicit request => {
      logger.debug(s"[AppealStartController][onPageLoad] - Session keys received: \n" +
        s"Appeal Type = ${request.session.get(SessionKeys.appealType)}, \n" +
        s"Penalty ID = ${request.session.get(SessionKeys.penaltyId)}, \n" +
        s"Start date of period = ${request.session.get(SessionKeys.startDateOfPeriod)}, \n" +
        s"End date of period = ${request.session.get(SessionKeys.endDateOfPeriod)}, \n" +
        s"Due date of period = ${request.session.get(SessionKeys.dueDateOfPeriod)}")
      Future.successful(Ok(appealStartPage()))
    }
  }
}