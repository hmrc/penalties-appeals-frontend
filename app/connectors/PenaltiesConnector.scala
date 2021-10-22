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
import models.appeals.AppealSubmission
import utils.Logger.logger
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException, NotFoundException}
import uk.gov.hmrc.http.HttpReads.Implicits._
import utils.EnrolmentKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   appConfig: AppConfig, headerGenerator: HeaderGenerator) {

 def getAppealUrlBasedOnPenaltyType(penaltyId: String, enrolmentKey: String, isLPP: Boolean,isAdditional:Boolean): String = {
    if (isLPP) {
      appConfig.appealLPPDataForPenaltyAndEnrolmentKey(penaltyId, EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey),isAdditional)
    } else appConfig.appealLSPDataForPenaltyAndEnrolmentKey(penaltyId, EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey))
  }

  def getAppealsDataForPenalty(penaltyId: String, enrolmentKey: String, isLPP: Boolean,isAdditional:Boolean)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getAppealsDataForPenalty] -"
    httpClient.GET[HttpResponse](
      getAppealUrlBasedOnPenaltyType(penaltyId, enrolmentKey, isLPP,isAdditional)
    ).map {
      response =>
        response.status match {
          case OK =>
            logger.debug(s"$startOfLogMsg OK response returned from Penalties backend for penalty with ID: $penaltyId and enrolment key $enrolmentKey")
            Some(response.json)
          case NOT_FOUND =>
            logger.info(s"$startOfLogMsg Returned 404 from Penalties backend - with body: ${response.body}")
            None
          case _ =>
            logger.warn(s"$startOfLogMsg Returned unknown response ${response.status} with body: ${response.body}")
            None
        }
    }.recover {
      case e =>
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
    }
  }

  def getListOfReasonableExcuses()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getListOfReasonableExcuses] -"
    httpClient.GET[HttpResponse](
      appConfig.reasonableExcuseFetchUrl
    ).map(
      response => Some(response.json)
    ).recover {
      case notFoundException: NotFoundException =>
        logger.error(s"$startOfLogMsg Returned 404 from penalties. With message: ${notFoundException.getMessage}")
        None
      case internalServerException: InternalServerException =>
        logger.error(s"$startOfLogMsg Returned 500 from penalties. With message: ${internalServerException.getMessage}")
        None
      case e =>
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
    }
  }

  def submitAppeal(appealSubmission: AppealSubmission, enrolmentKey: String, isLPP: Boolean, penaltyId: String)
                  (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[HttpResponse] = {
    val hcWithoutAuthorizationHeader: HeaderCarrier = hc.copy(authorization = None)
    val pegaHeaders = headerGenerator.headersForPEGA()
    httpClient.POST[AppealSubmission, HttpResponse](appConfig.submitAppealUrl(enrolmentKey, isLPP, penaltyId),
      appealSubmission, pegaHeaders)(AppealSubmission.writes, implicitly, hcWithoutAuthorizationHeader, implicitly)
  }

  def getOtherPenaltiesInTaxPeriod(penaltyId: String, enrolmentKey: String, isLPP: Boolean)
                                  (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient.GET[HttpResponse](appConfig.otherPenaltiesForPeriodUrl(penaltyId, EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey), isLPP))
  }
}
