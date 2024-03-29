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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger

import javax.inject.Inject
import utils.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

class FindOutHowToAppealStartController @Inject()(errorHandler: ErrorHandler,
                                                  appConfig: AppConfig)
                                                 (implicit val mcc: MessagesControllerComponents,
                                                  authorise: AuthPredicate,
                                                  dataRetrieval: DataRetrievalAction,
                                                  val config: Configuration,
                                                  ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def startFindOutHowToAppeal(): Action[AnyContent] = (authorise andThen dataRetrieval).async {
    implicit request => {
      val isAgent: Boolean = request.isAgent
      val isCa: Boolean = request.answers.getAnswer[Boolean](SessionKeys.isCaLpp).getOrElse(false)
      (isAgent, isCa) match {
        case (true, false) =>
          Future(Redirect(controllers.findOutHowToAppeal.routes.HowToAppealController.onPageLoad()))
        case (false, false) =>
          Future(Redirect(controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad()))
        case (_, true) =>
          Future(Redirect(controllers.findOutHowToAppeal.routes.ActionsToTakeBeforeAppealingOnlineController.onPageLoad()))
        case _ =>
          logger.debug("[FindOutHowToAppealStartController][startFindOutHowToAppeal] - CA LPP detected showing 404 (NOT_FOUND)")
          errorHandler.onClientError(request, NOT_FOUND, "")
      }
    }
  }
}