/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import config.ErrorHandler
import controllers.predicates.AuthPredicate
import models.{AppealData, UserRequest}
import utils.Logger.logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.AppealService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class InitialiseAppealController @Inject()(appealService: AppealService,
                                           errorHandler: ErrorHandler)(implicit mcc: MessagesControllerComponents, authorise: AuthPredicate)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(penaltyId: String): Action[AnyContent] = authorise.async {
    implicit user => {
      appealService.validatePenaltyIdForEnrolmentKey(penaltyId).map {
        _.fold(
          errorHandler.showInternalServerError
        )(
          appealData => {
            removeExistingKeysFromSessionAndRedirect(routes.AppealStartController.onPageLoad(), penaltyId, appealData)
          }
        )
      }
    }
  }

  private def removeExistingKeysFromSessionAndRedirect[A](urlToRedirectTo: Call,
                                                          penaltyId: String,
                                                          appealModel: AppealData)(implicit user: UserRequest[A]): Result = {
    logger.debug(s"[InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - Resetting appeals session: removing keys from session" +
      s" and replacing with new keys")
    Redirect(urlToRedirectTo)
      .addingToSession((SessionKeys.penaltyId, penaltyId))
      .addingToSession((SessionKeys.appealType, appealModel.`type`.toString))
      .addingToSession((SessionKeys.startDateOfPeriod, appealModel.startDate.toString))
      .addingToSession((SessionKeys.endDateOfPeriod, appealModel.endDate.toString))
      .addingToSession((SessionKeys.dueDateOfPeriod, appealModel.dueDate.toString))
      .addingToSession((SessionKeys.dateCommunicationSent, appealModel.dateCommunicationSent.toString))
  }
}
