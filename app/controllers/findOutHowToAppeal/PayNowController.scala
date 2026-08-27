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
import models.UserRequest
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PayNowService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

object PayNowController {
  private final case class PayNowJourneyData(vrn: String, chargeReference: String, vatAmount: BigDecimal, dueDate: LocalDate)

  sealed trait PayNowError {
    def message: String
  }

  final case class MissingSessionValue(sessionKey: String) extends PayNowError {
    override val message: String =
      s"[PayNowController][redirect] - Missing required session value for $sessionKey"
  }
}

class PayNowController @Inject()(mcc: MessagesControllerComponents,
                                 payNowService: PayNowService,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext,
                                 authorise: AuthPredicate,
                                 dataRetrieval: DataRetrievalAction,
                                 val config: Configuration) extends FrontendController(mcc) with I18nSupport {

  def redirect: Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit request =>
    payNowJourneyData match {
      case Right(data) =>
        payNowService.retrieveRedirectUrl(data.vrn, data.chargeReference, data.vatAmount, data.dueDate).map {
          case Right(url) => Redirect(url)
          case Left(_) =>
            logger.warn("[PayNowController][redirect] - Unable to retrieve successful response from Pay Now service, rendering ISE")
            errorHandler.showInternalServerError(Some(request))
        }
      case Left(error) =>
        renderMissingSessionValue(error)
    }
  }

  private def payNowJourneyData(implicit request: UserRequest[_]): Either[PayNowController.PayNowError, PayNowController.PayNowJourneyData] = {
    for {
      chargeReference <- request.answers
        .getAnswer[String](SessionKeys.principalChargeReference)
        .toRight(PayNowController.MissingSessionValue(SessionKeys.principalChargeReference))
      vatAmount <- request.answers
        .getAnswer[BigDecimal](SessionKeys.vatAmount)
        .toRight(PayNowController.MissingSessionValue(SessionKeys.vatAmount))
      dueDate = request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).getOrElse(LocalDate.now())
    } yield PayNowController.PayNowJourneyData(request.vrn, chargeReference, vatAmount, dueDate)
  }

  private def renderMissingSessionValue(error: PayNowController.PayNowError)
                                       (implicit request: UserRequest[_]): Future[Result] = {
    logger.warn(error.message)
    Future.successful(errorHandler.showInternalServerError(Some(request)))
  }
}
