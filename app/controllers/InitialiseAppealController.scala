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
import controllers.predicates.AuthPredicate
import models.PenaltyTypeEnum._
import models.appeals.MultiplePenaltiesData
import models.session.UserAnswers
import models.{AppealData, AuthRequest, PenaltyTypeEnum}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{AppealService, SessionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, SessionKeys}
import java.time.LocalDate
import java.util.UUID

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class InitialiseAppealController @Inject()(appealService: AppealService,
                                           sessionService: SessionService,
                                           errorHandler: ErrorHandler,
                                           appConfig: AppConfig)
                                          (implicit val mcc: MessagesControllerComponents,
                                           authorise: AuthPredicate,
                                           val config: Configuration,
                                           ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(penaltyId: String, isLPP: Boolean, isAdditional: Boolean): Action[AnyContent] = authorise.async {
    implicit user => {
      for {
        appealData <- appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP, isAdditional)
        multiPenaltyData <- if (isLPP) appealService.validateMultiplePenaltyDataForEnrolmentKey(penaltyId) else Future.successful(None)
        result <- if (appealData.isDefined) {
          removeExistingKeysFromSessionAndRedirect(routes.AppealStartController.onPageLoad(), penaltyId, appealData.get, multiPenaltyData, isAppealAgainstObligation = false)
        } else Future(errorHandler.showInternalServerError())
      } yield {
        result
      }
    }
  }

  def onPageLoadForFindOutHowToAppealLSP(penaltyId: String): Action[AnyContent] = authorise.async {
    implicit user => {
      appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP = false, isAdditional = false).flatMap {
        _.fold(
          Future(errorHandler.showInternalServerError())
        )(
          appealData => {
            val redirectUrl = {
              findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad()
            }
            removeExistingKeysFromSessionAndRedirect(
              redirectUrl, penaltyId, appealData, isAppealAgainstObligation = true
            )
          }
        )
      }
    }
  }

  def onPageLoadForFindOutHowToAppealLPP(principalChargeReference: String, vatAmountInPence: Int, vatPeriodStartDate: String, vatPeriodEndDate: String, isCa: Boolean): Action[AnyContent] = authorise.async {
    implicit request => {
      val vatAmmountBD = BigDecimal(vatAmountInPence) / 100

      removeExistingKeysFromSessionAndRedirectToFindOutHowToAppeal(
        controllers.findOutHowToAppeal.routes.FindOutHowToAppealStartController.startFindOutHowToAppeal(),
        vatPeriodStartDate = LocalDate.parse(vatPeriodStartDate),
        vatPeriodEndDate = LocalDate.parse(vatPeriodEndDate),
        vatAmount = vatAmmountBD,
        principalChargeReference = principalChargeReference,
        isCaLpp = isCa
      )
    }
  }

  private def removeExistingKeysFromSessionAndRedirect[A](urlToRedirectTo: Call,
                                                          penaltyNumber: String,
                                                          appealModel: AppealData,
                                                          multiPenaltiesModel: Option[MultiplePenaltiesData] = None,
                                                          isAppealAgainstObligation: Boolean)(implicit user: AuthRequest[A]): Future[Result] = {
    logger.debug(s"[InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - Resetting appeals session: removing keys from session" +
      s" and replacing with new keys")
    val journeyId: String = UUID.randomUUID().toString
    logger.debug(s"InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - Setting journeyId to: $journeyId")
    logger.debug(s"InitialiseAppealController][removeExistingKeysFromSessionAndRedirect] - MultiPenaltyData is defined: $multiPenaltiesModel")

    val baseUserAnswers = UserAnswers(journeyId)
      .setAnswer[String](SessionKeys.penaltyNumber, penaltyNumber)
      .setAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType, appealModel.`type`)
      .setAnswer[LocalDate](SessionKeys.startDateOfPeriod, appealModel.startDate)
      .setAnswer[LocalDate](SessionKeys.endDateOfPeriod, appealModel.endDate)
      .setAnswer[LocalDate](SessionKeys.dueDateOfPeriod, appealModel.dueDate)
      .setAnswer[LocalDate](SessionKeys.dateCommunicationSent, appealModel.dateCommunicationSent)

    val userAnswersWithPossibleMultiplePenaltiesData = multiPenaltiesModel.map {
      data => {
        val parsedFirstPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(data.firstPenaltyAmount)
        val firstPenaltyChargeReference = data.firstPenaltyChargeReference
        val parsedSecondPenaltyAmount = CurrencyFormatter.parseBigDecimalToFriendlyValue(data.secondPenaltyAmount)
        val secondPenaltyChargeReference = data.secondPenaltyChargeReference
        val firstPenaltyCommunicationDate = data.firstPenaltyCommunicationDate
        val secondPenaltyCommunicationDate = data.secondPenaltyCommunicationDate

        baseUserAnswers
          .setAnswer[String](SessionKeys.firstPenaltyChargeReference, firstPenaltyChargeReference)
          .setAnswer[String](SessionKeys.firstPenaltyAmount, parsedFirstPenaltyAmount)
          .setAnswer[String](SessionKeys.secondPenaltyChargeReference, secondPenaltyChargeReference)
          .setAnswer[String](SessionKeys.secondPenaltyAmount, parsedSecondPenaltyAmount)
          .setAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate, firstPenaltyCommunicationDate)
          .setAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate, secondPenaltyCommunicationDate)
      }
    }.getOrElse(baseUserAnswers)

    val allUserAnswers = {
      if (isAppealAgainstObligation) {
        userAnswersWithPossibleMultiplePenaltiesData.setAnswer[Boolean](SessionKeys.isFindOutHowToAppeal, isAppealAgainstObligation)
      } else userAnswersWithPossibleMultiplePenaltiesData
    }
    sessionService.updateAnswers(allUserAnswers).map {
      _ =>
        Redirect(urlToRedirectTo)
          .removingFromSession(SessionKeys.allKeys: _*)
          .removingFromSession(SessionKeys.penaltiesHasSeenConfirmationPage)
          .addingToSession((SessionKeys.journeyId, journeyId))
    }
  }

  private def removeExistingKeysFromSessionAndRedirectToFindOutHowToAppeal[A](urlToRedirectTo: Call,
                                                                              vatPeriodStartDate: LocalDate,
                                                                              vatPeriodEndDate: LocalDate,
                                                                              vatAmount: BigDecimal,
                                                                              principalChargeReference: String,
                                                                              isCaLpp: Boolean)(implicit user: AuthRequest[A]): Future[Result] = {
    logger.debug(s"[InitialiseAppealController][removeExistingKeysFromSessionAndRedirectToFindOutHowToAppeal] - Resetting appeals session: removing keys from session" +
      s" and replacing with new keys")
    val journeyId: String = UUID.randomUUID().toString
    logger.debug(s"InitialiseAppealController][removeExistingKeysFromSessionAndRedirectToFindOutHowToAppeal] - Setting journeyId to: $journeyId")

    val baseUserAnswers = UserAnswers(journeyId)
      .setAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType, PenaltyTypeEnum.Late_Payment)
      .setAnswer[LocalDate](SessionKeys.startDateOfPeriod, vatPeriodStartDate)
      .setAnswer[LocalDate](SessionKeys.endDateOfPeriod, vatPeriodEndDate)
      .setAnswer[String](SessionKeys.principalChargeReference, principalChargeReference)
      .setAnswer[BigDecimal](SessionKeys.vatAmount, vatAmount)
      .setAnswer[Boolean](SessionKeys.isCaLpp, isCaLpp)

    sessionService.updateAnswers(baseUserAnswers).map {
      _ =>
        Redirect(urlToRedirectTo)
          .removingFromSession(SessionKeys.allKeys: _*)
          .removingFromSession(SessionKeys.penaltiesHasSeenConfirmationPage)
          .addingToSession((SessionKeys.journeyId, journeyId))
    }.recover(
      _ =>
        errorHandler.showInternalServerError()
    )
  }
}
