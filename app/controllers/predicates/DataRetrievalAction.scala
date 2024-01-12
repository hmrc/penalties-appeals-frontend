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
import models.session.UserAnswers
import models.{AuthRequest, UserRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.SessionService
import utils.Logger.logger
import utils.SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(sessionService: SessionService,
                                        errorHandler: ErrorHandler)
                                       (implicit val executionContext: ExecutionContext) extends DataRetrievalAction {
  override protected def refine[A](request: AuthRequest[A]): Future[Either[Result, UserRequest[A]]] = {
    request.session.get(SessionKeys.journeyId).fold[Future[Either[Result, UserRequest[A]]]]({
      logger.warn(s"[DataRetrievalAction][refine] - No journey ID was found in the session for VRN: ${request.vrn} - " +
        s"redirecting to incomplete session data page")
      Future(Left(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData())))
    })(
      journeyId => {
        sessionService.getUserAnswers(journeyId).map {
          case Some(storedAnswers) => {
            logger.debug(s"[DataRetrievalActionImpl][refine] - Found $storedAnswers for VRN: ${request.vrn}")
            Right(UserRequest(request.vrn, request.active, request.arn, storedAnswers)(request))
          }
          case None => {
            logger.debug(s"[DataRetrievalActionImpl][refine] - Found no session answers for VRN: ${request.vrn}")
            Right(UserRequest(request.vrn, request.active, request.arn, UserAnswers(journeyId))(request))
          }
        }.recover {
          case e => {
            logger.error(s"[DataRetrievalActionImpl][refine] - Failed to query mongo for session data with message: ${e.getMessage}")
            Left(errorHandler.showInternalServerError()(request))
          }
        }
      }
    )
  }
}

trait DataRetrievalAction extends ActionRefiner[AuthRequest, UserRequest]
