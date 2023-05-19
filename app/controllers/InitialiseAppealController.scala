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

import config.ErrorHandler
import controllers.predicates.AuthPredicate
import models.appeals.MultiplePenaltiesData
import models.session.UserAnswers
import models.{AppealData, AuthRequest, PenaltyTypeEnum, UserRequest}
import play.api.i18n.I18nSupport
import play.api.mvc._
import _root_.config.featureSwitches.{FeatureSwitching, EnablePRM2509}
import models.PenaltyTypeEnum._
import models.monitoring.AuditPenaltyTypeEnum._
import models.monitoring.PenaltyAppealStartedAuditModel
import play.api.Configuration
import services.monitoring.AuditService
import services.{AppealService, SessionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, SessionKeys}

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InitialiseAppealController @Inject()(appealService: AppealService,
                                           sessionService: SessionService,
                                           auditService: AuditService,
                                           errorHandler: ErrorHandler)
                                          (implicit val mcc: MessagesControllerComponents,
                                            authorise: AuthPredicate,
                                            val config: Configuration,
                                            ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad(penaltyId: String, isLPP: Boolean, isAdditional: Boolean): Action[AnyContent] = authorise.async {
    implicit user => {
      for {
        appealData <- appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP, isAdditional)
        multiPenaltyData <- if (isLPP) appealService.validateMultiplePenaltyDataForEnrolmentKey(penaltyId) else Future.successful(None)
        result <- if (appealData.isDefined) {
          removeExistingKeysFromSessionAndRedirect(routes.AppealStartController.onPageLoad(), penaltyId, appealData.get, multiPenaltyData, isAppealAgainstObligation = false)
        } else Future(errorHandler.showInternalServerError)
      } yield {
        result
      }
    }
  }

  def onPageLoadForObligation(penaltyId: String): Action[AnyContent] = authorise.async {
    implicit user => {
      appealService.validatePenaltyIdForEnrolmentKey(penaltyId, isLPP = false, isAdditional = false).flatMap {
        _.fold(
          Future(errorHandler.showInternalServerError)
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
        userAnswersWithPossibleMultiplePenaltiesData.setAnswer[Boolean](SessionKeys.isObligationAppeal, isAppealAgainstObligation)
      } else userAnswersWithPossibleMultiplePenaltiesData
    }
    auditStartOfAppealJourney(penaltyNumber, appealModel)
    sessionService.updateAnswers(allUserAnswers).map {
      _ => Redirect(urlToRedirectTo)
        .removingFromSession(SessionKeys.allKeys: _*)
        .removingFromSession(SessionKeys.penaltiesHasSeenConfirmationPage)
        .addingToSession((SessionKeys.journeyId, journeyId))
    }
  }

  def auditStartOfAppealJourney(penaltyNumber: String, appealModel: AppealData)
                               (implicit ec: ExecutionContext, hc: HeaderCarrier, request: AuthRequest[_]): Unit = {
    if(isEnabled(EnablePRM2509)) {
      val appealType = appealModel.`type` match {
        case Late_Submission => LSP
        case Late_Payment => FirstLPP
        case Additional => SecondLPP
      }
      val auditModel = PenaltyAppealStartedAuditModel(penaltyNumber, appealType)
      auditService.audit(auditModel)
    }
  }
}
