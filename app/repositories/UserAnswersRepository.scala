/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import config.AppConfig
import models.session.UserAnswers
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import utils.Logger.logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersRepository @Inject()(
                                       mongoComponent: MongoComponent,
                                       timestampSupport: TimestampSupport,
                                       appConfig: AppConfig
                                     )(implicit ec: ExecutionContext)
  extends MongoCacheRepository[String] (
    mongoComponent = mongoComponent,
    collectionName = "user-answers",
    replaceIndexes = true,
    ttl = appConfig.mongoTTL,
    timestampSupport = timestampSupport,
    cacheIdType = CacheIdType.SimpleCacheId
  )(ec) {

  def insertData(userAnswers: UserAnswers): Future[Boolean] = {
    logger.debug(s"[UserAnswersRepository][insertData] - inserting userAnswer ${userAnswers.toString} in repository")
    put(userAnswers.journeyId)(DataKey(userAnswers.data.toString()), userAnswers)
      .map(item => item.id).map( _ => true).recover {
      case e => {
        logger.error(s"[UserAnswersRepository][insertData] - Failed to insert data with message: ${e.getMessage}")
        false
      }
    }
  }

  def updateUserAnswer(userAnswers: UserAnswers): Future[Option[String]] = {
    get[UserAnswers](userAnswers.journeyId)(DataKey(userAnswers.data.toString())).flatMap(
      document => {
        if (document.isDefined) {
          put(userAnswers.journeyId)(DataKey(userAnswers.data.toString()), userAnswers).map(item => {
            logger.debug(s"[UserAnswersRepository][updateUserAnswer] - updating userAnswer for answer: ${userAnswers.toString} in repository: " +
              s"is document defined: ${document.isDefined}")
            item.id
          }).map(Some(_))
        } else {
          Future.successful(None)
        }
      }
    )
  }

  def getUserAnswers(userAnswers: UserAnswers): Future[Option[Seq[UserAnswers]]] = {
    Some(userAnswers.journeyId) match {
      case Some(id) =>
        findById(id)
          .map {
            case Some(cacheItem) if cacheItem.data.fields.nonEmpty =>
              val list = cacheItem.data.values
              Some(list.map(a => a.as[UserAnswers]).toSeq)
            case None => None
          }
      case _ => Future.successful(None)
    }
  }

  def removeUserAnswer(userAnswers: UserAnswers): Future[Unit] = {
    logger.info(s"[UserAnswersRepository][removeUserAnswer] - Removing user answers for journey ID: ${userAnswers.journeyId}")
    deleteEntity(userAnswers.journeyId)
  }
}
