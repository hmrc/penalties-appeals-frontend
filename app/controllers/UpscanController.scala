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

package controllers

import config.{AppConfig, ErrorHandler}
import connectors.UpscanConnector
import forms.upscan.{S3UploadErrorForm, S3UploadSuccessForm}
import helpers.UpscanMessageHelper
import models.Mode
import models.upload._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import java.time.LocalDateTime

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanController @Inject()(repository: UploadJourneyRepository,
                                 connector: UpscanConnector,
                                 service: UpscanService)
                                (implicit appConfig: AppConfig,
                                 errorHandler: ErrorHandler,
                                 mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def getStatusOfFileUpload(journeyId: String, fileReference: String): Action[AnyContent] = Action.async {
    implicit request => {
      logger.debug(s"[UpscanController][getStatusOfFileUpload] - File upload status requested for journey: $journeyId with file reference: $fileReference")
      repository.getStatusOfFileUpload(journeyId, fileReference).map(
        _.fold({
          logger.error(
            s"[UpscanController][getStatusOfFileUpload] - File upload status was not found for journey: $journeyId with file reference: $fileReference")
          NotFound(s"File $fileReference in journey $journeyId did not exist.")
        })(
          fileStatus => {
            val localisedMessageOpt = fileStatus.errorMessage.map(UpscanMessageHelper.applyMessage(_))
            val fileStatusWithLocalisedMessage = fileStatus.copy(errorMessage = localisedMessageOpt)
            logger.debug(s"[UpscanController][getStatusOfFileUpload] - Found status for journey: $journeyId with file " +
              s"reference: $fileReference - returning status: $fileStatusWithLocalisedMessage with message: ${fileStatusWithLocalisedMessage.errorMessage}")
            Ok(Json.toJson(fileStatusWithLocalisedMessage))
          }
        )
      )
    }
  }

  def initiateCallToUpscan(journeyId: String): Action[AnyContent] = Action.async {
    implicit request => {
      logger.debug(s"[UpscanController][initiateCallToUpscan] - Initiating Call to Upscan for journey: $journeyId")
      val initiateRequestForMultiFileUpload = UpscanInitiateRequest(
        callbackUrl = appConfig.upscanCallbackBaseUrl + controllers.internal.routes.UpscanCallbackController.callbackFromUpscan(journeyId).url,
        successRedirect = Some(appConfig.upscanSuccessUrl + journeyId),
        errorRedirect = Some(appConfig.upscanFailureUrl + journeyId),
        minimumFileSize = Some(1),
        maximumFileSize = Some(appConfig.maxFileUploadSize))
      connector.initiateToUpscan(initiateRequestForMultiFileUpload).flatMap(
        response => {
          response.fold(
            error => {
              logger.error(s"[UpscanController][initiateCallToUpscan] - Upscan call failure with status: ${error.status} and body: ${error.body}")
              logger.error(s"[UpscanController][initiateCallToUpscan] - Failed to map response for journey: $journeyId")
              Future(InternalServerError("An exception has occurred."))
            },
            responseModel => {
              logger.debug(s"[UpscanController][initiateCallToUpscan] - Retrieving response model for journey: $journeyId")
              val uploadJourney = UploadJourney(responseModel.reference, UploadStatusEnum.WAITING)
              repository.updateStateOfFileUpload(journeyId, uploadJourney).map(
                _ => {
                  Ok(Json.toJson(responseModel)(UpscanInitiateResponseModel.formats))
                }
              )
            }
          )
        }
      )
    }
  }

  def removeFile(journeyId: String, fileReference: String): Action[AnyContent] = Action.async {
    logger.debug(s"[UpscanController][initiateCallToUpscan] - Requested removal of file for journey: $journeyId and file: $fileReference")
    repository.removeFileForJourney(journeyId, fileReference).map {
      _ => NoContent
    }.recover {
      case e =>
        logger.error(s"[UpscanController][initiateCallToUpscan] - Failed to delete file: $fileReference for journey: $journeyId with error: ${e.getMessage}")
        InternalServerError("An exception has occurred.")
    }
  }

  def uploadFailure(journeyId: String): Action[AnyContent] = Action.async {
    implicit request => {
      logger.error(s"[UpscanController][uploadFailure] - Error redirect initiated for journey: $journeyId")
      S3UploadErrorForm.form.bindFromRequest().fold(
        error => {
          logger.error(s"[UpscanController][uploadFailure] - Failed to parse S3 upload error with errors: ${error.errors}")
          Future(BadRequest(""))
        },
        s3UploadError => {
          val messageKeyToSet = UpscanMessageHelper.getUploadFailureMessage(s3UploadError.errorCode)
          val fileReference = s3UploadError.key
          logger.debug(s"[UpscanController][uploadFailure] - Setting $messageKeyToSet as the message key for file: $fileReference")
          val callbackModel: UploadJourney = UploadJourney(
            reference = fileReference,
            fileStatus = UploadStatusEnum.FAILED,
            downloadUrl = None,
            uploadDetails = None,
            failureDetails = Some(
              FailureDetails(
                failureReason = FailureReasonEnum.REJECTED,
                message = messageKeyToSet
              )
            )
          )
          repository.updateStateOfFileUpload(journeyId, callbackModel).map(_ => NoContent.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"))
        }
      )
    }
  }

  def preFlightUpload(journeyId: String): Action[AnyContent] = Action {
    Created.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*")
  }

  def filePosted(journeyId: String): Action[AnyContent] = Action.async {
    implicit request => {
      S3UploadSuccessForm.upscanUploadSuccessForm.bindFromRequest.fold(
        errors => {
          logger.error(s"[UpscanController][filePosted] - Could not bind form based on request with errors: ${errors.errors}")
          Future(BadRequest(""))
        },
        s3Upload => {
          val uploadModel: UploadJourney = UploadJourney(
            reference = s3Upload.key,
            fileStatus = UploadStatusEnum.WAITING
          )
          repository.getUploadsForJourney(Some(journeyId)).map(_.flatMap(_.find(_.reference == s3Upload.key).map(_.lastUpdated))).flatMap(
            lastUpdated => {
              if (lastUpdated.isDefined && LocalDateTime.now().isAfter(lastUpdated.get.plusSeconds(1))) {
                logger.debug(s"[UpscanController][filePosted] - Success redirect called: \n" +
                  s"lastUpdatedTime is: $lastUpdated \n is after 1 second: true \n setting upload to WAITING")
                repository.updateStateOfFileUpload(journeyId, uploadModel).map(_ => NoContent.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"))
              } else {
                logger.debug(s"[UpscanController][filePosted] - Success redirect called - did not update state of file upload - last updated defined: ${lastUpdated.isDefined}")
                Future(NoContent.withHeaders(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> "*"))
              }
            }
          )
        }
      )
    }
  }

  def preUpscanCheckFailed(isAddingAnotherDocument: Boolean, mode: Mode): Action[AnyContent] = Action {
    implicit request => {
      if(isAddingAnotherDocument) {
        Redirect(controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(mode))
          .addingToSession(SessionKeys.errorCodeFromUpscan -> request.getQueryString("errorCode").get)
      } else {
        Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
          .addingToSession(SessionKeys.errorCodeFromUpscan -> request.getQueryString("errorCode").get)
      }
    }
  }

  def fileVerification(isAddingAnotherDocument: Boolean, mode: Mode): Action[AnyContent] = Action.async {
    implicit request => {
      S3UploadSuccessForm.upscanUploadSuccessForm.bindFromRequest.fold(
        errors => {
          logger.error(s"[UpscanController][filePosted] - Could not bind form based on request with errors: ${errors.errors}")
          Future(errorHandler.showInternalServerError)
        },
        upload => {
          val timeoutForCheckingStatus = System.nanoTime() + (appConfig.upscanStatusCheckTimeout * 1000000000L)
          service.waitForStatus(request.session.get(SessionKeys.journeyId).get, upload.key, timeoutForCheckingStatus, mode, isAddingAnotherDocument,{
            (optFailureDetails, errorMessage) => {
              if(errorMessage.isDefined) {
                val failureReason = UpscanMessageHelper.getLocalisedFailureMessageForFailure(optFailureDetails.get.failureReason)
                if(isAddingAnotherDocument) {
                  Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(mode))
                    .addingToSession(SessionKeys.failureMessageFromUpscan -> failureReason))
                } else {
                  Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
                    .addingToSession(SessionKeys.failureMessageFromUpscan -> failureReason))
                }
              } else {
                Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadComplete()))
              }
            }
          })
        }
      )

    }
  }
}
