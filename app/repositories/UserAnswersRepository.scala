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

import models.session.UserAnswers
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, UpdateOptions, Updates}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import utils.Logger.logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserAnswersRepository @Inject()(mongo: MongoComponent)
                                     (implicit ec: ExecutionContext) extends PlayMongoRepository[UserAnswers](
  collectionName = "user-answers",
  mongoComponent = mongo,
  domainFormat = UserAnswers.format,
  indexes = Seq(
    IndexModel(
      keys = ascending("journeyId"),
      indexOptions = IndexOptions()
        .name("journeyId")
        .unique(true)
    )
  )) {

  def findAll(): Future[Seq[UserAnswers]] = {
    collection.find().toFuture
  }

  def getUserAnswer(id: String): Future[Option[UserAnswers]] =
    collection.find(equal("journeyId", id)).headOption()

  def insertUserAnswer(userAnswers: UserAnswers): Future[Boolean] = {
    collection.insertOne(userAnswers).toFuture().map(_.wasAcknowledged())
      .recover {
        case e => {
          logger.error(s"[UserAnswersRepository][insertUserAnswer] - Failed to insert data with message: ${e.getMessage}")
          false
        }
      }
  }

  def upsertUserAnswer(userAnswers: UserAnswers): Future[Boolean] = {
    collection.updateOne(
      filter = equal("journeyId", userAnswers.journeyId),
      update = Updates.combine(
        Updates.set("data", Codecs.toBson(userAnswers.data)),
        Updates.setOnInsert("journeyId", userAnswers.journeyId)
      ),
      options = UpdateOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
      .recover {
        case e => {
          logger.error(s"[UserAnswersRepository][upsertUserAnswer] - Failed to insert or update data with message: ${e.getMessage}")
          false
        }
      }
  }

  def deleteOneUserAnswer(journeyId: String): Future[Int] = {
    collection.deleteOne(equal("journeyId", journeyId)).toFuture().map(_.getDeletedCount.toInt)
      .recover {
        case e => {
          logger.error(s"[UserAnswersRepository][deleteOneUserAnswer] - Failed to delete data for journeyId: $journeyId with " +
            s"error message: ${e.getMessage}")
          0
        }
      }
  }

  def deleteManyUserAnswer(journeyId: List[String]): Future[Int] = {
    collection.deleteMany(in("journeyId", journeyId: _*)).toFuture().map(_.getDeletedCount.toInt)
      .recover {
        case e => {
          logger.error(s"[UserAnswersRepository][deleteManyUserAnswer] - Failed to delete data for id: $journeyId with " +
            s"error message: ${e.getMessage}")
          0
        }
      }
  }
}
