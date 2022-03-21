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
import models.pages.{AppealStartPage, PageMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.AppealStartPage

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.Future

class AppealStartController @Inject()(appealStartPage: AppealStartPage)(implicit mcc: MessagesControllerComponents,
                                                                        appConfig: AppConfig,
                                                                        authorise: AuthPredicate,
                                                                        dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired).async { implicit request => {
      logger.debug(s"[AppealStartController][onPageLoad] - Session keys received: \n" +
        s"Appeal Type = ${request.session.get(SessionKeys.appealType)}, \n" +
        s"Penalty Number = ${request.session.get(SessionKeys.penaltyNumber)}, \n" +
        s"Start date of period = ${request.session.get(SessionKeys.startDateOfPeriod)}, \n" +
        s"End date of period = ${request.session.get(SessionKeys.endDateOfPeriod)}, \n" +
        s"Due date of period = ${request.session.get(SessionKeys.dueDateOfPeriod)}, \n" +
        s"Date communication sent of period = ${request.session.get(SessionKeys.dateCommunicationSent)}, \n")
      val dateCommunicationSentParsed = LocalDateTime.parse(request.session.get(SessionKeys.dateCommunicationSent).get)
      val isObligationAppeal = request.session.get(SessionKeys.isObligationAppeal).isDefined
      Future.successful(Ok(appealStartPage(
        dateCommunicationSentParsed.isBefore(LocalDateTime.now().minusDays(appConfig.daysRequiredForLateAppeal)),
        isObligationAppeal,
        PageMode(AppealStartPage, NormalMode)
      )))
    }
  }
}
