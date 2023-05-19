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

package connectors

import config.AppConfig
import connectors.httpParsers.AppealSubmissionHTTPParser.{AppealSubmissionReads, AppealSubmissionResponse}
import connectors.httpParsers.MultiplePenaltiesHttpParser.{MultiplePenaltiesResponse, MultiplePenaltiesResponseReads}
import connectors.httpParsers.UnexpectedFailure
import models.appeals.AppealSubmission
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException, NotFoundException}
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.{EnrolmentKeys, PagerDutyHelper}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   appConfig: AppConfig) {

  def getAppealUrlBasedOnPenaltyType(penaltyId: String, enrolmentKey: String, isLPP: Boolean, isAdditional: Boolean): String = {
    if (isLPP) {
      appConfig.appealLPPDataForPenaltyAndEnrolmentKey(penaltyId, EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey), isAdditional)
    } else appConfig.appealLSPDataForPenaltyAndEnrolmentKey(penaltyId, EnrolmentKeys.constructMTDVATEnrolmentKey(enrolmentKey))
  }

  def getAppealsDataForPenalty(penaltyId: String, enrolmentKey: String, isLPP: Boolean, isAdditional: Boolean)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getAppealsDataForPenalty] -"
    httpClient.GET[HttpResponse](
      getAppealUrlBasedOnPenaltyType(penaltyId, enrolmentKey, isLPP, isAdditional)
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
            PagerDutyHelper.logStatusCode("getAppealsDataForPenalty", response.status)(RECEIVED_4XX_FROM_PENALTIES, RECEIVED_5XX_FROM_PENALTIES)
            logger.warn(s"$startOfLogMsg Returned unknown response ${response.status} with body: ${response.body}")
            None
        }
    }.recover {
      case e =>
        PagerDutyHelper.log("getAppealsDataForPenalty", UNKNOWN_EXCEPTION_CALLING_PENALTIES)
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
    }
  }

  def getMultiplePenaltiesForPrincipleCharge(penaltyId: String, enrolmentKey: String)
                                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MultiplePenaltiesResponse] = {
    val startOfLogMsg: String = "[PenaltiesConnector][getMultiplePenaltiesForPrincipleCharge] -"
    logger.debug(s"$startOfLogMsg Calling penalties backend with $penaltyId and $enrolmentKey")
    httpClient.GET[MultiplePenaltiesResponse](appConfig.multiplePenaltyDataUrl(penaltyId,enrolmentKey))(MultiplePenaltiesResponseReads, hc, ec)
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
        PagerDutyHelper.log("getListOfReasonableExcuses", RECEIVED_5XX_FROM_PENALTIES)
        logger.error(s"$startOfLogMsg Returned 500 from penalties. With message: ${internalServerException.getMessage}")
        None
      case e =>
        PagerDutyHelper.log("getListOfReasonableExcuses", UNKNOWN_EXCEPTION_CALLING_PENALTIES)
        logger.error(s"$startOfLogMsg Returned an exception with message: ${e.getMessage}")
        None
    }
  }

  def submitAppeal(appealSubmission: AppealSubmission, enrolmentKey: String, isLPP: Boolean,
                   penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean)
                  (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[AppealSubmissionResponse] = {
    logger.debug(s"[PenaltiesConnector][submitAppeal] - Submitting appeal model send to backend: ${Json.toJson(appealSubmission)}")
    httpClient.POST[AppealSubmission, AppealSubmissionResponse](
      appConfig.submitAppealUrl(enrolmentKey, isLPP, penaltyNumber, correlationId, isMultiAppeal), appealSubmission
    ).recover {
      case e => {
        logger.error(s"[PenaltiesConnector][submitAppeal] - An issue occurred whilst submitting appeal to penalties backend, error message: ${e.getMessage}")
        Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, s"An issue occurred whilst appealing a penalty with error: ${e.getMessage}"))
      }
    }
  }
}
