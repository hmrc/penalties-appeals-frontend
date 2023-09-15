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

import config.{AppConfig, ErrorHandler}
import config.featureSwitches.{FeatureSwitching, ShowViewAppealDetailsPage}
import controllers.predicates.AuthPredicate
import helpers.SessionAnswersHelper
import javax.inject.Inject
import models.UserRequest
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.ViewAppealDetailsPage

import scala.concurrent.{ExecutionContext, Future}

class ViewAppealDetailsController @Inject()(viewAppealDetailsPage: ViewAppealDetailsPage,
                                            sessionAnswersHelper: SessionAnswersHelper,
                                            sessionService: SessionService,
                                            errorHandler: ErrorHandler)
                                           (implicit mcc: MessagesControllerComponents,
                                            val  config: Configuration,
                                            appConfig: AppConfig,
                                            authorise: AuthPredicate,
                                            ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad(): Action[AnyContent] = authorise.async {
    implicit request => {
      if (!isEnabled(ShowViewAppealDetailsPage)) {
        Future(NotFound(errorHandler.notFoundTemplate))
      } else {
        request.session.get(SessionKeys.previouslySubmittedJourneyId).fold({
          logger.warn(s"[ViewAppealDetailsController][onPageLoad] - No previously submitted journey ID was found in the session for VRN: ${request.vrn} - " +
            s"redirecting to incomplete session data page")
          Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData()))
        })(
          journeyId => {
            sessionService.getUserAnswers(journeyId).map {
              optUserAnswers => {
                optUserAnswers.fold({
                  logger.warn(s"[ViewAppealDetailsController][onPageLoad] - No submitted user answers were found in the session for VRN: ${SessionKeys.previouslySubmittedJourneyId} - " +
                    s"redirecting to incomplete session data page")
                  Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData())
                })(
                  userAnswers => {
                    implicit val userRequest: UserRequest[AnyContent] = UserRequest(request.vrn, request.active, request.arn, userAnswers)(request)
                    userAnswers.getAnswer[String](SessionKeys.reasonableExcuse).fold({
                      if (userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined && sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty) {
                        for {
                          fileNames <- sessionAnswersHelper.getPreviousUploadsFileNames()(userRequest)
                        } yield {
                          val answersFromSession = sessionAnswersHelper.getSubmittedAnswers() ++ sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))
                          Ok(viewAppealDetailsPage(answersFromSession)(request.messages, appConfig, userRequest))
                        }
                      } else {
                        logger.error("[ViewAppealsDetailsController][onPageLoad] User hasn't selected reasonable excuse option - no key in session")
                        Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
                      }
                    })(
                      reasonableExcuse => {
                        if (sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty) {
                          for {
                            content <- sessionAnswersHelper.getContentWithExistingUploadFileNames(reasonableExcuse)
                          } yield {
                            Ok(viewAppealDetailsPage(sessionAnswersHelper.getSubmittedAnswers() ++ content)(request.messages, appConfig, userRequest))
                          }
                        } else {
                          logger.error(s"[ViewAppealsDetailsController][onPageLoad] User hasn't got all keys in session for reasonable excuse: $reasonableExcuse")
                          logger.debug(s"[ViewAppealsDetailsController][onPageLoad] User has keys: ${request.session.data} " +
                            s"and tried to load page with reasonable excuse: $reasonableExcuse")
                          Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
                        }
                      }
                    )
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
