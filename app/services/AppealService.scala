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

package services

import config.AppConfig
import connectors.PenaltiesConnector
import helpers.DateTimeHelper
import models.appeals.AppealSubmission
import models.monitoring.{AppealAuditModel, AuditPenaltyTypeEnum, DuplicateFilesAuditModel}
import models.upload.{UploadJourney, UploadStatusEnum}
import models.{AppealData, PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import play.api.http.Status._
import play.api.libs.json.{JsResult, JsValue, Json}
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.Logger.logger
import utils.{SessionKeys, UUIDGenerator}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              appConfig: AppConfig,
                              dateTimeHelper: DateTimeHelper,
                              auditService: AuditService,
                              idGenerator: UUIDGenerator,
                              uploadJourneyRepository: UploadJourneyRepository) {

  def validatePenaltyIdForEnrolmentKey[A](penaltyId: String, isLPP: Boolean, isAdditional: Boolean)
                                         (implicit user: UserRequest[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
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
    val agentReferenceNo = userRequest.arn
    val penaltyId = userRequest.session.get(SessionKeys.penaltyId).get
    val correlationId: String = idGenerator.generateUUID
    val userHasUploadedFiles = userRequest.session.get(SessionKeys.isUploadEvidence).contains("yes")
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file => file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      uploads = if(userHasUploadedFiles) readyOrDuplicateFileUploads else None
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isLateAppeal, agentReferenceNo, uploads)
      response <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, penaltyId, correlationId)
    } yield {
      response.status match {
        case OK =>
          if (isLPP) {
            if (appealType.contains(PenaltyTypeEnum.Late_Payment.toString)) {
              auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LPP.toString, correlationId, uploads))
            } else {
              auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.Additional.toString, correlationId, uploads))
            }
          } else {
            auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LSP.toString, correlationId, uploads))
          }
          sendAuditIfDuplicatesExist(uploads)
          logger.debug("[AppealService][submitAppeal] - Received OK from the appeal submission call")
          Right((): Unit)
        case _ =>
          logger.error(s"[AppealService][submitAppeal] - Received unknown status code from connector: ${response.status}")
          Left(response.status)
      }
    }
  }.recover {
    case e: UpstreamErrorResponse =>
      logger.error(s"[AppealService][submitAppeal] - Received 4xx/5xx response, error message: ${e.getMessage}")
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
      case e =>
        logger.error(s"$startOfLogMsg unknown error occurred, error message: ${e.getMessage}")
        false
    }
  }

  def sendAuditIfDuplicatesExist(optFileUploads: Option[Seq[UploadJourney]])
                                (implicit userRequest: UserRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    if(optFileUploads.isDefined) {
      val fileUploads = optFileUploads.get.filter(_.uploadDetails.isDefined)
      val checksumMapToUpload = fileUploads.groupBy(_.uploadDetails.get.checksum)
      val onlyDuplicateFileUploads = checksumMapToUpload.filter(_._2.size > 1).values.flatten.toSeq
      if(onlyDuplicateFileUploads.nonEmpty) {
        logger.debug("[AppealService][sendAuditIfDuplicatesExist] - Found duplicates in repository, sending duplicate appeal event")
        val duplicateUploadsInAuditFormat: JsValue = auditService.getAllDuplicateUploadsForAppealSubmission(onlyDuplicateFileUploads)
        auditService.audit(DuplicateFilesAuditModel(duplicateUploadsInAuditFormat))
      }
    }
  }
}
