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
import models.UserRequest
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PayNowService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

object PayNowController {
  sealed trait PayNowError {
    def message: String
  }

  final case class MissingSessionAnswer(sessionKey: String) extends PayNowError {
    val message: String = s"[PayNowController][redirect] - Missing required session answer: $sessionKey"
  }

  private final case class PayNowJourneyData(chargeReference: String, vatAmount: BigDecimal, dueDate: LocalDate)
}

class PayNowController @Inject()(mcc: MessagesControllerComponents,
                                 payNowService: PayNowService,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext,
                                 authorise: AuthPredicate,
                                 dataRetrieval: DataRetrievalAction,
                                 val config: Configuration) extends FrontendController(mcc) with I18nSupport {
  import PayNowController._

  def redirect: Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit request =>
    val vrn: String = request.vrn
    payNowJourneyData match {
      case Left(error) => Future.successful(renderError(error))
      case Right(journeyData) =>
        payNowService.retrieveRedirectUrl(vrn, journeyData.chargeReference, journeyData.vatAmount, journeyData.dueDate).map {
          case Right(url) => Redirect(url)
          case Left(_) =>
            logger.warn("[PayNowController][redirect] - Unable to retrieve successful response from Pay Now service, rendering ISE")
            errorHandler.showInternalServerError(Some(request))
        }
    }
  }

  private def payNowJourneyData(implicit request: UserRequest[_]): Either[PayNowError, PayNowJourneyData] = {
    for {
      chargeReference <- request.answers
        .getAnswer[String](SessionKeys.principalChargeReference)
        .toRight(MissingSessionAnswer(SessionKeys.principalChargeReference))
      vatAmount <- request.answers
        .getAnswer[BigDecimal](SessionKeys.vatAmount)
        .toRight(MissingSessionAnswer(SessionKeys.vatAmount))
      dueDate = request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod).getOrElse(LocalDate.now())
    } yield PayNowJourneyData(chargeReference, vatAmount, dueDate)
  }

  private def renderError(error: PayNowError)(implicit request: UserRequest[_]): Result = {
    logger.error(error.message)
    errorHandler.showInternalServerError(Some(request))
  }
}
