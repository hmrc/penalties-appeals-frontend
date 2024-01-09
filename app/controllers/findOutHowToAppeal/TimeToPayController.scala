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

import config.ErrorHandler
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TimeToPayService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger

import scala.concurrent.ExecutionContext

class TimeToPayController @Inject()(mcc: MessagesControllerComponents,
                                    timeToPayService: TimeToPayService,
                                    errorHandler: ErrorHandler)
                                   (implicit ec: ExecutionContext,
                                    authorise: AuthPredicate,
                                    dataRetrieval: DataRetrievalAction,
                                    val config: Configuration) extends FrontendController(mcc) with I18nSupport {

  def redirect: Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit request =>
    timeToPayService.retrieveRedirectUrl.map {
      case Right(url) => Redirect(url)
      case Left(_) =>
        logger.warn("[TimeToPayController][redirect] - Unable to retrieve successful response from TTP service, rendering ISE")
        errorHandler.showInternalServerError(request)
    }
  }
}
