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

package controllers

import controllers.testHelpers.AuthorisationTest
import models.session.UserAnswers
import models.{CheckMode, NormalMode, PenaltyTypeEnum}
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.Json
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class RemoveFileControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller = injector.instanceOf[RemoveFileController]
  val repository = injector.instanceOf[UploadJourneyRepository]

  class Setup(sessionDataToStore: UserAnswers = UserAnswers("1234", Json.obj())) extends UserAnswersSetup(sessionDataToStore) {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  val userAnswers: UserAnswers = userAnswers(Json.obj(
    SessionKeys.penaltyNumber -> "1234",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
  ))

  "GET /remove-file/:fileReference" should {
    "return 200 (OK) the page if the file reference exists" in new Setup(userAnswers) {
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel, isInitiateCall = true))
      val result = await(controller.onPageLoad(fileReference = "ref1", isJsEnabled = true, mode = NormalMode)(fakeRequest))
      result.header.status shouldBe OK
    }

    "render ISE when the file reference does not exist" in new Setup(userAnswers) {
      val result = await(controller.onPageLoad(fileReference = "ref1", isJsEnabled = true, mode = NormalMode)(fakeRequest))
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe routes.InternalServerErrorController.onPageLoad().url
    }

    runControllerPredicateTests(controller.onPageLoad(fileReference = "ref1", isJsEnabled = true, mode = NormalMode), "GET", "/remove-file/ref1?isJsEnabled=true")

  }

  "POST /remove-file/:fileReference" should {
    "return 303 (SEE_OTHER) redirecting to upload list page when the user answers yes (js disabled)" in new Setup(userAnswers) {
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel.copy(reference = "ref2"), isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 2
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = false, mode = CheckMode)(fakeRequest
        .withFormUrlEncodedBody("value" -> "yes")))
      eventually {
        await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      }
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
    }

    "return 303 (SEE_OTHER) redirecting to upload JS page when the user answers yes (js enabled)" in new Setup(userAnswers) {
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = true, mode = CheckMode)(fakeRequest
        .withFormUrlEncodedBody("value" -> "yes")))
      eventually {
        result.header.status shouldBe SEE_OTHER
        result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, true).url
        await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 0
      }
    }

    "return 303 (SEE_OTHER) redirecting back to upload list page when the user answers no (js disabled)" in new Setup(userAnswers) {
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = false, mode = CheckMode)(fakeRequest
        .withFormUrlEncodedBody("value" -> "no")))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
    }

    "return 303 (SEE_OTHER) redirecting back to upload JS page when the user answers no (js enabled)" in new Setup(userAnswers) {
      await(repository.updateStateOfFileUpload(journeyId = "1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = true, mode = CheckMode)(fakeRequest
        .withFormUrlEncodedBody("value" -> "no")))
      await(repository.getNumberOfDocumentsForJourneyId("1234")) shouldBe 1
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, true).url
    }

    runControllerPredicateTests(controller.onSubmit(fileReference = "ref1", isJsEnabled = true, mode = NormalMode), "POST", "/remove-file/ref1?isJsEnabled=true")
  }
}
