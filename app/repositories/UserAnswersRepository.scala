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
import helpers.DateTimeHelper
import models.session.UserAnswers
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, UpdateOptions, Updates}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import utils.Logger.logger

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserAnswersRepository @Inject()(mongo: MongoComponent,
                                      appConfig: AppConfig,
                                      dateTimeHelper: DateTimeHelper)
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
    ),
    IndexModel(
      keys = ascending("lastUpdated"),
      indexOptions = IndexOptions()
        .name("user-answers-last-updated-index")
        .expireAfter(appConfig.mongoTTL.toSeconds, TimeUnit.SECONDS)
    )
  )) {

  def getUserAnswer(id: String): Future[Option[UserAnswers]] =
    collection.find(equal("journeyId", id)).headOption()

  def upsertUserAnswer(userAnswers: UserAnswers): Future[Boolean] = {
    collection.updateOne(
      filter = equal("journeyId", userAnswers.journeyId),
      update = Updates.combine(
        Updates.set("data", Codecs.toBson(userAnswers.data)),
        Updates.setOnInsert("journeyId", userAnswers.journeyId),
        Updates.set("lastUpdated", dateTimeHelper.dateTimeNow)
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

  def deleteUserAnswers(journeyId: String): Future[Int] = {
    collection.deleteOne(equal("journeyId", journeyId)).toFuture().map(_.getDeletedCount.toInt)
      .recover {
        case e => {
          logger.error(s"[UserAnswersRepository][deleteUserAnswers] - Failed to delete data for journeyId: $journeyId with " +
            s"error message: ${e.getMessage}")
          0
        }
      }
  }
}
