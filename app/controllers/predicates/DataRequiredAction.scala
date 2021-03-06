/*
 * Copyright 2022 HM Revenue & Customs
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
import models.UserRequest
import play.api.mvc.{ActionRefiner, Result}
import utils.Logger.logger
import utils.SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait DataRequiredAction extends ActionRefiner[UserRequest, UserRequest]

class DataRequiredActionImpl @Inject()(errorHandler: ErrorHandler)(implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {
    (request.session.get(SessionKeys.penaltyNumber),
      request.session.get(SessionKeys.appealType),
      request.session.get(SessionKeys.startDateOfPeriod),
      request.session.get(SessionKeys.endDateOfPeriod),
      request.session.get(SessionKeys.dueDateOfPeriod),
      request.session.get(SessionKeys.dateCommunicationSent),
      request.session.get(SessionKeys.journeyId)) match {
      case (Some(_), Some(_), Some(_), Some(_), Some(_), Some(_), Some(_)) =>
        Future.successful(Right(request))
      case _ =>
        logger.error("[DataRequiredAction][refine] - Some data was missing from the session - rendering ISE")
        logger.debug(s"[DataRequiredAction][refine] - Required data from session: ${
          Seq(
          request.session.get(SessionKeys.penaltyNumber),
          request.session.get(SessionKeys.appealType),
          request.session.get(SessionKeys.startDateOfPeriod),
          request.session.get(SessionKeys.endDateOfPeriod),
          request.session.get(SessionKeys.dueDateOfPeriod),
          request.session.get(SessionKeys.dateCommunicationSent),
          request.session.get(SessionKeys.journeyId))
        }")
        Future.successful(Left(errorHandler.showInternalServerError(request)))
    }
  }
}
