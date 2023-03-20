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

import java.time.LocalDate

import config.featureSwitches.{FeatureSwitching, ShowDigitalCommsMessage}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import helpers.SessionAnswersHelper
import javax.inject.Inject
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

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(checkYourAnswersPage: CheckYourAnswersPage,
                                           appealService: AppealService,
                                           appealConfirmationPage: AppealConfirmationPage,
                                           errorHandler: ErrorHandler,
                                           uploadJourneyRepository: UploadJourneyRepository,
                                           sessionAnswersHelper: SessionAnswersHelper,
                                           sessionService: SessionService)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           val config: Configuration,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRetrieval: DataRetrievalAction,
                                           dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport with ImplicitDateFormatter with FeatureSwitching {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(CheckYourAnswersPage, mode)

  def onPageLoad: Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
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

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
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
        val confirmationAppealType = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).get.toString
        val confirmationStartDate = dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).get)
        val confirmationEndDate = dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).get)
        val confirmationObligation = userRequest.answers.getAnswer[Boolean](SessionKeys.isObligationAppeal).contains(true).toString
        val confirmationMultipleAppeals = userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).getOrElse("no")
        val confirmationIsAgent = userRequest.isAgent.toString
        uploadJourneyRepository.removeUploadsForJourney(userRequest.session.get(SessionKeys.journeyId).get).map {
          _ => Redirect(controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation()).addingToSession(
            List(
              ("confirmationAppealType", confirmationAppealType),
              ("confirmationStartDate", confirmationStartDate),
              ("confirmationEndDate", confirmationEndDate),
              ("confirmationObligation", confirmationObligation),
              ("confirmationMultipleAppeals", confirmationMultipleAppeals),
              ("confirmationIsAgent", confirmationIsAgent)): _*
          )
        }
      }
    ))
  }

  def onPageLoadForConfirmation(): Action[AnyContent] = authorise {
    implicit userRequest => {
      val appealType = PenaltyTypeEnum.withName(userRequest.session.get(SessionKeys.confirmationAppealType).get)
      val bothPenalties: String = userRequest.session.get(SessionKeys.confirmationMultipleAppeals).get
      val (readablePeriodStart, readablePeriodEnd) =
        (userRequest.session.get(SessionKeys.confirmationStartDate).get,
         userRequest.session.get(SessionKeys.confirmationEndDate).get)
      val isObligationAppeal: Boolean = userRequest.session.get(SessionKeys.confirmationObligation).get.toBoolean
      val showDigitalCommsMessage: Boolean = isEnabled(ShowDigitalCommsMessage)
      val isAgent: Boolean = userRequest.session.get(SessionKeys.confirmationIsAgent).get.toBoolean
      Ok(appealConfirmationPage(readablePeriodStart, readablePeriodEnd, isObligationAppeal, showDigitalCommsMessage, appealType, bothPenalties, isAgent))
        .removingFromSession(SessionKeys.allKeys: _*)
    }
  }

  def changeAnswer(continueUrl: String, pageName: String): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => {
      Redirect(continueUrl).addingToSession(SessionKeys.originatingChangePage -> pageName)
    }
  }
}
