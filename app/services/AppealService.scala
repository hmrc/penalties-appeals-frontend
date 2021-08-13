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

package services

import config.AppConfig
import connectors.PenaltiesConnector
import helpers.DateTimeHelper
import models.appeals.AppealSubmission
import models.{AppealData, PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import play.api.http.Status._
import utils.Logger.logger
import play.api.libs.json.{JsResult, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.EnrolmentKeys.constructMTDVATEnrolmentKey
import utils.SessionKeys

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealService @Inject()(penaltiesConnector: PenaltiesConnector, appConfig: AppConfig, dateTimeHelper: DateTimeHelper) {

  def validatePenaltyIdForEnrolmentKey[A](penaltyId: String, isLPP: Boolean,isAdditional:Boolean)(implicit user: UserRequest[A], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AppealData]] = {
    penaltiesConnector.getAppealsDataForPenalty(penaltyId, user.vrn, isLPP,isAdditional).map {
      _.fold[Option[AppealData]](
        None
      )(
        jsValue => {
          val parsedAppealDataModel = Json.fromJson(jsValue)(AppealData.format)
          parsedAppealDataModel.fold(
            failure => {
              logger.warn(s"[AppealService][validatePenaltyIdForEnrolmentKey] - Failed to parse to model with error(s): $failure")
              None
            },
            parsedModel => Some(parsedModel)
          )
        }
      )
    }
  }

  def getReasonableExcuseListAndParse()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[ReasonableExcuse]]] = {
    penaltiesConnector.getListOfReasonableExcuses().map {
      _.fold[Option[Seq[ReasonableExcuse]]](
        None
      )(
        jsValue => {
          val resultOfParsing: JsResult[Seq[ReasonableExcuse]] = Json.fromJson[Seq[ReasonableExcuse]](jsValue)(ReasonableExcuse.seqReads)
          resultOfParsing.fold(
            failure => {
              logger.error(s"[AppealService][getReasonableExcuseListAndParse] - Failed to parse to model with error(s): $failure")
              None
            },
            seqOfReasonableExcuses => Some(seqOfReasonableExcuses)
          )
        }
      )
    }
  }

  def submitAppeal(reasonableExcuse: String)(implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean] = {
    val dateSentParsed: LocalDateTime = LocalDateTime.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateTimeNow: LocalDateTime = dateTimeHelper.dateTimeNow
    val isLateAppeal = dateSentParsed.isBefore(dateTimeNow.minusDays(daysResultingInLateAppeal))
    val enrolmentKey = constructMTDVATEnrolmentKey(userRequest.vrn)
    val appealType = userRequest.session.get(SessionKeys.appealType)
    val isLPP = appealType.contains(PenaltyTypeEnum.Late_Payment.toString) || appealType.contains(PenaltyTypeEnum.Additional.toString)

    val modelFromRequest: AppealSubmission = AppealSubmission.constructModelBasedOnReasonableExcuse(reasonableExcuse, isLateAppeal)
    penaltiesConnector.submitAppeal(modelFromRequest, enrolmentKey, isLPP).map {
      response =>
        response.status match {
          case OK => {
            logger.debug("[AppealService][submitAppeal] - Received OK from the appeal submission call")
            true
          }
          case _ => {
            logger.error(s"[AppealService][submitAppeal] - Received unknown status code from connector: ${response.status}")
            false
          }
        }
    } recover {
      case e => {
        logger.error(s"[AppealService][submitAppeal] - An unknown error occurred, error message: ${e.getMessage}")
        false
      }
    }
  }

  def otherPenaltiesInTaxPeriod(penaltyId: String, isLPP: Boolean)
                               (implicit userRequest: UserRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean] = {
    val startOfLogMsg: String = "[AppealService][otherPenaltiesInTaxPeriod] -"
    penaltiesConnector.getOtherPenaltiesInTaxPeriod(penaltyId, userRequest.vrn, isLPP).map(
      response => response.status match {
        case OK =>
          logger.debug(s"$startOfLogMsg Received OK from the other penalties call")
          true
        case NO_CONTENT =>
          logger.debug(s"$startOfLogMsg Received No CONTENT from the other penalties call")
          false
      }
    ).recover {
      case e => {
        logger.error(s"$startOfLogMsg unknown error occurred, error message: ${e.getMessage}")
        false
      }
    }
  }
}
