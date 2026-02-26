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

class DataRequiredActionImpl @Inject() (errorHandler: ErrorHandler)(implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {
    val penaltyNumber     = (SessionKeys.penaltyNumber, request.answers.getAnswer[String](SessionKeys.penaltyNumber))
    val appealType        = (SessionKeys.appealType, request.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType))
    val startDateOfPeriod = (SessionKeys.startDateOfPeriod, request.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod))
    val endDateOfPeriod   = (SessionKeys.endDateOfPeriod, request.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod))
    val dueDateOfPeriod   = (SessionKeys.dueDateOfPeriod, request.answers.getAnswer[LocalDate](SessionKeys.dueDateOfPeriod))
    val journeyId         = (SessionKeys.journeyId, request.session.get(SessionKeys.journeyId))
    val penaltiesHasSeenConfirmationPage =
      (SessionKeys.penaltiesHasSeenConfirmationPage, request.session.get(SessionKeys.penaltiesHasSeenConfirmationPage))

    val requiredFieldsWithKeys: Seq[(_, Option[_])] =
      Seq(penaltyNumber, appealType, startDateOfPeriod, endDateOfPeriod, dueDateOfPeriod, journeyId)

    if (penaltiesHasSeenConfirmationPage._2.isDefined && journeyId._2.isDefined) {
      logger.info(
        "[DataRequiredAction][refine] - User has 'penaltiesHasSeenConfirmationPage' session key in session, routing to 'You cannot go back to appeal details page'")
      Future.successful(Left(Redirect(controllers.routes.YouCannotGoBackToAppealController.onPageLoad())))
    } else if (requiredFieldsWithKeys.map(_._2).forall(_.isDefined)) {
      Future.successful(Right(request))
    } else {
      val missingRequiredFields: Seq[String] = requiredFieldsWithKeys.filterNot(_._2.isDefined).map(_._1.toString)
      logger.error(
        "[DataRequiredAction][refine] - Required data was missing from the session - rendering ISE. " +
          s"Missing data fields: ${missingRequiredFields.mkString(", ")}")
      Future.successful(Left(errorHandler.showInternalServerError(Some(request))(request)))
    }
  }
}
