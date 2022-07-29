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
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import utils.IntegrationSpecCommonBase

import scala.concurrent.ExecutionContext.Implicits.global

class UserAnswersRepositoryISpec extends IntegrationSpecCommonBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  val repository: UserAnswersRepository = new UserAnswersRepository(mongoComponent)

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
    //Stop it showing in Mongo unnecessarily after test suite has finished
    await(repository.collection.drop().toFuture())
  }

  class Setup {
    await(repository.collection.drop().toFuture())
    await(repository.ensureIndexes)
    await(repository.collection.countDocuments().head()) shouldBe 0L
  }

  val userAnswer: UserAnswers = UserAnswers(
    journeyId = "journey123",
    data = Json.obj(
      "key1" -> "value1",
      "key2" -> "value2"
    )
  )

  val userAnswer2: UserAnswers = UserAnswers(journeyId = "journey456", data = Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))
  val userAnswer3: UserAnswers = UserAnswers(journeyId = "journey789", data = Json.obj(
    "key1" -> "value1",
    "key2" -> "value2"
  ))

  "findAll" should {
    "return all the documents in the collection when they exist" in new Setup {
      val documentsToStore: Seq[UserAnswers] = Seq(
        UserAnswers("journey123", Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )),
        UserAnswers("journey124", Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )),
        UserAnswers("journey125", Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        ))
      )

      documentsToStore.map {
        payload => {
          await(repository.collection.insertOne(payload).toFuture()).wasAcknowledged() shouldBe true
        }
      }

      val result = await(repository.findAll())
      result shouldBe documentsToStore
    }

    "return no documents when none exist in the collection" in new Setup {
      val result = await(repository.findAll())
      result.isEmpty shouldBe true
    }
  }

  "upsertUserAnswer" should {
    "insert userAnswer payload when there is no duplicate keys" in new Setup {
      val result = await(repository.upsertUserAnswer(userAnswer))
      result shouldBe true

      val recordsInMongoAfterInsertion = await(repository.findAll())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head shouldBe userAnswer
    }

    "update userAnswer payload when there IS duplicate key" in new Setup {
      val duplicateUserAnswer: UserAnswers = UserAnswers("journey123", Json.obj(
        "key12" -> "value12",
        "key23" -> "value23"
      ))
      val result = await(repository.upsertUserAnswer(userAnswer))
      result shouldBe true

      val duplicateResult = await(repository.upsertUserAnswer(duplicateUserAnswer))
      duplicateResult shouldBe true

      val recordsInMongoAfterUpdate = await(repository.findAll())
      recordsInMongoAfterUpdate.size shouldBe 1
      recordsInMongoAfterUpdate.head shouldBe duplicateUserAnswer
      recordsInMongoAfterUpdate.head.data shouldBe duplicateUserAnswer.data
    }
  }

  "getUserAnswer" should {
    s"return a $UserAnswers when there is a pre-existing record under the journeyId" in new Setup {
      val setUpResult = await(repository.upsertUserAnswer(userAnswer))
      setUpResult shouldBe true

      val recordsInMongoAfterInsertion = await(repository.findAll())
      recordsInMongoAfterInsertion.size shouldBe 1
      recordsInMongoAfterInsertion.head shouldBe userAnswer

      val getResult = await(repository.getUserAnswer("journey123"))
      getResult.isDefined shouldBe true
      getResult.get shouldBe userAnswer
    }

    s"return $None when there is NO pre-existing record under the journeyId" in new Setup {
      val getResult = await(repository.getUserAnswer("journey123"))
      getResult.isDefined shouldBe false
    }
  }

  "deleteOneUserAnswer" should {
    "return 1 when there is a pre-existing record under the journeyId" in new Setup {
      val setUpResult = await(repository.upsertUserAnswer(userAnswer))
      setUpResult shouldBe true

      val recordsInMongoAfterInsertion = await(repository.findAll())
      recordsInMongoAfterInsertion.size shouldBe 1

      val deleteResult = await(repository.deleteOneUserAnswer("journey123"))
      deleteResult shouldBe 1

      val recordsInMongoAfterDeletion = await(repository.findAll())
      recordsInMongoAfterDeletion.size shouldBe 0
    }

    "return 0 when there is no pre-existing record under the journeyId" in new Setup {
      val deleteResult = await(repository.deleteOneUserAnswer("journey123"))
      deleteResult shouldBe 0
    }
  }

  "deleteManyUserAnswer" should {
    "return 3 when there is the same number of pre-existing records under the journeyIds" in new Setup {
      val setUpResult = (await(repository.upsertUserAnswer(userAnswer)),
        await(repository.upsertUserAnswer(userAnswer2)), await(repository.upsertUserAnswer(userAnswer3)))

      setUpResult shouldBe ((true, true, true): (Boolean, Boolean, Boolean))

      val recordsInMongoAfterInsertion = await(repository.findAll())
      recordsInMongoAfterInsertion.size shouldBe 3

      val deleteResult = await(repository.deleteManyUserAnswer(List("journey123", "journey456", "journey789")))
      deleteResult shouldBe 3

      val recordsInMongoAfterDeletion = await(repository.findAll())
      recordsInMongoAfterDeletion.size shouldBe 0
    }

    "return 2 when there is the same number of pre-existing records, but one of them does not have the correct journeyId" in new Setup {
      val setUpResult = (await(repository.upsertUserAnswer(userAnswer)),
        await(repository.upsertUserAnswer(userAnswer2)), await(repository.upsertUserAnswer(userAnswer3)))

      setUpResult shouldBe ((true, true, true): (Boolean, Boolean, Boolean))

      val recordsInMongoAfterInsertion = await(repository.findAll())
      recordsInMongoAfterInsertion.size shouldBe 3

      val deleteResult = await(repository.deleteManyUserAnswer(List("journey123", "journey000", "journey789")))
      deleteResult shouldBe 2

      val recordsInMongoAfterDeletion = await(repository.findAll())
      recordsInMongoAfterDeletion.size shouldBe 1
    }

    "return 0 when there are no pre-existing record under the journeyIds" in new Setup {
      val deleteResult = await(repository.deleteManyUserAnswer(List("journey123", "journey456", "journey789")))
      deleteResult shouldBe 0
    }
  }
}
