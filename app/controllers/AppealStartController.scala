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

import java.time.LocalDate

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import javax.inject.Inject
import models.NormalMode
import models.pages.{AppealStartPage, PageMode}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.AppealStartPage

import scala.concurrent.Future

class AppealStartController @Inject()(appealStartPage: AppealStartPage)(implicit mcc: MessagesControllerComponents,
                                                                        appConfig: AppConfig,
                                                                        val config: Configuration,
                                                                        authorise: AuthPredicate,
                                                                        dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired).async { implicit request => {
      logger.debug(s"[AppealStartController][onPageLoad] - Session keys received: \n" +
        s"Appeal Type = ${request.session.get(SessionKeys.appealType)}, \n" +
        s"Penalty Number = ${request.session.get(SessionKeys.penaltyNumber)}, \n" +
        s"Start date of period = ${request.session.get(SessionKeys.startDateOfPeriod)}, \n" +
        s"End date of period = ${request.session.get(SessionKeys.endDateOfPeriod)}, \n" +
        s"Due date of period = ${request.session.get(SessionKeys.dueDateOfPeriod)}, \n" +
        s"Date communication sent of period = ${request.session.get(SessionKeys.dateCommunicationSent)}, \n")
      val isObligationAppeal = request.session.get(SessionKeys.isObligationAppeal).isDefined
      Future.successful(Ok(appealStartPage(
        isAppealLate,
        isObligationAppeal,
        PageMode(AppealStartPage, NormalMode)
      )))
    }
  }

  private def isAppealLate()(implicit request: Request[_]): Boolean = {
      val dateCommunicationSentParsedAsLocalDate = LocalDate.parse(request.session.get(SessionKeys.dateCommunicationSent).get)
      dateCommunicationSentParsedAsLocalDate.isBefore(getFeatureDate.minusDays(appConfig.daysRequiredForLateAppeal))
  }
}
