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

package controllers.internal

import helpers.UpscanMessageHelper
import models.upload.{UploadJourney, UploadStatusEnum}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UpscanCallbackController @Inject()(repository: UploadJourneyRepository,
                                         service: UpscanService)
                                        (implicit mcc: MessagesControllerComponents, ec: ExecutionContext) extends FrontendController(mcc) {

  def callbackFromUpscan(journeyId: String, isJsEnabled: Boolean): Action[JsValue] = Action.async(parse.json) {
    implicit request => {
      service.scheduleCallbackOperation {
        withJsonBody[UploadJourney] {
          callbackModel => {
            if (callbackModel.failureDetails.isDefined) {
              logger.debug("[UpscanCallbackController][callbackFromUpscan] - Callback received and upload failed, marking failure in repository")
              val failureReason = callbackModel.failureDetails.get.failureReason
              val localisedFailureReason = UpscanMessageHelper.getLocalisedFailureMessageForFailure(failureReason, isJsEnabled)
              val failureDetails = callbackModel.failureDetails.get.copy(message = localisedFailureReason)
              repository.updateStateOfFileUpload(journeyId, callbackModel.copy(failureDetails = Some(failureDetails))).map(_ => NoContent)
            } else {
              repository.getAllChecksumsForJourney(Some(journeyId)).flatMap(
                seqOfChecksums => {
                  repository.getFieldsForFileReference(journeyId, callbackModel.reference).flatMap(
                    fields =>
                      if (seqOfChecksums.contains(callbackModel.uploadDetails.get.checksum)) {
                        logger.debug("[UpscanCallbackController][callbackFromUpscan] - Checksum is already in Mongo. Marking file as duplicate.")
                        val duplicateCallbackModel = callbackModel.copy(
                          fileStatus = UploadStatusEnum.DUPLICATE,
                          uploadFields = fields
                        )
                        repository.updateStateOfFileUpload(journeyId, duplicateCallbackModel).map(_ => NoContent)
                      } else {
                        logger.debug("[UpscanCallbackController][callbackFromUpscan] - Callback received and upload was successful, marking success in repository")
                        repository.updateStateOfFileUpload(journeyId, callbackModel.copy(uploadFields = fields)).map(_ => NoContent)
                      }
                  )
                }
              )
            }
          }
        }
      }
    }
  }
}
