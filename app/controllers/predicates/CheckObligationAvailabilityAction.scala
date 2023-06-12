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

import config.AppConfig
import config.featureSwitches.ShowFullAppealAgainstTheObligation
import models.UserRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.Logger.logger
import utils.SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait CheckObligationAvailabilityAction extends ActionRefiner[UserRequest, UserRequest]

class CheckObligationAvailabilityActionImpl @Inject()(appConfig: AppConfig)
                                                     (implicit val executionContext: ExecutionContext) extends CheckObligationAvailabilityAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {
    if(request.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).contains(true)) {
      if(appConfig.isEnabled(ShowFullAppealAgainstTheObligation)) {
        Future.successful(Right(request))
      } else {
        logger.info(s"[CheckObligationAvailabilityAction][refine] - User attempted to access a page but the obligation journey is disabled." +
          s" Redirecting to appeal by letter page.")
        Future.successful(Left(Redirect(controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter())))
      }
    } else {
      Future.successful(Right(request))
    }
  }
}
