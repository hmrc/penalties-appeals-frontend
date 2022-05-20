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
import config.featureSwitches.{FeatureSwitching, UseNewAPIModel}
import connectors.PenaltiesConnector
import helpers.DateTimeHelper
import models.appeals.AppealSubmission
import models.monitoring.{AppealAuditModel, AuditPenaltyTypeEnum, DuplicateFilesAuditModel}
import models.upload.{UploadJourney, UploadStatusEnum}
import models.v2.{AppealInformation, AppealData => AppealDataV2}
import models.{AppealData, PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsResult, JsValue, Json}
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.Logger.logger
import utils.{SessionKeys, UUIDGenerator}

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              appConfig: AppConfig,
                              dateTimeHelper: DateTimeHelper,
                              auditService: AuditService,
                              idGenerator: UUIDGenerator,
                              uploadJourneyRepository: UploadJourneyRepository)(implicit val config: Configuration) extends FeatureSwitching {

  def validatePenaltyIdForEnrolmentKey[A](penaltyId: String, isLPP: Boolean, isAdditional: Boolean)
                                         (implicit user: UserRequest[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealInformation[_]]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, user.vrn, isLPP, isAdditional).map {
      _.fold[Option[AppealInformation[_]]](
        None
      )(
        jsValue => {
          val parsedAppealDataModel = Json.fromJson(jsValue)(if(isEnabled(UseNewAPIModel)) AppealDataV2.format else AppealData.format)
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
    val enrolmentKey = constructMTDVATEnrolmentKey(userRequest.vrn)
    val appealType = userRequest.session.get(SessionKeys.appealType)
    val isLPP = appealType.contains(PenaltyTypeEnum.Late_Payment.toString) || appealType.contains(PenaltyTypeEnum.Additional.toString)
    val agentReferenceNo = userRequest.arn
    val penaltyNumber = userRequest.session.get(SessionKeys.penaltyNumber).get
    val correlationId: String = idGenerator.generateUUID
    val userHasUploadedFiles = userRequest.session.get(SessionKeys.isUploadEvidence).contains("yes")
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file => file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      uploads = if(userHasUploadedFiles) readyOrDuplicateFileUploads else None
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isAppealLate, agentReferenceNo, uploads)
      response <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, penaltyNumber, correlationId)
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

  private def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
    if(isEnabled(UseNewAPIModel)) {
      val dateTimeNow: LocalDate = dateTimeHelper.dateNow
      val dateSentParsed: LocalDate = LocalDate.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
      dateSentParsed.isBefore(dateTimeNow.minusDays(appConfig.daysRequiredForLateAppeal))
    } else {
      val dateTimeNow: LocalDateTime = dateTimeHelper.dateTimeNow
      val dateSentParsed: LocalDateTime = LocalDateTime.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
      dateSentParsed.isBefore(dateTimeNow.minusDays(appConfig.daysRequiredForLateAppeal))
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
