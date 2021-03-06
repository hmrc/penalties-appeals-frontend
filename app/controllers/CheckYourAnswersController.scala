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

import java.time.LocalDate

import config.featureSwitches.FeatureSwitching
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import helpers.SessionAnswersHelper
import javax.inject.Inject
import models.PenaltyTypeEnum.{Additional, Late_Payment, Late_Submission}
import models.pages.{CheckYourAnswersPage, PageMode}
import models.{Mode, NormalMode, PenaltyTypeEnum, UserRequest}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import services.AppealService
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
                                           sessionAnswersHelper: SessionAnswersHelper)
                                          (implicit mcc: MessagesControllerComponents,
                                           ec: ExecutionContext,
                                           val config: Configuration,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport with ImplicitDateFormatter with FeatureSwitching {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(CheckYourAnswersPage, mode)

  def onPageLoad: Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      request.session.get(SessionKeys.reasonableExcuse).fold({
        if(request.session.get(SessionKeys.isObligationAppeal).isDefined && sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage().nonEmpty) {
          logger.debug(s"[CheckYourAnswersController][onPageLoad] Loading check your answers page for appealing against obligation")
          for {
            fileNames <- sessionAnswersHelper.getPreviousUploadsFileNames()(request)
          } yield {
            val answersFromSession = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(if (fileNames.isEmpty) None else Some(fileNames))
            Ok(checkYourAnswersPage(answersFromSession, pageMode(NormalMode))).removingFromSession(SessionKeys.originatingChangePage)
          }
        } else {
          logger.error("[CheckYourAnswersController][onPageLoad] User hasn't selected reasonable excuse option - no key in session")
          Future(errorHandler.showInternalServerError)
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
            logger.debug(s"[CheckYourAnswersController][onPageLoad] User has keys: ${request.session.data} " +
              s"and tried to load page with reasonable excuse: $reasonableExcuse")
            Future(errorHandler.showInternalServerError)
          }
        }
      )
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      request.session.get(SessionKeys.reasonableExcuse).fold({
        if(request.session.get(SessionKeys.isObligationAppeal).getOrElse("") == "true") {
          handleAppealSubmission("obligation")
        } else {
          logger.error("[CheckYourAnswersController][onSubmit] No reasonable excuse selection found in session")
          Future(errorHandler.showInternalServerError)
        }
      })(
        reasonableExcuse => {
          if (sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse(reasonableExcuse)) {
            logger.debug(s"[CheckYourAnswersController][onPageLoad] All keys are present for reasonable excuse: $reasonableExcuse")
            handleAppealSubmission(reasonableExcuse)
          } else {
            logger.error(s"[CheckYourAnswersController][onSubmit] User did not have all answers for reasonable excuse: $reasonableExcuse")
            Future(errorHandler.showInternalServerError)
          }
        }
      )
    }
  }

  private def handleAppealSubmission(reasonableExcuse: String)(implicit userRequest: UserRequest[_]) = {
    appealService.submitAppeal(reasonableExcuse).flatMap(_.fold(
      {
        case SERVICE_UNAVAILABLE => Future(Redirect(controllers.routes.ServiceUnavailableController.onPageLoad()))
        case INTERNAL_SERVER_ERROR | BAD_REQUEST | UNPROCESSABLE_ENTITY => Future(Redirect(controllers.routes.ProblemWithServiceController.onPageLoad()))
        case CONFLICT => Future(Redirect(controllers.routes.DuplicateAppealController.onPageLoad()))
        case _ => Future(errorHandler.showInternalServerError)
      },
      _ => {
        uploadJourneyRepository.removeUploadsForJourney(userRequest.session.get(SessionKeys.journeyId).get).map {
          _ => Redirect(controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation())
        }
      }
    ))
  }

  def onPageLoadForConfirmation(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val penaltyType: String = PenaltyTypeEnum.withName(request.session.get(SessionKeys.appealType).get) match {
        case Late_Submission => "penaltyType.lateSubmission"
        case Late_Payment | Additional => "penaltyType.latePayment"
      }
      val (readablePeriodStart, readablePeriodEnd) =
        (dateToString(LocalDate.parse(request.session.get(SessionKeys.startDateOfPeriod).get)),
          dateToString(LocalDate.parse(request.session.get(SessionKeys.endDateOfPeriod).get)))
      val isObligationAppeal: Boolean = request.session.get(SessionKeys.isObligationAppeal).contains("true")
      Ok(appealConfirmationPage(penaltyType, readablePeriodStart, readablePeriodEnd, isObligationAppeal))
        .removingFromSession(SessionKeys.allKeys: _*)
    }
  }

  def changeAnswer(continueUrl: String, pageName: String): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      Redirect(continueUrl).addingToSession(SessionKeys.originatingChangePage -> pageName)
    }
  }
}
