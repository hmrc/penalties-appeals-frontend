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

import config.featureSwitches.{FeatureSwitching, ShowDigitalCommsMessage}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, CheckObligationAvailabilityAction, DataRequiredAction, DataRetrievalAction}
import helpers.{IsLateAppealHelper, SessionAnswersHelper}
import models.pages.{CheckYourAnswersPage, PageMode}
import models.{Mode, NormalMode, PenaltyTypeEnum, UserRequest}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.UploadJourneyRepository
import services.{AppealService, SessionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.{AppealConfirmationPage, CheckYourAnswersPage}
import viewtils.ImplicitDateFormatter

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(checkYourAnswersPage: CheckYourAnswersPage,
                                           appealService: AppealService,
                                           appealConfirmationPage: AppealConfirmationPage,
                                           errorHandler: ErrorHandler,
                                           sessionService: SessionService,
                                           sessionAnswersHelper: SessionAnswersHelper,
                                           isLateAppealHelper: IsLateAppealHelper)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           val config: Configuration,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRetrieval: DataRetrievalAction,
                                           dataRequired: DataRequiredAction,
                                           checkObligationAvailability: CheckObligationAvailabilityAction) extends FrontendController(mcc) with I18nSupport with ImplicitDateFormatter with FeatureSwitching {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(CheckYourAnswersPage, mode)

  def onPageLoad: Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired andThen checkObligationAvailability).async {
    implicit userRequest => {
      if(isLateAppealHelper.isAppealLate() && userRequest.answers.getAnswer[String](SessionKeys.lateAppealReason).isEmpty){
        logger.warn("[CheckYourAnswersController][onPageLoad] User tried skipping late appeal page, redirecting back to late appeal page")
        Future(Redirect(controllers.routes.MakingALateAppealController.onPageLoad()))
      } else {
        userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).fold({
          if (userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).isDefined && sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty) {
            logger.debug(s"[CheckYourAnswersController][onPageLoad] Loading check your answers page for appealing against obligation")
            for {
              fileNames <- sessionAnswersHelper.getPreviousUploadsFileNames()(userRequest)
            } yield {
              val answersFromSession = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))
              Ok(checkYourAnswersPage(answersFromSession, pageMode(NormalMode))).removingFromSession(SessionKeys.originatingChangePage)
            }
          } else {
            logger.error("[CheckYourAnswersController][onPageLoad] User hasn't selected reasonable excuse option - no key in session")
            Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
          }
        })(
          reasonableExcuse => {
            if (sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty) {
              logger.debug(s"[CheckYourAnswersController][onPageLoad] Loading check your answers page")
              for {
                content <- sessionAnswersHelper.getContentWithExistingUploadFileNames(reasonableExcuse)
              } yield {
                Ok(checkYourAnswersPage(content, pageMode(NormalMode))).removingFromSession(SessionKeys.originatingChangePage)
              }
            } else {
              logger.error(s"[CheckYourAnswersController][onPageLoad] User hasn't got all keys in session for reasonable excuse: $reasonableExcuse")
              logger.debug(s"[CheckYourAnswersController][onPageLoad] User has keys: ${userRequest.session.data} " +
                s"and tried to load page with reasonable excuse: $reasonableExcuse")
              Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
            }
          }
        )
      }
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired andThen checkObligationAvailability).async {
    implicit userRequest => {
      userRequest.answers.getAnswer[String](SessionKeys.reasonableExcuse).fold({
        if(userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).contains(true)) {
          handleAppealSubmission("obligation")
        } else {
          logger.error("[CheckYourAnswersController][onSubmit] No reasonable excuse selection found in session")
          Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
        }
      })(
        reasonableExcuse => {
          if (sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse(reasonableExcuse)) {
            logger.debug(s"[CheckYourAnswersController][onPageLoad] All keys are present for reasonable excuse: $reasonableExcuse")
            handleAppealSubmission(reasonableExcuse)
          } else {
            logger.error(s"[CheckYourAnswersController][onSubmit] User did not have all answers for reasonable excuse: $reasonableExcuse")
            Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoad()))
          }
        }
      )
    }
  }

  private def handleAppealSubmission(reasonableExcuse: String)(implicit userRequest: UserRequest[_]): Future[Result] = {
    appealService.submitAppeal(reasonableExcuse).flatMap(_.fold(
      {
        case SERVICE_UNAVAILABLE => Future(Redirect(controllers.routes.ServiceUnavailableController.onPageLoad()))
        case INTERNAL_SERVER_ERROR | BAD_REQUEST | UNPROCESSABLE_ENTITY => Future(Redirect(controllers.routes.ProblemWithServiceController.onPageLoad()))
        case CONFLICT => Future(Redirect(controllers.routes.DuplicateAppealController.onPageLoad()))
        case _ => Future(errorHandler.showInternalServerError)
      },
      _ => {
        val previouslySubmittedJourneyId = userRequest.answers.journeyId
        Future(Redirect(controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation()).addingToSession(
          SessionKeys.previouslySubmittedJourneyId -> previouslySubmittedJourneyId,
          SessionKeys.penaltiesHasSeenConfirmationPage -> "true"
        ))
      }
    ))
  }

  def onPageLoadForConfirmation(): Action[AnyContent] = authorise.async {
    implicit request => {
      request.session.get(SessionKeys.previouslySubmittedJourneyId).fold({
        logger.warn(s"[CheckYourAnswersController][onPageLoadForConfirmation] - No journey ID was found in the session for VRN: ${request.vrn} - " +
          s"redirecting to incomplete session data page")
        Future(Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData()))
      })(
        journeyId => {
          sessionService.getUserAnswers(journeyId).map {
            userAnswers => {
              userAnswers.fold({
                logger.warn(s"[CheckYourAnswersController][onPageLoadForConfirmation] - No submitted user answers were found in the session for VRN: ${request.vrn} - " +
                  s"redirecting to incomplete session data page")
                Redirect(controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData())
              })(
                userAnswers => {
                  val appealType = userAnswers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).get
                  val bothPenalties: String = userAnswers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).getOrElse("no")
                  val startDate: LocalDate = userAnswers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).get
                  val endDate: LocalDate = userAnswers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).get
                  val (readablePeriodStart, readablePeriodEnd) = (dateToString(startDate), dateToString(endDate))
                  val isObligationAppeal: Boolean = userAnswers.getAnswer[Boolean](SessionKeys.isObligationAppeal).contains(true)
                  val showDigitalCommsMessage: Boolean = isEnabled(ShowDigitalCommsMessage)
                  val isAgent: Boolean = request.isAgent
                  val vrn = request.vrn
                  Ok(appealConfirmationPage(readablePeriodStart, readablePeriodEnd, isObligationAppeal, showDigitalCommsMessage, appealType, bothPenalties, isAgent, vrn))
                    .removingFromSession(SessionKeys.allKeys: _*)
                }
              )
            }
          }
        })
    }
  }

  def changeAnswer(continueUrl: String, pageName: String): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => {
      Redirect(continueUrl).addingToSession(SessionKeys.originatingChangePage -> pageName)
    }
  }
}
