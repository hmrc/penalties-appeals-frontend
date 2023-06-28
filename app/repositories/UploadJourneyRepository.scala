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

package repositories

import config.AppConfig
import models.upload.{UploadJourney, UploadStatus}
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import utils.Logger.logger

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

  def updateStateOfFileUpload(journeyId: String, callbackModel: UploadJourney, isInitiateCall: Boolean = false): Future[Option[String]] = {
    get[UploadJourney](journeyId)(DataKey(callbackModel.reference)).flatMap(
      document => {
        if (document.isDefined || isInitiateCall) {
          put(journeyId)(DataKey(callbackModel.reference), callbackModel).map(item => {
            logger.info(s"[UploadJourneyRepository][updateStateOfFileUpload] - updating state of file (reference: ${callbackModel.reference}) upload in repository: " +
              s"is document defined: ${document.isDefined}, isInitiateCall: $isInitiateCall")
            item.id
          }).map(Some(_))
        } else {
          logger.info(s"[UploadJourneyRepository][updateStateOfFileUpload] - found no existing file in Mongo for file ref: ${callbackModel.reference}")
          Future.successful(None)
        }
      }
    )
  }

  def getFieldsForFileReference(journeyId: String, fileReference: String): Future[Option[Map[String, String]]] = {
    get[UploadJourney](journeyId)(DataKey(fileReference)).map(
      upload => {
        if (upload.exists(_.uploadFields.isDefined)) {
          upload.flatMap(_.uploadFields.map(
            fields => {
              logger.debug(s"[UploadJourneyRepository][getFieldsForFileReference] -" +
                s" Received the following upload fields, fields: $fields using file reference: $fileReference")
              fields
            }
          ))
        } else {
          logger.info(s"[UploadJourneyRepository][getFieldsForFileReference] - found no existing file in Mongo for file ref: $fileReference")
          None
        }
      }
    )
  }

  def getStatusOfFileUpload(journeyId: String, fileReference: String): Future[Option[UploadStatus]] = {
    get[UploadJourney](journeyId)(DataKey(fileReference)).map(
      upload => {
        if (upload.exists(_.failureDetails.isDefined)) {
          upload.flatMap(_.failureDetails.map(
            failureDetails => {
              logger.warn(s"[UploadJourneyRepository][getStatusOfFileUpload] -" +
                s" Received failure response back from Upscan, status: ${failureDetails.failureReason} for journey: $journeyId")
              UploadStatus(failureDetails.failureReason.toString, Some(failureDetails.message))
            }))
        } else {
          logger.info(s"[UploadJourneyRepository][getStatusOfFileUpload] - Success response returned for journey: $journeyId and file reference: $fileReference")
          upload.map(file => UploadStatus(file.fileStatus.toString))
        }
      }
    )
  }

  def getUploadsForJourney(journeyId: Option[String]): Future[Option[Seq[UploadJourney]]] = {
    journeyId match {
      case Some(id) =>
        findById(id)
          .map {
            case Some(cacheItem) if cacheItem.data.fields.nonEmpty =>
              val list = cacheItem.data.values
              Some(list.map(a => a.as[UploadJourney]).toSeq)
            case None => None
            case _ => throw new MatchError(s"[UploadJourneyRepository][getUploadsForJourney] - unknown id ${findById(id)}")
          }
      case _ => Future.successful(None)
    }
  }

  def getFileForJourney(journeyId: String, fileReference: String): Future[Option[UploadJourney]] = {
    findById(journeyId).map(_.fold[Option[UploadJourney]](None)(
      {
        item => {
          val list = item.data.values
          list.map(a => a.as[UploadJourney]).toSeq
            .find(_.reference.equals(fileReference))
        }
      }
    ))
  }

  def getNumberOfDocumentsForJourneyId(journeyId: String): Future[Int] = {
    findById(journeyId).map {
      _.map {
        item => {
          val list = item.data.values
          list.map(a => a.as[UploadJourney]).toSeq.size
        }
      }.getOrElse(0)
    }
  }

  def removeUploadsForJourney(journeyId: String): Future[Unit] = {
    logger.info(s"[UploadJourneyRepository][removeUploadsForJourney] - Removing uploads for journey ID: $journeyId")
    deleteEntity(journeyId)
  }

  def removeFileForJourney(journeyId: String, fileReference: String): Future[Unit] = {
    getNumberOfDocumentsForJourneyId(journeyId).map {
      amountOfDocuments => {
        if (amountOfDocuments <= 1) {
          deleteEntity(journeyId)
        } else {
          delete(journeyId)(DataKey(fileReference))
        }
      }
    }
  }

  def getAllChecksumsForJourney(journeyId: Option[String]): Future[Seq[String]] = {
    getUploadsForJourney(journeyId).map(
      _.map(
        _.map(
          _.uploadDetails.map(
            _.checksum
          )
        ).collect { case Some(x) => x }
      ).getOrElse(Seq.empty))
  }

  def getFileIndexForJourney(journeyId: String, fileReference: String): Future[Int] = {
    findById(journeyId).map {
      _.map {
        item => {
          val list = item.data.values
          list.map(a => a.as[UploadJourney]).toSeq.indexWhere(_.reference.equals(fileReference))
        }
      }.getOrElse(-1)
    }
  }
}
