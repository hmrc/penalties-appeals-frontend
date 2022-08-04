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

import models.{AuthRequest, UserRequest}
import models.session.UserAnswers
import play.api.mvc.ActionTransformer
import services.SessionService
import utils.Logger.logger
import utils.SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(val sessionService: SessionService)
                                       (implicit val executionContext: ExecutionContext) extends DataRetrievalAction {
  override protected def transform[A](request: AuthRequest[A]): Future[UserRequest[A]] = {
    sessionService.getUserAnswers(request.session.get(SessionKeys.journeyId).get).map {
      case Some(storedAnswers) => {
        logger.debug(s"[DataRetrievalActionImpl][transform] - Found $storedAnswers for VRN: ${request.vrn}")
        UserRequest(request.vrn, request.active, request.arn, storedAnswers)(request)
      }
      case None => {
        logger.debug(s"[DataRetrievalActionImpl][transform] - Found no session answers for VRN: ${request.vrn}")
        UserRequest(request.vrn, request.active, request.arn, UserAnswers(request.session.get(SessionKeys.journeyId).get))(request)
      }
    }.recover {
      case e => {
        logger.error(s"[DataRetrievalActionImpl][transform] - Failed to query mongo for session data")
        throw e
      }
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[AuthRequest, UserRequest]
