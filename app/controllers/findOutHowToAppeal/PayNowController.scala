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

import java.time.LocalDate

import config.ErrorHandler
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.PayNowService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys

import scala.concurrent.ExecutionContext

class PayNowController @Inject()(mcc: MessagesControllerComponents,
                                 payNowService: PayNowService,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext,
                                 authorise: AuthPredicate,
                                 dataRetrieval: DataRetrievalAction,
                                 val config: Configuration) extends FrontendController(mcc) with I18nSupport {

  def redirect: Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit request =>
    val vrn: String = request.vrn
    val chargeReference: String = request.answers.getAnswer[String](SessionKeys.principalChargeReference).get
    val vatAmount: BigDecimal = request.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).get
    val dueDate: LocalDate = request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).getOrElse(LocalDate.now())
    payNowService.retrieveRedirectUrl(vrn, chargeReference, vatAmount, dueDate).map {
      case Right(url) => Redirect(url)
      case Left(_) =>
        logger.warn("[PayNowController][redirect] - Unable to retrieve successful response from Pay Now service, rendering ISE")
        errorHandler.showInternalServerError(Some(request))
    }
  }
}