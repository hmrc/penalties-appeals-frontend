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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import controllers.PenaltySelectionController.{MissingSessionAnswer, PenaltySelectionError}
import forms.PenaltySelectionForm
import helpers.FormProviderHelper
import models.pages._
import models.{Mode, PenaltyTypeEnum, UserRequest}
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.{AppealCoverBothPenaltiesPage, AppealSinglePenaltyPage, PenaltySelectionPage}
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltySelectionController @Inject()(penaltySelectionPage: PenaltySelectionPage,
                                           appealCoverBothPenaltiesPage: AppealCoverBothPenaltiesPage,
                                           appealSinglePenaltyPage: AppealSinglePenaltyPage,
                                           navigation: Navigation,
                                           sessionService: SessionService,
                                           errorHandler: ErrorHandler)
                                          (implicit mcc: MessagesControllerComponents,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRequired: DataRequiredAction,
                                           dataRetrieval: DataRetrievalAction,
                                           executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  private def penaltyAmounts(implicit userRequest: UserRequest[_]): Either[PenaltySelectionError, (String, String)] =
    for {
      firstPenalty  <- userRequest.answers.getAnswer[String](SessionKeys.firstPenaltyAmount).toRight(MissingSessionAnswer(SessionKeys.firstPenaltyAmount))
      secondPenalty <- userRequest.answers.getAnswer[String](SessionKeys.secondPenaltyAmount).toRight(MissingSessionAnswer(SessionKeys.secondPenaltyAmount))
    } yield (firstPenalty, secondPenalty)

  private def singlePenaltyDetails(implicit userRequest: UserRequest[_]): Either[PenaltySelectionError, (Boolean, String)] =
    for {
      appealType    <- userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).toRight(MissingSessionAnswer(SessionKeys.appealType))
      isLPP2         = appealType == PenaltyTypeEnum.Additional
      amountKey      = if (isLPP2) SessionKeys.secondPenaltyAmount else SessionKeys.firstPenaltyAmount
      penaltyAmount <- userRequest.answers.getAnswer[String](amountKey).toRight(MissingSessionAnswer(amountKey))
    } yield (isLPP2, penaltyAmount)

  def onPageLoadForPenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        PenaltySelectionForm.doYouWantToAppealBothPenalties,
        SessionKeys.doYouWantToAppealBothPenalties,
        userRequest.answers
      )
      val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
      penaltyAmounts match {
        case Right((firstPenalty, secondPenalty)) =>
          Ok(penaltySelectionPage(formProvider, radioOptions, firstPenalty, secondPenalty, pageMode(PenaltySelectionPage, mode)))
        case Left(error) =>
          logger.error(s"[PenaltySelectionController][onPageLoadForPenaltySelection] - ${error.message}")
          errorHandler.showInternalServerError(Some(userRequest))
      }
    }
  }

  def onSubmitForPenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      PenaltySelectionForm.doYouWantToAppealBothPenalties.bindFromRequest().fold(
        errors => {
          val radioOptions = RadioOptionHelper.yesNoRadioOptions(errors)
          penaltyAmounts match {
            case Right((firstPenalty, secondPenalty)) =>
              Future.successful(BadRequest(penaltySelectionPage(errors, radioOptions, firstPenalty, secondPenalty, pageMode(PenaltySelectionPage, mode))))
            case Left(error) =>
              logger.error(s"[PenaltySelectionController][onSubmitForPenaltySelection] - ${error.message}")
              Future.successful(errorHandler.showInternalServerError(Some(userRequest)))
          }
        },
        answer => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.doYouWantToAppealBothPenalties, answer)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(PenaltySelectionPage, mode, Some(answer)))
          }
        }
      )
    }
  }

  def onPageLoadForSinglePenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      singlePenaltyDetails match {
        case Right((isLPP2, penaltyAmount)) =>
          val nextPageUrl: String = navigation.nextPage(AppealSinglePenaltyPage, mode).url
          Ok(appealSinglePenaltyPage(pageMode(AppealSinglePenaltyPage, mode), nextPageUrl, penaltyAmount, isLPP2))
        case Left(error) =>
          logger.error(s"[PenaltySelectionController][onPageLoadForSinglePenaltySelection] - ${error.message}")
          errorHandler.showInternalServerError(Some(userRequest))
      }
    }
  }

  def onPageLoadForAppealCoverBothPenalties(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val nextPageUrl: String = navigation.nextPage(AppealCoverBothPenaltiesPage, mode).url
      Ok(appealCoverBothPenaltiesPage(pageMode(AppealCoverBothPenaltiesPage, mode), nextPageUrl))
    }
  }
}

object PenaltySelectionController {
  sealed trait PenaltySelectionError {
    def message: String
  }

  final case class MissingSessionAnswer(key: String) extends PenaltySelectionError {
    def message: String = s"Required session answer missing for key: $key"
  }
}
