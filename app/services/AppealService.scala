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

import java.time.LocalDate
import config.AppConfig
import config.featureSwitches.FeatureSwitching
import connectors.PenaltiesConnector
import helpers.DateTimeHelper

import javax.inject.Inject
import models.appeals.{AppealSubmission, MultiplePenaltiesData}
import models.monitoring.{AppealAuditModel, AuditPenaltyTypeEnum, DuplicateFilesAuditModel}
import models.upload.{UploadJourney, UploadStatusEnum}
import models._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsResult, JsValue, Json}
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.Logger.logger
import utils.{EnrolmentKeys, SessionKeys, UUIDGenerator}

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
    val correlationId: String = idGenerator.generateUUID
    val userHasUploadedFiles = userRequest.answers.getAnswer[String](SessionKeys.isUploadEvidence).contains("yes")
    if (!userRequest.answers.getAnswer[String](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString) && userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes")) {
      multipleAppeal(enrolmentKey, appealType, isLPP, correlationId, agentReferenceNo, userHasUploadedFiles, reasonableExcuse)
    } else {
      singleAppeal(enrolmentKey, appealType, isLPP, correlationId, agentReferenceNo, userHasUploadedFiles, reasonableExcuse)
    }
  }

  private def singleAppeal(enrolmentKey: String, appealType: Option[PenaltyTypeEnum.Value], isLPP: Boolean,
                           correlationId: String, agentReferenceNo: Option[String], userHasUploadedFiles: Boolean,
                           reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file =>
        file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      uploads = if (userHasUploadedFiles) readyOrDuplicateFileUploads else None
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isAppealLate, agentReferenceNo, uploads)
      penaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.penaltyNumber).get
      response <- {
        penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, penaltyNumber, correlationId, isMultiAppeal = false)
      }
    } yield {
      response.status match {
        case OK =>
          sendAppealAudit(modelFromRequest, uploads, correlationId, isLPP, isMultipleAppeal = false, appealType)
          sendAuditIfDuplicatesExist(uploads)
          logger.info("[AppealService][singleAppeal] - Received OK from the appeal submission call")
          Right((): Unit)
        case _ =>
          logger.error(s"[AppealService][singleAppeal] - Received unknown status code from connector: ${response.status}")
          Left(response.status)
      }
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
                             correlationId: String, agentReferenceNo: Option[String], userHasUploadedFiles: Boolean,
                             reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Either[Int, Unit]] = {
    for {
      fileUploads <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      readyOrDuplicateFileUploads = fileUploads.map(_.filter(file =>
        file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
      uploads = if (userHasUploadedFiles) readyOrDuplicateFileUploads else None
      modelFromRequest: AppealSubmission = AppealSubmission
        .constructModelBasedOnReasonableExcuse(reasonableExcuse, isAppealLate, agentReferenceNo, uploads)
      firstPenaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.firstPenaltyChargeReference).get
      secondPenaltyNumber = userRequest.answers.getAnswer[String](SessionKeys.secondPenaltyChargeReference).get
      firstResponse <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, firstPenaltyNumber, correlationId, isMultiAppeal = true)
      secondResponse <- penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP, secondPenaltyNumber, correlationId, isMultiAppeal = true)
      vrn = userRequest.vrn
      dateFrom = userRequest.answers.getAnswer[String](SessionKeys.startDateOfPeriod).get
      dateTo = userRequest.answers.getAnswer[String](SessionKeys.endDateOfPeriod).get
    } yield {
      handleMultipleAppealResponse(firstResponse, secondResponse, isLPP, appealType, modelFromRequest, uploads, vrn, dateFrom, dateTo, correlationId)
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

  private def handleMultipleAppealResponse(firstResponse: HttpResponse,
                                           secondResponse: HttpResponse,
                                           isLPP: Boolean,
                                           appealType: Option[PenaltyTypeEnum.Value],
                                           modelFromRequest: AppealSubmission,
                                           uploads: Option[Seq[UploadJourney]],
                                           vrn: String,
                                           dateFrom: String,
                                           dateTo: String,
                                           correlationId: String)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, userRequest: UserRequest[_]) = {
    (firstResponse.status, secondResponse.status) match {
      case (OK, OK) =>
        sendAppealAudit(modelFromRequest, uploads, correlationId, isLPP, isMultipleAppeal = true, appealType)
        sendAuditIfDuplicatesExist(uploads)
        logger.debug("[AppealService][multipleAppeal] - Received OK from the appeal submission call")
        Right((): Unit)
      case (MULTI_STATUS, MULTI_STATUS) =>
        logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, vrn, dateFrom, dateTo)(
          didLPP1SubmissionSucceed = true,
          didLPP1DocumentUploadSucceed = false,
          didLPP2SubmissionSucceed = true,
          didLPP2DocumentUploadSucceed = false)
        sendAppealAudit(modelFromRequest, uploads, correlationId, isLPP, isMultipleAppeal = true, appealType)
        sendAuditIfDuplicatesExist(uploads)
        logger.debug("[AppealService][multipleAppeal] - Both penalties were appealed successfully, but the uploads had issues")
        Right((): Unit)
      case (OK | MULTI_STATUS, _) =>
        logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, vrn, dateFrom, dateTo)(
          didLPP1SubmissionSucceed = true,
          didLPP1DocumentUploadSucceed = firstResponse.status == OK,
          didLPP2SubmissionSucceed = false,
          didLPP2DocumentUploadSucceed = false)
        sendAppealAudit(modelFromRequest, uploads, correlationId, isLPP, isMultipleAppeal = true, appealType)
        sendAuditIfDuplicatesExist(uploads)
        logger.debug("[AppealService][multipleAppeal] - First penalty was appealed successfully, second penalty had issues")
        Right((): Unit)
      case (_, OK | MULTI_STATUS) =>
        logPartialFailureOfMultipleAppeal(firstResponse, secondResponse, vrn, dateFrom, dateTo)(
          didLPP1SubmissionSucceed = false,
          didLPP1DocumentUploadSucceed = false,
          didLPP2SubmissionSucceed = true,
          didLPP2DocumentUploadSucceed = secondResponse.status == OK)
        logger.debug("[AppealService][multipleAppeal] - Second penalty was appealed successfully, first penalty had issues")
        Right((): Unit)
      case _ =>
        logger.error(s"[AppealService][multipleAppeal] - Received unknown status code from connector:" +
          s" First response: ${firstResponse.status}, Second response: ${secondResponse.status}")
        Left(firstResponse.status)
    }
  }

  private def sendAppealAudit(modelFromRequest: AppealSubmission,
                              uploads: Option[Seq[UploadJourney]],
                              correlationId: String,
                              isLPP: Boolean,
                              isMultipleAppeal: Boolean,
                              appealType: Option[PenaltyTypeEnum.Value])(implicit ec: ExecutionContext,
                                                                         headerCarrier: HeaderCarrier,
                                                                         userRequest: UserRequest[_]): Unit = {
    val startOfLog = s"[AppealService][${if (isMultipleAppeal) "multipleAppeal" else "singleAppeal"}"
    if (isLPP) {
      if (appealType.contains(PenaltyTypeEnum.Late_Payment)) {
        logger.info(s"$startOfLog - Auditing first LPP appeal payload")
        auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.FirstLPP, correlationId, uploads))
      } else {
        logger.info(s"$startOfLog - Auditing second LPP appeal payload")
        auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.SecondLPP, correlationId, uploads))
      }
    } else {
      logger.info(s"$startOfLog - Auditing LSP appeal payload")
      auditService.audit(AppealAuditModel(modelFromRequest, AuditPenaltyTypeEnum.LSP, correlationId, uploads))
    }
  }

  private def logPartialFailureOfMultipleAppeal(lpp1Response: HttpResponse, lpp2Response: HttpResponse,
                                                vrn: String, dateFrom: String, dateTo: String)(
    didLPP1SubmissionSucceed: Boolean,
    didLPP1DocumentUploadSucceed: Boolean,
    didLPP2SubmissionSucceed: Boolean,
    didLPP2DocumentUploadSucceed: Boolean): Unit = {

    val lpp1Message = (didLPP1SubmissionSucceed, didLPP1DocumentUploadSucceed) match {
      case (true, true) => s"LPP1 appeal was submitted successfully, Case Id is ${lpp1Response.body}. "
      case (true, false) => s"LPP1 appeal was submitted successfully but there was an issue storing the notification for uploaded files, response body (${lpp1Response.body}). "
      case (false, _) => s"LPP1 appeal was not submitted successfully, Reason given ${lpp1Response.body}. "
    }
    val lpp2Message = (didLPP2SubmissionSucceed, didLPP2DocumentUploadSucceed) match {
      case (true, true) => s"LPP2 appeal was submitted successfully, Case Id is ${lpp2Response.body}."
      case (true, false) => s"LPP2 appeal was submitted successfully but there was an issue storing the notification for uploaded files, response body (${lpp2Response.body})."
      case (false, _) => s"LPP2 appeal was not submitted successfully, Reason given ${lpp2Response.body}."
    }

    logger.error(s"MULTI_APPEAL_FAILURE Multiple appeal covering $dateFrom-$dateTo for user with VRN $vrn failed. " + lpp1Message + lpp2Message)
  }
}
