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

package connectors

import config.AppConfig
import models.AppealData
import play.api.Logger.logger
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.EnrolmentKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   appConfig: AppConfig) {
  def getAppealsDataForPenalty(penaltyId: String, enrolmentKey: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getAppealsDataForPenalty] -"
    httpClient.GET[HttpResponse](
      appConfig.appealDataForPenaltyAndEnrolmentKey(
        penaltyId,
        EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey)
      )
    ).map {
      response => response.status match {
        case OK => {
          logger.debug(s"$startOfLogMsg OK response returned from Penalties backend for penalty with ID: $penaltyId and enrolment key $enrolmentKey")
          Some(response.json)
        }
        case NOT_FOUND => {
          logger.info(s"$startOfLogMsg Returned 404 from Penalties backend - with body: ${response.body}")
          None
        }
        case _ => {
          logger.warn(s"$startOfLogMsg Returned unknown response ${response.status} with body: ${response.body}")
          None
        }
      }
    }.recover {
      case e => {
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
      }
    }
  }
}
