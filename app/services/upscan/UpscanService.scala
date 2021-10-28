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

package services.upscan

import config.{AppConfig, ErrorHandler}
import connectors.UpscanConnector
import connectors.httpParsers.UpscanInitiateHttpParser
import models.upload.{FailureDetails, UploadJourney, UploadStatusEnum, UpscanInitiateRequest, UpscanInitiateResponseModel}
import play.api.mvc.{Request, Result}
import repositories.UploadJourneyRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanService @Inject()(uploadJourneyRepository: UploadJourneyRepository,
                              upscanConnector: UpscanConnector)(implicit appConfig: AppConfig, errorHandler: ErrorHandler) {

  private lazy val upscanInitiateModelForSynchronousUpload: String => UpscanInitiateRequest = (journeyId: String) => UpscanInitiateRequest(
    callbackUrl = appConfig.upscanCallbackBaseUrl + controllers.internal.routes.UpscanCallbackController.callbackFromUpscan(journeyId).url,
    successRedirect = Some(appConfig.upscanBaseUrl + controllers.routes.UpscanController.fileVerification().url),
    errorRedirect = Some(appConfig.upscanBaseUrl + controllers.routes.UpscanController.preUpscanCheckFailed().url),
    minimumFileSize = Some(1),
    maximumFileSize = Some(appConfig.maxFileUploadSize)
  )

  def initiateSynchronousCallToUpscan(journeyId: String)(implicit ec: ExecutionContext,
                                                         hc: HeaderCarrier): Future[Either[UpscanInitiateHttpParser.ErrorResponse, UpscanInitiateResponseModel]] = {
    val initiateRequestModel = upscanInitiateModelForSynchronousUpload(journeyId)
    upscanConnector.initiateToUpscan(initiateRequestModel).flatMap {
      _.fold(
        error => {
          logger.error(s"[UpscanService][initiateSynchronousCallToUpscan] - Initiate call to Upscan failed with error: ${error.body} and status: ${error.status}")
          Future(Left(error))
        },
        responseModel => {
          logger.debug(s"[UpscanController][initiateCallToUpscan] - Retrieving response model for journey: $journeyId")
          val uploadJourney = UploadJourney(responseModel.reference, UploadStatusEnum.WAITING)
          uploadJourneyRepository.updateStateOfFileUpload(journeyId, uploadJourney).map {
            _ => Right(responseModel)
          }
        }
      )
    }
  }

  def waitForStatus(journeyId: String,
                    file: String,
                    timeoutNano: Long,
                    block: (Option[FailureDetails], Option[String]) => Future[Result])
                   (implicit request: Request[_], ec: ExecutionContext): Future[Result] = {
    uploadJourneyRepository.getStatusOfFileUpload(journeyId, file).flatMap {
      _.fold(
        Future(errorHandler.showInternalServerError)
      )(
        uploadStatus => {
          logger.debug("waitForStatus - running")
          if(uploadStatus.status != "WAITING") {
            uploadJourneyRepository.getUploadsForJourney(Some(journeyId)).flatMap {
              uploads => {
                val uploadForFile = uploads.get.find(_.reference == file).get
                block(uploadForFile.failureDetails, uploadStatus.errorMessage)
              }
            }
          } else if(System.nanoTime() > timeoutNano) {
            logger.debug("waitForStatus - skipping check")
            Future(errorHandler.showInternalServerError)
          } else {
            waitForStatus(journeyId, file, timeoutNano, block)
          }
        }
      )
    }
  }
}
