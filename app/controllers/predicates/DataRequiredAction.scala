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

package controllers.predicates

import config.ErrorHandler
import models.{PenaltyTypeEnum, UserRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.Logger.logger
import utils.SessionKeys

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait DataRequiredAction extends ActionRefiner[UserRequest, UserRequest]

class DataRequiredActionImpl @Inject()(errorHandler: ErrorHandler)(implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {
    (request.answers.getAnswer[String](SessionKeys.penaltyNumber),
      request.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType),
      request.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod),
      request.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod),
      request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod),
      request.answers.getAnswer[String](SessionKeys.dateCommunicationSent),
      request.session.get(SessionKeys.journeyId),
      request.session.get(SessionKeys.penaltiesHasSeenConfirmationPage)) match {
      case (Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), _) =>
        Future.successful(Right(request))
      case (None, None, None, None, None, None, None, Some(_)) =>
        logger.info("[DataRequiredAction] - User has 'penaltiesHasSeenConfirmationPage' session key in session, routing to 'You cannot go back to appeal details page'")
        Future.successful(Left(Redirect(controllers.routes.YouCannotGoBackToAppealController.onPageLoad())))
      case _ =>
        logger.error("[DataRequiredAction][refine] - Some data was missing from the session - rendering ISE")
        logger.debug(s"[DataRequiredAction][refine] - Required data from session: ${
          Seq(
            request.answers.getAnswer[String](SessionKeys.penaltyNumber),
            request.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType),
            request.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod),
            request.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod),
            request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod),
            request.answers.getAnswer[String](SessionKeys.dateCommunicationSent),
            request.session.get(SessionKeys.journeyId))
        }")
        Future.successful(Left(errorHandler.showInternalServerError(request)))
    }
  }
}
