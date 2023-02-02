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

package controllers

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import models.pages.{AppealStartPage, PageMode}
import models.{NormalMode, PenaltyTypeEnum, UserRequest}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.AppealStartPage

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class AppealStartController @Inject()(appealStartPage: AppealStartPage)(implicit mcc: MessagesControllerComponents,
                                                                        appConfig: AppConfig,
                                                                        val config: Configuration,
                                                                        authorise: AuthPredicate,
                                                                        dataRequired: DataRequiredAction,
                                                                        dataRetrieval: DataRetrievalAction) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      logger.debug(s"[AppealStartController][onPageLoad] - Session keys received: \n" +
        s"Appeal Type = ${userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType)}, \n" +
        s"Penalty Number = ${userRequest.answers.getAnswer[String](SessionKeys.penaltyNumber)}, \n" +
        s"Start date of period = ${userRequest.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod)}, \n" +
        s"End date of period = ${userRequest.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod)}, \n" +
        s"Due date of period = ${userRequest.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod)}, \n" +
        s"Is obligation = ${userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal)}, \n" +
        s"Date communication sent of period = ${userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent)}, \n")
      val isObligationAppeal = userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined
      Future.successful(Ok(appealStartPage(
        isAppealLate(),
        isObligationAppeal,
        PageMode(AppealStartPage, NormalMode)
      )))
    }
  }

  private def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
      val dateCommunicationSentParsedAsLocalDate = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).get
      dateCommunicationSentParsedAsLocalDate.isBefore(getFeatureDate.minusDays(appConfig.daysRequiredForLateAppeal))
  }
}
