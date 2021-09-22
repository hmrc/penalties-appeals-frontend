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

import config.AppConfig
import connectors.UpscanConnector
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import javax.inject.Inject
import models.upload.{UploadJourney, UploadStatusEnum}
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanController @Inject()(repository: UploadJourneyRepository, connector: UpscanConnector)
                                (implicit appConfig: AppConfig, mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def getStatusOfFileUpload(journeyId: String, fileReference: String): Action[AnyContent] = Action.async {
    implicit request => {
      logger.debug(s"[UpscanController][getStatusOfFileUpload] - File upload status requested for journey: $journeyId with file reference: $fileReference")
      repository.getStatusOfFileUpload(journeyId, fileReference).map(
        _.fold({
          logger.error(s"[UpscanController][getStatusOfFileUpload] - File upload status was not found for journey: $journeyId with file reference: $fileReference")
          NotFound(s"File $fileReference in journey $journeyId did not exist.")
        })(
          fileStatus => {
            logger.debug(s"[UpscanController][getStatusOfFileUpload] - Found status for journey: $journeyId with file reference: $fileReference - returning status: $fileStatus")
            Ok(Json.toJson(fileStatus))
          }
        )
      )
    }
  }

  def initiateCallToUpscan(journeyId: String): Action[AnyContent] = Action.async {
    implicit request => {
      logger.debug(s"[UpscanController][initiateCallToUpscan] - Initiating Call to Upscan for journey: $journeyId")
      val initiateRequest = UpscanInitiateRequest(appConfig.upscanCallbackBaseUrl + controllers.internal.routes.UpscanCallbackController.callbackFromUpscan(journeyId).url)
      connector.initiateToUpscan(initiateRequest).flatMap(
        response => {
          response.fold(
            error => Future(InternalServerError(s"[UpscanController][initiateCallToUpscan] - Failed to map response for journey: $journeyId")),
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
}
