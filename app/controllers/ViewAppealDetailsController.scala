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

import config.featureSwitches.{FeatureSwitching, ShowViewAppealDetailsPage}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicate
import helpers.SessionAnswersHelper
import models.UserRequest
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.ViewAppealDetailsPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAppealDetailsController @Inject()(viewAppealDetailsPage: ViewAppealDetailsPage,
                                            sessionAnswersHelper: SessionAnswersHelper,
                                            sessionService: SessionService,
                                            errorHandler: ErrorHandler)
                                           (implicit mcc: MessagesControllerComponents,
                                            val config: Configuration,
                                            appConfig: AppConfig,
                                            authorise: AuthPredicate,
                                            ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad(): Action[AnyContent] = authorise.async {
    implicit request => {
      if (!isEnabled(ShowViewAppealDetailsPage)) {
        Future(NotFound(errorHandler.notFoundTemplate))
      } else {
        implicit val messages: Messages = request.messages
        request.session.get(SessionKeys.previouslySubmittedJourneyId).fold({
          logger.warn(s"[ViewAppealDetailsController][onPageLoad] - No previously submitted journey ID was found in the session for VRN: ${request.vrn} - " +
            s"redirecting to incomplete session data page")
          Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData()))
        })(
          journeyId => {
            sessionService.getUserAnswers(journeyId).flatMap {
              optUserAnswers => {
                optUserAnswers.fold[Future[Result]]({
                  logger.warn(s"[ViewAppealDetailsController][onPageLoad] - No submitted user answers were found in the session for VRN: ${request.vrn} with previously submitted journey ID $journeyId - " +
                    s"redirecting to incomplete session data page")
                  Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData()))
                })(
                  userAnswers => {
                    implicit val userRequest: UserRequest[AnyContent] = UserRequest(request.vrn, request.active, request.arn, userAnswers)(request)
                    val reasonableExcuseSpecificAnswers = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()
                    val appealMetaInformation = sessionAnswersHelper.getSubmittedAnswers(getFeatureDate)
                    if (userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined || userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).contains("other")) {
                      for {
                        fileNames <- sessionAnswersHelper.getPreviousUploadsFileNames(journeyId)
                      } yield {
                        val answersFromSession = appealMetaInformation ++ sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))
                        Ok(viewAppealDetailsPage(answersFromSession))
                      }
                    } else {
                      Future(Ok(viewAppealDetailsPage(appealMetaInformation ++ reasonableExcuseSpecificAnswers)))
                    }
                  }
                )
              }
            }
          }
        )
      }
    }
  }
}
