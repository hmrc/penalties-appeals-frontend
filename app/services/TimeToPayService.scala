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

import connectors.TimeToPayConnector
import connectors.httpParsers.ErrorResponse
import javax.inject.Inject
import models.ess.TimeToPayRequestModel
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TimeToPayService @Inject()(ttpConnector: TimeToPayConnector) {

  def retrieveRedirectUrl(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorResponse, String]] = {
    val returnUrl = controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanProcessedController.onPageLoad().url
    val backUrl = controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad().url
    val requestModel = TimeToPayRequestModel(returnUrl, backUrl)

    ttpConnector.setupJourney(requestModel).map {
      case Right(url) => Right(url.nextUrl)
      case Left(errorResponse) => Left(errorResponse)
    }
  }

}
