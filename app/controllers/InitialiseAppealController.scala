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

package controllers

import config.ErrorHandler
import controllers.predicates.AuthPredicate
import models.UserRequest
import models.v2.AppealInformation
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.AppealService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class InitialiseAppealController @Inject()(appealService: AppealService,
                                           errorHandler: ErrorHandler)(implicit mcc: MessagesControllerComponents, authorise: AuthPredicate)
  extends FrontendController(mcc) with I18nSupport {

   def onPageLoad(penaltyId: String, isLPP: Boolean,isAdditional:Boolean): Action[AnyContent] = authorise.async {
    implicit user => {
      appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP,isAdditional).map {
        _.fold(
          errorHandler.showInternalServerError
        )(
          appealData => {
            removeExistingKeysFromSessionAndRedirect(routes.AppealStartController.onPageLoad(), penaltyId, appealData, isAppealAgainstObligation = false)
          }
        )
      }
    }
  }

 def onPageLoadForObligation(penaltyId: String, isLPP: Boolean,isAdditional:Boolean): Action[AnyContent] = authorise.async {
    implicit user => {
      appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP,isAdditional).map {
        _.fold(
          errorHandler.showInternalServerError
        )(
          appealData => {
            removeExistingKeysFromSessionAndRedirect(
              routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration(), penaltyId, appealData, isAppealAgainstObligation = true)
          }
        )
      }
    }
  }

  private def removeExistingKeysFromSessionAndRedirect[A](urlToRedirectTo: Call,
                                                          penaltyNumber: String,
                                                          appealModel: AppealInformation[_],
                                                          isAppealAgainstObligation: Boolean)(implicit user: UserRequest[A]): Result = {
    logger.debug(s"[InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - Resetting appeals session: removing keys from session" +
      s" and replacing with new keys")
    val journeyId: String = UUID.randomUUID().toString
    logger.debug(s"InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - Setting journeyId to: $journeyId")
    Redirect(urlToRedirectTo)
      .removingFromSession(SessionKeys.allKeys: _*)
      .addingToSession((SessionKeys.penaltyNumber, penaltyNumber))
      .addingToSession((SessionKeys.appealType, appealModel.`type`.toString))
      .addingToSession((SessionKeys.startDateOfPeriod, appealModel.startDate.toString))
      .addingToSession((SessionKeys.endDateOfPeriod, appealModel.endDate.toString))
      .addingToSession((SessionKeys.dueDateOfPeriod, appealModel.dueDate.toString))
      .addingToSession((SessionKeys.dateCommunicationSent, appealModel.dateCommunicationSent.toString))
      .addingToSession((SessionKeys.journeyId, journeyId))
      .addingToSession(if(isAppealAgainstObligation) (SessionKeys.isObligationAppeal, isAppealAgainstObligation.toString) else ("", ""))  }

}
