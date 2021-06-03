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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import helpers.SessionAnswersHelper
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AppealService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.CheckYourAnswersPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(checkYourAnswersPage: CheckYourAnswersPage,
                                           appealService: AppealService,
                                           errorHandler: ErrorHandler)(implicit mcc: MessagesControllerComponents,
                                                                       ec: ExecutionContext,
                                                                       appConfig: AppConfig,
                                                                       authorise: AuthPredicate,
                                                                       dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {
  def onPageLoad: Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      request.session.get(SessionKeys.reasonableExcuse).fold({
        logger.error("[CheckYourAnswersController][onPageLoad] User hasn't selected reasonable excuse option - no key in session")
        errorHandler.showInternalServerError
      })(
        reasonableExcuse => {
          if (SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse(reasonableExcuse)) {
            logger.debug(s"[CheckYourAnswersController][onPageLoad] Loading check your answers page for reasonable excuse: $reasonableExcuse")
            val answersFromSession = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse)
            Ok(checkYourAnswersPage(answersFromSession))
          } else {
            logger.error(s"[CheckYourAnswersController][onPageLoad] User hasn't got all keys in session for reasonable excuse: $reasonableExcuse")
            logger.debug(s"[CheckYourAnswersController][onPageLoad] User has keys: ${request.session.data} and tried to load page with reasonable excuse: $reasonableExcuse")
            errorHandler.showInternalServerError
          }
        }
      )
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      request.session.get(SessionKeys.reasonableExcuse).fold({
        logger.error("[CheckYourAnswersController][onSubmit] No reasonable excuse selection found in session")
        Future(errorHandler.showInternalServerError)
      })(
        reasonableExcuse => {
          if (SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse(reasonableExcuse)) {
            logger.debug(s"[CheckYourAnswersController][onPageLoad] All keys are present for reasonable excuse: $reasonableExcuse")
            appealService.submitAppeal(reasonableExcuse).map {
              case true => {
                //TODO: change to confirmation page once appeal has been submitted successfully
                Redirect("")
              }
              case false => errorHandler.showInternalServerError
            }
          } else {
            logger.error(s"[CheckYourAnswersController][onSubmit] User did not have all answers for reasonable excuse: $reasonableExcuse")
            Future(errorHandler.showInternalServerError)
          }
        }
      )
    }
  }
}
