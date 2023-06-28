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

package services

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.PenaltiesConnector
import connectors.httpParsers.AppealSubmissionHTTPParser.AppealSubmissionResponse
import connectors.httpParsers.ErrorResponse
import helpers.DateTimeHelper
import models._
import models.appeals.{AppealSubmission, AppealSubmissionResponseModel, MultiplePenaltiesData}
import models.monitoring.{AppealAuditModel, AuditPenaltyTypeEnum, DuplicateFilesAuditModel}
import models.upload.{UploadJourney, UploadStatusEnum}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsResult, JsValue, Json}
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys
import utils.{EnrolmentKeys, SessionKeys, UUIDGenerator}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealService @Inject()(penaltiesConnector: PenaltiesConnector,
                              appConfig: AppConfig,
                              dateTimeHelper: DateTimeHelper,
                              auditService: AuditService,
                              idGenerator: UUIDGenerator,
                              uploadJourneyRepository: UploadJourneyRepository)(implicit val config: Configuration) extends FeatureSwitching {

  def validatePenaltyIdForEnrolmentKey(penaltyId: String, isLPP: Boolean, isAdditional: Boolean)
                                      (implicit user: AuthRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, user.vrn, isLPP, isAdditional).map {
      _.fold[Option[AppealData]]({
        logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Found no appeal data for penalty ID: $penaltyId")
        None
      })(
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

  def validateMultiplePenaltyDataForEnrolmentKey(penaltyId: String)
                                                (implicit user: AuthRequest[_],
                                                 hc: HeaderCarrier,
                                                 ec: ExecutionContext): Future[Option[MultiplePenaltiesData]] = {
    val enrolmentKey = EnrolmentKeys.constructMTDVATEnrolmentKey(user.vrn)
    for {
      multiplePenaltiesResponse <- penaltiesConnector.getMultiplePenaltiesForPrincipleCharge(penaltyId, enrolmentKey)
    } yield {
      multiplePenaltiesResponse match {
        case Right(model) =>
          logger.info(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - Received Right with parsed model")
          Some(model)
        case Left(e) =>
          logger.error(s"[AppealService][validateMultiplePenaltyDataForEnrolmentKey] - received Left with error $e")
          None
      }
    }
  }

  def getReasonableExcuseListAndParse()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[ReasonableExcuse]]] = {
    penaltiesConnector.getListOfReasonableExcuses().map {
      _.fold[Option[Seq[ReasonableExcuse]]]({
        logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Found no reasonable excuses")
        None
      })(
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
    val appealType = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType)
    val isLPP = appealType.contains(PenaltyTypeEnum.Late_Payment) || appealType.contains(PenaltyTypeEnum.Additional)
    val agentReferenceNo = userRequest.arn
    val userHasUploadedFiles = userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).contains("yes")
    if (!userRequest.answers.getAnswer[String](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString) && userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      multipleAppeal(enrolmentKey, appealType, isLPP, agentReferenceNo, userHasUploadedFiles, reasonableExcuse)
    } else {
      singleAppeal(enrolmentKey, appealType, isLPP, agentReferenceNo, userHasUploadedFiles, reasonableExcuse)
    }
  }

  private def singleAppeal(enrolmentKey: String, appealType: Option[PenaltyTypeEnum.Value], isLPP: Boolean,
                           agentReferenceNo: Option[String], userHasUploadedFiles: Boolean,
                           reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file =>
        file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      correlationId = idGenerator.generateUUID
      uploads = if (userHasUploadedFiles) readyOrDuplicateFileUploads else None
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isAppealLate(), agentReferenceNo, uploads)
      penaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.penaltyNumber).get
      response <- {
        penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, penaltyNumber, correlationId, isMultiAppeal = false)
      }
    } yield {
      response.fold(
        error => {
          logger.error(s"[AppealService][singleAppeal] - Received unknown status code from connector: ${error.status}")
          Left(error.status)
        },
        responseModel => {
          sendAppealAudit(modelFromRequest, uploads, correlationId, isLPP, isMultipleAppeal = false, appealType, responseModel.caseId.get, penaltyNumber)
          sendAuditIfDuplicatesExist(uploads)
          logger.info("[AppealService][singleAppeal] - Received OK from the appeal submission call")
          Right((): Unit)
        }
      )
    }
  }.recover {
    case e: UpstreamErrorResponse =>
      logger.error(s"[AppealService][singleAppeal] - Received 4xx/5xx response, error message: ${e.getMessage}")
      Left(e.statusCode)
    case e =>
      logger.error(s"[AppealService][singleAppeal] - An unknown error occurred, error message: ${e.getMessage}")
      Left(INTERNAL_SERVER_ERROR)
  }

  private def multipleAppeal(enrolmentKey: String, appealType: Option[PenaltyTypeEnum.Value], isLPP: Boolean,
                             agentReferenceNo: Option[String], userHasUploadedFiles: Boolean,
                             reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file =>
        file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      uploads = if (userHasUploadedFiles) readyOrDuplicateFileUploads else None
      firstCorrelationId = idGenerator.generateUUID
      secondCorrelationId = idGenerator.generateUUID
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isAppealLate(), agentReferenceNo, uploads)
      firstPenaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.firstPenaltyChargeReference).get
      secondPenaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.secondPenaltyChargeReference).get
      firstResponse <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, firstPenaltyNumber, firstCorrelationId, isMultiAppeal = true)
      secondResponse <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, secondPenaltyNumber, secondCorrelationId, isMultiAppeal = true)
      vrn = userRequest.vrn
      dateFrom = userRequest.answers.getAnswer[String](SessionKeys.startDateOfPeriod).get
      dateTo = userRequest.answers.getAnswer[String](SessionKeys.endDateOfPeriod).get
    } yield {
      handleMultipleAppealResponse(firstResponse, secondResponse, isLPP, appealType, modelFromRequest, uploads, vrn, dateFrom, dateTo,
        firstCorrelationId, secondCorrelationId, firstPenaltyNumber, secondPenaltyNumber)
    }
  }.recover {
    case e: UpstreamErrorResponse =>
      logger.error(s"[AppealService][multipleAppeal] - Received 4xx/5xx response, error message: ${e.getMessage}")
      Left(e.statusCode)
    case e =>
      logger.error(s"[AppealService][multipleAppeal] - An unknown error occurred, error message: ${e.getMessage}")
      Left(INTERNAL_SERVER_ERROR)
  }

  def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
    val dateTimeNow: LocalDate = dateTimeHelper.dateNow
    val dateWhereLateAppealIsApplicable: LocalDate = dateTimeNow.minusDays(appConfig.daysRequiredForLateAppeal)
    if (userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      userRequest.answers.getAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate).exists(_.isBefore(dateWhereLateAppealIsApplicable)) ||
        userRequest.answers.getAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate).exists(_.isBefore(dateWhereLateAppealIsApplicable))
    } else {
      val dateSentParsed: LocalDate = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).get
      dateSentParsed.isBefore(dateWhereLateAppealIsApplicable)
    }
  }

  def sendAuditIfDuplicatesExist(optFileUploads: Option[Seq[UploadJourney]])
                                (implicit userRequest: UserRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    if (optFileUploads.isDefined) {
      val fileUploads = optFileUploads.get.filter(_.uploadDetails.isDefined)
      val checksumMapToUpload = fileUploads.groupBy(_.uploadDetails.get.checksum)
      val onlyDuplicateFileUploads = checksumMapToUpload.filter(_._2.size > 1).values.flatten.toSeq
      if (onlyDuplicateFileUploads.nonEmpty) {
        logger.debug("[AppealService][sendAuditIfDuplicatesExist] - Found duplicates in repository, sending duplicate appeal event")
        val duplicateUploadsInAuditFormat: JsValue = auditService.getAllDuplicateUploadsForAppealSubmission(onlyDuplicateFileUploads)
        auditService.audit(DuplicateFilesAuditModel(duplicateUploadsInAuditFormat))
      }
    }
  }

  //scalastyle:off
  private def handleMultipleAppealResponse(firstResponse: AppealSubmissionResponse,
                                           secondResponse: AppealSubmissionResponse,
                                           isLPP: Boolean,
                                           appealType: Option[PenaltyTypeEnum.Value],
                                           modelFromRequest: AppealSubmission,
                                           uploads: Option[Seq[UploadJourney]],
                                           vrn: String,
                                           dateFrom: String,
                                           dateTo: String,
                                           firstCorrelationId: String,
                                           secondCorrelationId: String,
                                           firstPenaltyNumber: String,
                                           secondPenaltyNumber: String)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, userRequest: UserRequest[_]): Either[Int, Unit] = {
    (firstResponse, secondResponse) match {
      //Both succeed
      case (Right(firstResponseModel), Right(secondResponseModel)) =>
        logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId, vrn, dateFrom, dateTo)
        sendAppealAudit(modelFromRequest, uploads, firstCorrelationId, isLPP, isMultipleAppeal = true, appealType, firstResponseModel.caseId.get, firstPenaltyNumber)
        sendAppealAudit(modelFromRequest, uploads, secondCorrelationId, isLPP, isMultipleAppeal = true, appealType, secondResponseModel.caseId.get, secondPenaltyNumber)
        sendAuditIfDuplicatesExist(uploads)
        Right((): Unit)
      //One (LPP1) succeeded
      case (Right(firstResponseModel), Left(secondResponseModel)) =>
        logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, firstCorrelationId, secondCorrelationId, vrn, dateFrom, dateTo)
        sendAppealAudit(modelFromRequest, uploads, firstCorrelationId, isLPP, isMultipleAppeal = true, appealType, firstResponseModel.caseId.get, firstPenaltyNumber)
        sendAuditIfDuplicatesExist(uploads)
        logger.debug(s"[AppealService][multipleAppeal] - First penalty was $firstResponseModel, second penalty was $secondResponseModel")
        Right((): Unit)
      //One (LPP2) succeeded
      case (Left(firstResponseModel), Right(secondResponseModel)) =>
        logPartialFailureOfMultipleAppeal(Left(firstResponseModel), Right(secondResponseModel), firstCorrelationId, secondCorrelationId, vrn, dateFrom, dateTo)
        sendAppealAudit(modelFromRequest, uploads, secondCorrelationId, isLPP, isMultipleAppeal = true, appealType, secondResponseModel.caseId.get, secondPenaltyNumber)
        sendAuditIfDuplicatesExist(uploads)
        logger.debug(s"[AppealService][multipleAppeal] - Second penalty was $secondResponseModel, first penalty was $firstResponseModel")
        Right((): Unit)
      //Both failed
      case _ =>
        logger.error(s"[AppealService][multipleAppeal] - Received unknown status code from connector:" +
          s" First response: $firstResponse, Second response: $secondResponse")
        Left(firstResponse.left.toOption.orElse(secondResponse.left.toOption).map(_.status).getOrElse(INTERNAL_SERVER_ERROR))
    }
  }

  private def sendAppealAudit(modelFromRequest: AppealSubmission,
                              uploads: Option[Seq[UploadJourney]],
                              correlationId: String,
                              isLPP: Boolean,
                              isMultipleAppeal: Boolean,
                              appealType: Option[PenaltyTypeEnum.Value],
                              caseId: String,
                              penaltyNumber: String)(implicit ec: ExecutionContext,
                                                                         headerCarrier: HeaderCarrier,
                                                                         userRequest: UserRequest[_]): Unit = {
    val startOfLog = s"[AppealService][${if (isMultipleAppeal) "multipleAppeal" else "singleAppeal"}]"
    if (isLPP) {
      if (appealType.contains(PenaltyTypeEnum.Late_Payment)) {
        logger.info(s"$startOfLog - Auditing first LPP appeal payload")
        auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.FirstLPP, correlationId, uploads, caseId, penaltyNumber))
      } else {
        logger.info(s"$startOfLog - Auditing second LPP appeal payload")
        auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.SecondLPP, correlationId, uploads, caseId, penaltyNumber))
      }
    } else {
      logger.info(s"$startOfLog - Auditing LSP appeal payload")
      auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LSP, correlationId, uploads, caseId, penaltyNumber))
    }
  }

  private def logPartialFailureOfMultipleAppeal(lpp1Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                lpp2Response: Either[ErrorResponse, AppealSubmissionResponseModel],
                                                firstCorrelationId: String, secondCorrelationId: String, vrn: String, dateFrom: String, dateTo: String): Unit = {
    val isSuccess = lpp1Response.exists(_.status == OK) && lpp2Response.exists(_.status == OK)
    if(!isSuccess) {
      val lpp1Message = lpp1Response match {
        case Right(model) if model.status == OK => s"LPP1 appeal was submitted successfully, case ID is ${model.caseId}. Correlation ID for LPP1: $firstCorrelationId. "
        case Right(model) if model.status == MULTI_STATUS => s"LPP1 appeal was submitted successfully (case ID is ${model.caseId}) but there was an issue storing the notification for uploaded files, response body (${model.error}). Correlation ID for LPP1: $firstCorrelationId. "
        case Left(model) => s"LPP1 appeal was not submitted successfully, Reason given ${model.body}. Correlation ID for LPP1: $firstCorrelationId. "
        case _ => throw new MatchError(s"[AppealService][logPartialFailureOfMultipleAppeal] - unknown lpp1 response $lpp1Response")
      }
      val lpp2Message = lpp2Response match {
        case Right(model) if model.status == OK => s"LPP2 appeal was submitted successfully, case ID is ${model.caseId}. Correlation ID for LPP2: $secondCorrelationId. "
        case Right(model) if model.status == MULTI_STATUS => s"LPP2 appeal was submitted successfully (case ID is ${model.caseId}) but there was an issue storing the notification for uploaded files, response body (${model.error}). Correlation ID for LPP2: $secondCorrelationId. "
        case Left(model) => s"LPP2 appeal was not submitted successfully, Reason given ${model.body}. Correlation ID for LPP2: $secondCorrelationId. "
        case _ => throw new MatchError(s"[AppealService][logPartialFailureOfMultipleAppeal] - unknown lpp2 response $lpp2Response")
      }
      logger.error(s"${PagerDutyKeys.MULTI_APPEAL_FAILURE} Multiple appeal covering $dateFrom-$dateTo for user with VRN $vrn failed. " + lpp1Message + lpp2Message)
    }
  }
}
