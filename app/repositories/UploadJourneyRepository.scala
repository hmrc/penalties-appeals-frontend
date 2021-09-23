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

package repositories

import config.AppConfig
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}
import models.upload.{UploadJourney, UploadStatusEnum}
import uk.gov.hmrc.mongo.cache.{CacheIdType, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadJourneyRepository @Inject()(
                                         mongoComponent: MongoComponent,
                                         timestampSupport: TimestampSupport,
                                         appConfig: AppConfig
                                       )(implicit ec: ExecutionContext)
  extends MongoCacheRepository[String](
    mongoComponent = mongoComponent,
    collectionName = "file-upload-journeys",
    replaceIndexes = true,
    ttl = appConfig.mongoTTL,
    timestampSupport = timestampSupport,
    cacheIdType = CacheIdType.SimpleCacheId
  )(ec) {

  def updateStateOfFileUpload(journeyId: String, callbackModel: UploadJourney): Future[CacheItem] = {
    put(journeyId)(DataKey(callbackModel.reference), callbackModel)
  }

  def getStatusOfFileUpload(journeyId: String, fileReference: String): Future[Option[UploadStatusEnum.Value]] = {
    get[UploadJourney](journeyId)(DataKey(fileReference)).map(_.map(_.fileStatus))
  }

  def getUploadsForJourney(journeyId: Option[String]): Future[Option[Seq[UploadJourney]]] = {
    journeyId match {
      case Some(id) =>
        findById(id)
        .map {
          case Some(cacheItem) if cacheItem.data.fields.nonEmpty =>
            val list = cacheItem.data.values
            Some(list.map( a => a.as[UploadJourney]).toSeq)
          case None => None
        }
      case _ => Future.successful(None)
    }
  }

  def getNumberOfDocumentsForJourneyId(journeyId: String): Future[Int] = {
    findById(journeyId).map {
      _.map {
        item => {
          val list = item.data.values
          list.map( a => a.as[UploadJourney]).toSeq.size
        }
      }.getOrElse(0)
    }
  }
}
