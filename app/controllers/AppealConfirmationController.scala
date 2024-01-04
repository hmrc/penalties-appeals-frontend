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
import config.featureSwitches.{FeatureSwitching, ShowDigitalCommsMessage}
import controllers.predicates.AuthPredicate
import models.PenaltyTypeEnum
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.AppealConfirmationPage
import viewtils.ImplicitDateFormatter

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealConfirmationController @Inject()(
                                              sessionService: SessionService,
                                              appealConfirmationPage: AppealConfirmationPage
                                            )(implicit ec: ExecutionContext,
                                              val config: Configuration,
                                              appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authorise: AuthPredicate) extends FrontendController(mcc) with I18nSupport with ImplicitDateFormatter with FeatureSwitching {
  def onPageLoad(): Action[AnyContent] = authorise.async {
    implicit request => {
      request.session.get(SessionKeys.previouslySubmittedJourneyId).fold({
        logger.warn(s"[AppealConfirmationController][onPageLoad] - No journey ID was found in the session for VRN: ${request.vrn} - " +
          s"redirecting to incomplete session data page")
        Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData()))
      })(
        journeyId => {
          sessionService.getUserAnswers(journeyId).map {
            userAnswers => {
              userAnswers.fold({
                logger.warn(s"[AppealConfirmationController][onPageLoad] - No submitted user answers were found in the session for VRN: ${request.vrn} - " +
                  s"redirecting to incomplete session data page")
                Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData())
              })(
                userAnswers => {
                  val appealType = userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).get
                  val bothPenalties: String = userAnswers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).getOrElse("no")
                  val startDate: LocalDate = userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).get
                  val endDate: LocalDate = userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).get
                  val (readablePeriodStart, readablePeriodEnd) = (dateToString(startDate), dateToString(endDate))
                  val isFindOutHowToAppeal: Boolean = userAnswers.getAnswer[Boolean](SessionKeys.isFindOutHowToAppeal).contains(true)
                  val showDigitalCommsMessage: Boolean = isEnabled(ShowDigitalCommsMessage)
                  val isAgent: Boolean = request.isAgent
                  val vrn = request.vrn
                  Ok(appealConfirmationPage(readablePeriodStart, readablePeriodEnd, isFindOutHowToAppeal, showDigitalCommsMessage, appealType, bothPenalties, isAgent, vrn))
                    .removingFromSession(SessionKeys.allKeys: _*)
                }
              )
            }
          }
        })
    }
  }
}
