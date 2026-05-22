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

package controllers

import config.featureSwitches.FeatureSwitching
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import helpers.{IsLateAppealHelper, SessionAnswersHelper}
import models.pages.{CheckYourAnswersPage, PageMode}
import models.{Mode, NormalMode, UserRequest}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.AppealService
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.CheckYourAnswersPage
import viewtils.ImplicitDateFormatter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(checkYourAnswersPage: CheckYourAnswersPage,
                                           appealService: AppealService,
                                           errorHandler: ErrorHandler,
                                           sessionAnswersHelper: SessionAnswersHelper,
                                           isLateAppealHelper: IsLateAppealHelper)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           val config: Configuration,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRetrieval: DataRetrievalAction,
                                           dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport with ImplicitDateFormatter with FeatureSwitching {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(CheckYourAnswersPage, mode)

  def onPageLoad: Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest =>
      if (isLateAppealHelper.isAppealLate() && userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).isEmpty) {
        logger.warn("[CheckYourAnswersController][onPageLoad] User tried skipping late appeal page, redirecting back to late appeal page")
        Future.successful(Redirect(controllers.routes.MakingALateAppealController.onPageLoad()))
      } else {
        userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse) match {
          case None =>
            logger.error("[CheckYourAnswersController][onPageLoad] User hasn't selected reasonable excuse option - no key in session")
            Future.successful(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))

          case Some(reasonableExcuse) if sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty =>
            logger.debug(s"[CheckYourAnswersController][onPageLoad] Loading check your answers page")
            sessionAnswersHelper.getContentWithExistingUploadFileNames(reasonableExcuse).map { content =>
              Ok(checkYourAnswersPage(content, pageMode(NormalMode))).removingFromSession(SessionKeys.originatingChangePage)
            }

          case Some(reasonableExcuse) =>
            logger.error(s"[CheckYourAnswersController][onPageLoad] User hasn't got all keys in session for reasonable excuse: $reasonableExcuse")
            logger.debug(s"[CheckYourAnswersController][onPageLoad] User has keys: ${userRequest.session.data} " +
              s"and tried to load page with reasonable excuse: $reasonableExcuse")
            Future.successful(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest =>
      userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse) match {
        case None =>
          logger.error("[CheckYourAnswersController][onSubmit] No reasonable excuse selection found in session")
          Future.successful(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))

        case Some(reasonableExcuse) if sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse(reasonableExcuse) =>
          logger.debug(s"[CheckYourAnswersController][onPageLoad] All keys are present for reasonable excuse: $reasonableExcuse")
          handleAppealSubmission(reasonableExcuse)

        case Some(reasonableExcuse) =>
          logger.error(s"[CheckYourAnswersController][onSubmit] User did not have all answers for reasonable excuse: $reasonableExcuse")
          Future.successful(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
      }
  }

  private def handleAppealSubmission(reasonableExcuse: String)(implicit userRequest: UserRequest[_]): Future[Result] = {
    appealService.submitAppeal(reasonableExcuse).map {
      case Left(SERVICE_UNAVAILABLE) =>
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad())
      case Left(INTERNAL_SERVER_ERROR | BAD_REQUEST | UNPROCESSABLE_ENTITY) =>
        Redirect(controllers.routes.ProblemWithServiceController.onPageLoad())
      case Left(CONFLICT) =>
        Redirect(controllers.routes.DuplicateAppealController.onPageLoad())
      case Left(_) =>
        errorHandler.showInternalServerError(Some(userRequest))
      case Right(_) =>
        // Remove any previous appeal data from Mongo + file uploads (if they exist) - fire and forget
        // If it fails it will be picked up by the TTL index
        appealService.removePreviouslySubmittedAppealData(userRequest.session.get(SessionKeys.previouslySubmittedJourneyId))
        Redirect(controllers.routes.AppealConfirmationController.onPageLoad()).addingToSession(
          SessionKeys.previouslySubmittedJourneyId -> userRequest.answers.journeyId,
          SessionKeys.penaltiesHasSeenConfirmationPage -> "true"
        )
    }
  }

  def changeAnswer(continueUrl: RedirectUrl, pageName: String): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => {
      Redirect(continueUrl.get(OnlyRelative).url).addingToSession(SessionKeys.originatingChangePage -> pageName)
    }
  }
}
