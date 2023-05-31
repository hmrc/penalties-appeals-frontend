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

import models.session.UserAnswers
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import utils.IntegrationSpecCommonBase

class UserAnswersRepositoryISpec extends IntegrationSpecCommonBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  lazy val repository: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  class Setup {
    await(repository.collection.drop().toFuture())
    await(repository.ensureIndexes)
    await(repository.collection.countDocuments().head()) shouldBe 0L
  }

  val userAnswers: UserAnswers = UserAnswers(journeyId = "journey123", data = Json.obj("key1" -> "value1", "key2" -> "value2"))
  val userAnswers2: UserAnswers = userAnswers.copy(journeyId = "journey456")
  val userAnswers3: UserAnswers = userAnswers.copy(journeyId = "journey789")

  "upsertUserAnswer" should {
    "insert userAnswer payload when there is no duplicate keys" in new Setup {
      val result = await(repository.upsertUserAnswer(userAnswers))
      result shouldBe true

      val recordsInMongoAfterInsertion = await(repository.collection.find().toFuture())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head shouldBe userAnswers
    }

    "update userAnswer payload when there IS duplicate key" in new Setup {
      val duplicateUserAnswer: UserAnswers = userAnswers.copy(data = Json.obj(
        "key12" -> "value12",
        "key23" -> "value23"
      ))
      await(repository.upsertUserAnswer(userAnswers))
      await(repository.upsertUserAnswer(duplicateUserAnswer))
      val recordsInMongoAfterUpdate = await(repository.collection.find().toFuture())
      recordsInMongoAfterUpdate.size shouldBe 1
      recordsInMongoAfterUpdate.head shouldBe duplicateUserAnswer
    }
  }

  "getUserAnswer" should {
    s"return a $UserAnswers when there is a pre-existing record under the journeyId" in new Setup {
      await(repository.upsertUserAnswer(userAnswers))
      val recordsInMongoAfterInsertion = await(repository.collection.find().toFuture())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head shouldBe userAnswers

      val getResult = await(repository.getUserAnswer("journey123"))
      getResult.isDefined shouldBe true
      getResult.get shouldBe userAnswers
    }

    s"return $None when there is NO pre-existing record under the journeyId" in new Setup {
      val getResult = await(repository.getUserAnswer("journey123"))
      getResult.isEmpty shouldBe true
    }
  }

  "deleteUserAnswers" should {
    "return 1 when there is a pre-existing record under the journeyId" in new Setup {
      await(repository.upsertUserAnswer(userAnswers))
      val recordsInMongoAfterInsertion = await(repository.collection.find().toFuture())
      recordsInMongoAfterInsertion.size shouldBe 1
      val deleteResult = await(repository.deleteUserAnswers("journey123"))
      deleteResult shouldBe 1
      val recordsInMongoAfterDeletion = await(repository.collection.find().toFuture())
      recordsInMongoAfterDeletion.size shouldBe 0
    }

    "return 0 when there is no pre-existing record under the journeyId" in new Setup {
      val deleteResult = await(repository.deleteUserAnswers("journey123"))
      deleteResult shouldBe 0
    }
  }
}
