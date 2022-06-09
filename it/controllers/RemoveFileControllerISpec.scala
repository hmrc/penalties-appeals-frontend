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

package controllers

import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{CheckMode, NormalMode}
import org.mongodb.scala.Document
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDateTime

class RemoveFileControllerISpec extends IntegrationSpecCommonBase {
  val controller = injector.instanceOf[RemoveFileController]
  val repository = injector.instanceOf[UploadJourneyRepository]

  class Setup {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file/ref1").withSession(
    authToken -> "1234",
    (SessionKeys.penaltyNumber, "1234"),
    (SessionKeys.appealType, "Late_Submission"),
    (SessionKeys.startDateOfPeriod, "2020-01-01"),
    (SessionKeys.endDateOfPeriod, "2020-01-01"),
    (SessionKeys.dueDateOfPeriod, "2020-02-07"),
    (SessionKeys.dateCommunicationSent, "2020-02-08"),
    (SessionKeys.journeyId, "J1234")
  )

  val fileUploadModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    )),
    uploadFields = Some(Map(
      "key" -> "abcxyz",
      "algo" -> "md5"
    ))
  )

  "GET /remove-file/:fileReference" should {
    "return 200 (OK) the page if the file reference exists" in new Setup {
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel, isInitiateCall = true))
      val result = await(controller.onPageLoad(fileReference = "ref1", isJsEnabled = true, mode = NormalMode)(fakeRequestWithCorrectKeys))
      result.header.status shouldBe OK
    }

    "render ISE when the file reference does not exist" in new Setup {
      val result = await(controller.onPageLoad(fileReference = "ref1", isJsEnabled = true, mode = NormalMode)(fakeRequestWithCorrectKeys))
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/remove-file/ref1").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoad(fileReference = "F1234", isJsEnabled = true, mode = NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/remove-file/ref1").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoad(fileReference = "F1234", isJsEnabled = true, mode = NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/remove-file/ref1?isJsEnabled=false").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /remove-file/:fileReference" should {
    "return 303 (SEE_OTHER) redirecting to upload list page when the user answers yes (js disabled)" in new Setup {
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel.copy(reference = "ref2"), isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 2
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = false, mode = CheckMode)(fakeRequestWithCorrectKeys
        .withFormUrlEncodedBody("value" -> "yes")))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
    }

    "return 303 (SEE_OTHER) redirecting to upload JS page when the user answers yes (js enabled)" in new Setup {
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = true, mode = CheckMode)(fakeRequestWithCorrectKeys
        .withFormUrlEncodedBody("value" -> "yes")))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 0
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
    }

    "return 303 (SEE_OTHER) redirecting back to upload list page when the user answers no (js disabled)" in new Setup {
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = false, mode = CheckMode)(fakeRequestWithCorrectKeys
        .withFormUrlEncodedBody("value" -> "no")))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
    }

    "return 303 (SEE_OTHER) redirecting back to upload JS page when the user answers no (js enabled)" in new Setup {
      await(repository.updateStateOfFileUpload(journeyId = "J1234", callbackModel = fileUploadModel, isInitiateCall = true))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      val result = await(controller.onSubmit(fileReference = "ref1", isJsEnabled = true, mode = CheckMode)(fakeRequestWithCorrectKeys
        .withFormUrlEncodedBody("value" -> "no")))
      await(repository.getNumberOfDocumentsForJourneyId("J1234")) shouldBe 1
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file/ref1").withSession(
        authToken -> "1234"
      )
      val request = await(controller.onPageLoad(fileReference = "F1234", isJsEnabled = true, mode = NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file/ref1").withSession(
        authToken -> "1234",
        (SessionKeys.penaltyNumber, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01")
      )
      val request = await(controller.onPageLoad(fileReference = "F1234", isJsEnabled = true, mode = NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/remove-file/ref1?isJsEnabled=false").post(""))
      request.status shouldBe SEE_OTHER
    }
  }
}
