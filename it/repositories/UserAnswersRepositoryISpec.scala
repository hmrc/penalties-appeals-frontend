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
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.DeleteResult
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.cache.DataKey
import utils.IntegrationSpecCommonBase

import scala.concurrent.Future

class UserAnswersRepositoryISpec extends IntegrationSpecCommonBase {
  lazy val repository: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  def deleteAll(): Future[DeleteResult] =
    repository
      .collection
      .deleteMany(filter = Document())
      .toFuture

  class Setup {
    await(deleteAll())
  }

  val userAnswerModel: UserAnswers = UserAnswers("journey123", Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))
  val userAnswerModel1: UserAnswers = UserAnswers("journey123", Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))
  val userAnswerModel2: UserAnswers = UserAnswers("journey1234", Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))

  "insertData" should {
    "insert data into repository" in new Setup {
      val dataInserted: Boolean = await(repository.insertData(userAnswerModel))
      dataInserted shouldBe true
    }
  }

  "updateUserAnswer" should {
    "update UserAnswer data" in new Setup {
      repository.put(userAnswerModel.journeyId)(DataKey(userAnswerModel.data.toString()), userAnswerModel)
      await(repository.updateUserAnswer(userAnswerModel))
      val modelInRepository: UserAnswers = await(repository.get[UserAnswers]("journey123")(DataKey(userAnswerModel.data.toString()))).get
      modelInRepository shouldBe userAnswerModel
    }
  }

  "not update the state when the document does not exist" in new Setup {
    val modelInRepository: Option[UserAnswers] = await(repository.get[UserAnswers](userAnswerModel.journeyId)(DataKey(userAnswerModel.data.toString())))
    modelInRepository.isEmpty shouldBe true
  }

  "getUserAnswers" should {
    s"return $None when the user answer does not exist" in new Setup {
      val result = await(repository.getUserAnswers(userAnswerModel))
      result.isDefined shouldBe false
    }
    s"return $Some when the user answer exists" in new Setup {
      repository.put(userAnswerModel.journeyId)(DataKey(userAnswerModel.data.toString()), userAnswerModel)
      await(repository.updateUserAnswer(userAnswerModel))
      val result = await(repository.getUserAnswers(userAnswerModel))
      result.isDefined shouldBe true
    }
  }

  "removeUserAnswer" should {
    "remove the file in the journey if it exists" in new Setup {
      repository.put(userAnswerModel.journeyId)(DataKey(userAnswerModel.data.toString()), userAnswerModel)
      repository.put(userAnswerModel2.journeyId)(DataKey(userAnswerModel2.data.toString()), userAnswerModel2)
      await(repository.getUserAnswers(userAnswerModel2)).size shouldBe 1
      await(repository.removeUserAnswer(userAnswerModel))
      await(repository.getUserAnswers(userAnswerModel2)).size shouldBe 1
      await(repository.getUserAnswers(userAnswerModel)).size shouldBe 0
    }

    "do not remove the file in the journey if the journey specified doesn't exist" in new Setup {
      repository.put(userAnswerModel.journeyId)(DataKey(userAnswerModel.data.toString()), userAnswerModel)
      repository.put(userAnswerModel2.journeyId)(DataKey(userAnswerModel2.data.toString()), userAnswerModel2)
      await(repository.getUserAnswers(userAnswerModel)).size shouldBe 1
      await(repository.removeUserAnswer(userAnswerModel2))
      await(repository.getUserAnswers(userAnswerModel)).size shouldBe 1
    }
  }
}
