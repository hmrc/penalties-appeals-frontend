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

package services.upscan

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import config.{AppConfig, ErrorHandler}
import connectors.UpscanConnector
import connectors.httpParsers.ErrorResponse
import models.Mode
import models.upload._
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.{PagerDutyHelper, SessionKeys}

import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class UpscanService @Inject()(uploadJourneyRepository: UploadJourneyRepository,
                              upscanConnector: UpscanConnector,
                              scheduler: ActorSystem)(implicit appConfig: AppConfig, errorHandler: ErrorHandler) {

  def upscanInitiateModelForSynchronousUpload(journeyId: String, isAddingAnotherDocument: Boolean, mode: Mode, isJsEnabled: Boolean = false): UpscanInitiateRequest = {
    UpscanInitiateRequest(
      callbackUrl = appConfig.upscanCallbackBaseUrl + controllers.internal.routes.UpscanCallbackController.callbackFromUpscan(journeyId, isJsEnabled).url,
      successRedirect = Some(appConfig.upscanBaseUrl + controllers.routes.UpscanController.fileVerification(isAddingAnotherDocument, mode, isJsEnabled).url),
      errorRedirect = Some(appConfig.upscanBaseUrl + controllers.routes.UpscanController.preUpscanCheckFailed(isAddingAnotherDocument, mode).url),
      minimumFileSize = Some(1),
      maximumFileSize = Some(appConfig.maxFileUploadSize)
    )
  }

  def initiateSynchronousCallToUpscan(journeyId: String, isAddingAnotherDocument: Boolean, mode: Mode)(implicit ec: ExecutionContext,
                                                         hc: HeaderCarrier): Future[Either[ErrorResponse, UpscanInitiateResponseModel]] = {
    val initiateRequestModel = upscanInitiateModelForSynchronousUpload(journeyId, isAddingAnotherDocument, mode)
    upscanConnector.initiateToUpscan(initiateRequestModel).flatMap {
      _.fold(
        error => {
          PagerDutyHelper.logStatusCode("initiateSynchronousCallToUpscan", error.status)(RECEIVED_4XX_FROM_UPSCAN, RECEIVED_5XX_FROM_UPSCAN)
          logger.error(s"[UpscanService][initiateSynchronousCallToUpscan] - Initiate call to Upscan failed with error: ${error.body} and status: ${error.status}")
          Future(Left(error))
        },
        responseModel => {
          logger.info(s"[UpscanController][initiateSynchronousCallToUpscan] - Retrieving response model for journey: $journeyId")
          val uploadJourney = UploadJourney(responseModel.reference, UploadStatusEnum.WAITING,
            uploadFields = Some(responseModel.uploadRequest.fields))
          uploadJourneyRepository.updateStateOfFileUpload(journeyId, uploadJourney, isInitiateCall = true).map {
            _ => {
              logger.info(s"[UpscanController][initiateSynchronousCallToUpscan] - Stored init state of file upload for journey: $journeyId")
              Right(responseModel)
            }
          }
        }
      )
    }
  }

  def waitForStatus(journeyId: String,
                    fileReference: String,
                    timeoutNano: Long,
                    mode: Mode,
                    isAddingAnotherDocument: Boolean,
                    block: (Option[FailureDetails], Option[String]) => Future[Result])
                   (implicit request: Request[_], ec: ExecutionContext): Future[Result] = {
    uploadJourneyRepository.getStatusOfFileUpload(journeyId, fileReference).flatMap {
      _.fold({
        logger.error(s"[UpscanService][waitForStatus] - No upload found for journey: $journeyId and file: $fileReference")
        Future(errorHandler.showInternalServerError())
      })(
        uploadStatus => {
          logger.info(s"[UpscanService][waitForStatus] - Running status check for upload for journey: $journeyId and file: $fileReference")
          if(uploadStatus.status != "WAITING") {
            logger.info(s"[UpscanService][waitForStatus] - Upload callback received for journey: $journeyId and file: $fileReference")
            uploadJourneyRepository.getUploadsForJourney(Some(journeyId)).flatMap {
              uploads => {
                val uploadForFile = uploads.get.find(_.reference == fileReference).get
                block(uploadForFile.failureDetails, uploadStatus.errorMessage)
              }
            }
          } else if(System.nanoTime() > timeoutNano) {
            logger.warn("[UpscanService][waitForStatus] - Checking for completed upload status timed out - rendering 'taking longer than expected' page")
            Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(mode)).addingToSession(
              SessionKeys.fileReference -> fileReference,
              SessionKeys.isAddingAnotherDocument -> s"$isAddingAnotherDocument"
            ))
          } else {
            logger.info(s"[UpscanService][waitForStatus] - Upload still in waiting state - waiting ${appConfig.upscanStatusCheckDelay}ms before running check again for journey: $journeyId and file: $fileReference")
            after(duration = FiniteDuration(appConfig.upscanStatusCheckDelay, "ms"), using = scheduler.scheduler)(
              waitForStatus(journeyId, fileReference, timeoutNano, mode, isAddingAnotherDocument, block)
            )
          }
        }
      )
    }
  }

  def scheduleCallbackOperation(block: => Future[Result])(implicit ec: ExecutionContext): Future[Result] = {
    if(appConfig.upscanCallbackDelayEnabled) {
      after(duration = FiniteDuration(appConfig.upscanCallbackUpdateDelay, "ms"), using = scheduler.scheduler)(
        block
      )
    } else {
      block
    }
  }

  def removeFileFromJourney(journeyId: String, fileReference: String): Future[Unit] = {
    uploadJourneyRepository.removeFileForJourney(journeyId, fileReference)
  }

  def getFileNameForJourney(journeyId: String, fileReference: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    uploadJourneyRepository.getFileForJourney(journeyId, fileReference)
      .map(_.flatMap(_.uploadDetails.map(_.fileName)))
  }

  def getAmountOfFilesUploadedForJourney(journeyId: String)(implicit ec: ExecutionContext): Future[Int] = {
    uploadJourneyRepository.getUploadsForJourney(Some(journeyId)).map(
      _.fold(0)(_.count(file => file.fileStatus == UploadStatusEnum.READY))
    )
  }
}
