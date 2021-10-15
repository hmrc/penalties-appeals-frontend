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

package controllers.internal

import config.AppConfig
import helpers.UpscanMessageHelper

import javax.inject.Inject
import models.upload.{FailureReasonEnum, UploadJourney}
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.mvc.{Action, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

class UpscanCallbackController @Inject()(repository: UploadJourneyRepository)
                                        (implicit appConfig: AppConfig,
                                         mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def callbackFromUpscan(journeyId: String): Action[JsValue] =
    Action.async(parse.json) { implicit request => {
        withJsonBody[UploadJourney] { callbackModel => {
          if(callbackModel.failureDetails.isDefined) {
            val failureReason = callbackModel.failureDetails.get.failureReason
            val localisedFailureReason = UpscanMessageHelper.getLocalisedFailureMessageForFailure(failureReason)
            val failureDetails = callbackModel.failureDetails.get.copy(message = localisedFailureReason)
            repository.updateStateOfFileUpload(journeyId, callbackModel.copy(failureDetails = Some(failureDetails))).map(_ => NoContent)
          } else {
            repository.updateStateOfFileUpload(journeyId, callbackModel).map(_ => NoContent)
          }
        }
        }
      }
    }
}
