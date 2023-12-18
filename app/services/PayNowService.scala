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
import connectors.PayNowConnector
import connectors.httpParsers.ErrorResponse
import javax.inject.Inject
import models.payApi.PayNowRequestModel
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


class PayNowService @Inject()(payNowConnector: PayNowConnector,
                              appConfig: AppConfig
                             ) {

  def retrieveRedirectUrl(vrn:String, chargeReference:String, vatAmount: BigDecimal, dueDate:LocalDate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorResponse, String]] = {

      val returnUrl = s"${appConfig.platformPenaltiesFrontendHost}${controllers.findOutHowToAppeal.routes.WaitForPaymentToClearController.onPageLoad().url}"
      val backUrl = s"${appConfig.platformPenaltiesFrontendHost}${controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad().url}"
      val requestModel = PayNowRequestModel(vrn, chargeReference, (vatAmount * 100).toInt, dueDate, returnUrl, backUrl)

      payNowConnector.setupJourney(requestModel).map {
        case Right(url) => Right(url.nextUrl)
        case Left(errorResponse) => Left(errorResponse)
      }
    }


}

