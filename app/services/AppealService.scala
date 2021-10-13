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

package services

import config.AppConfig
import connectors.{HeaderGenerator, PenaltiesConnector}
import helpers.DateTimeHelper
import models.appeals.{AgentDetails, AppealSubmission}
import models.monitoring.{AppealAuditModel, AuditPenaltyTypeEnum}
import models.{AppealData, PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import play.api.http.Status._
import utils.Logger.logger
import play.api.libs.json.{JsResult, Json}
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse, UpstreamErrorResponse}
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.SessionKeys

import java.time.LocalDateTime
import javax.inject.Inject
import uk.gov.hmrc.auth.core.retrieve.{AgentInformation, Name}
import uk.gov.hmrc.emailaddress.EmailAddress

import scala.concurrent.{ExecutionContext, Future}

class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              appConfig: AppConfig,
                              dateTimeHelper: DateTimeHelper,
                              auditService: AuditService,
                              headerGenerator: HeaderGenerator,
                              uploadJourneyRepository: UploadJourneyRepository) {

  def validatePenaltyIdForEnrolmentKey[A](penaltyId: String, isLPP: Boolean, isAdditional: Boolean)(implicit user: UserRequest[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, user.vrn, isLPP, isAdditional).map {
      _.fold[Option[AppealData]](
        None
      )(
        jsValue => {
          val parsedAppealDataModel = Json.fromJson(jsValue)(AppealData.format)
          parsedAppealDataModel.fold(
            failure => {
              logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Failed to parse to model with error(s): $failure")
              None
            },
            parsedModel => Some(parsedModel)
          )
        }
      )
    }
  }

  def getReasonableExcuseListAndParse()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[ReasonableExcuse]]] = {
    penaltiesConnector.getListOfReasonableExcuses().map {
      _.fold[Option[Seq[ReasonableExcuse]]](
        None
      )(
        jsValue => {
          val resultOfParsing: JsResult[Seq[ReasonableExcuse]] = Json.fromJson[Seq[ReasonableExcuse]](jsValue)(ReasonableExcuse.seqReads)
          resultOfParsing.fold(
            failure => {
              logger.error(s"[AppealService][getReasonableExcuseListAndParse] - Failed to parse to model with error(s): $failure")
              None
            },
            seqOfReasonableExcuses => Some(seqOfReasonableExcuses)
          )
        }
      )
    }
  }

  def submitAppeal(reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    val dateSentParsed: LocalDateTime = LocalDateTime.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateTimeNow: LocalDateTime = dateTimeHelper.dateTimeNow
    val isLateAppeal = dateSentParsed.isBefore(dateTimeNow.minusDays(daysResultingInLateAppeal))
    val enrolmentKey = constructMTDVATEnrolmentKey(userRequest.vrn)
    val appealType = userRequest.session.get(SessionKeys.appealType)
    val isLPP = appealType.contains(PenaltyTypeEnum.Late_Payment.toString) || appealType.contains(PenaltyTypeEnum.Additional.toString)
    val agentDetails = getAgentDetails
    val penaltyId = userRequest.session.get(SessionKeys.penaltyId).get
    for {
      amountOfFileUploads <- uploadJourneyRepository.getNumberOfDocumentsForJourneyId(userRequest.session.get(SessionKeys.journeyId).get)
      modelFromRequest: AppealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(reasonableExcuse, isLateAppeal, amountOfFileUploads, agentDetails)
      response <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, penaltyId)
    } yield {
      response.status match {
        case OK => {
          if (isLPP)
            if (appealType.contains(PenaltyTypeEnum.Late_Payment.toString))
              auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LPP.toString, headerGenerator))
            else
              auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.Additional.toString, headerGenerator))
          else
            auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LSP.toString, headerGenerator))
          logger.debug("[AppealService][submitAppeal] - Received OK from the appeal submission call")
          Right()
        }
        case _ => {
          logger.error(s"[AppealService][submitAppeal] - Received unknown status code from connector: ${response.status}")
          Left(response.status)
        }
      }
    }
  }.recover {
    case e: UpstreamErrorResponse =>
      logger.error(s"[AppealService][submitAppeal] - Received 5xx response, error message: ${e.getMessage}")
      Left(e.statusCode)
    case e =>
      logger.error(s"[AppealService][submitAppeal] - An unknown error occurred, error message: ${e.getMessage}")
      Left(INTERNAL_SERVER_ERROR)
  }

  def otherPenaltiesInTaxPeriod(penaltyId: String, isLPP: Boolean)
                               (implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean] = {
    val startOfLogMsg: String = "[AppealService][otherPenaltiesInTaxPeriod] -"
    penaltiesConnector.getOtherPenaltiesInTaxPeriod(penaltyId, userRequest.vrn, isLPP).map(
      response => response.status match {
        case OK =>
          logger.debug(s"$startOfLogMsg Received OK from the other penalties call")
          true
        case NO_CONTENT =>
          logger.debug(s"$startOfLogMsg Received No CONTENT from the other penalties call")
          false
      }
    ).recover {
      case e => {
        logger.error(s"$startOfLogMsg unknown error occurred, error message: ${e.getMessage}")
        false
      }
    }
  }

  def getAgentDetails(implicit user: UserRequest[_]): Option[AgentDetails] = {
    if (user.isAgent) {
      val details = user.agentDetails
      Some(AgentDetails(
        agentReferenceNo = user.arn.getOrElse(""),
        name = details._1.map(_.name.getOrElse("")).getOrElse(""),
        addressLine1 = details._3.map(_.line1.getOrElse("")).getOrElse(""),
        addressLine2 = details._3.map(_.line2.getOrElse("")),
        addressLine3 = details._3.map(_.line3.getOrElse("")),
        addressLine4 = details._3.map(_.line4.getOrElse("")),
        addressLine5 = details._3.map(_.line5.getOrElse("")),
        postCode = details._3.map(_.postCode.getOrElse("")).getOrElse(""),
        agentEmailID = Some(EmailAddress(details._2.getOrElse("")))
      ))
    } else
      None
  }
}
