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

import models.UserRequest
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.http.Status
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.{AuthStub, PenaltiesStub}
import utils.{IntegrationSpecCommonBase, SessionKeys}
import java.time.LocalDateTime

import uk.gov.hmrc.http.SessionKeys.authToken

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersControllerISpec extends IntegrationSpecCommonBase {
  val controller: CheckYourAnswersController = injector.instanceOf[CheckYourAnswersController]
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val correlationId: String = "correlationId"

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(Document()).toFuture())
    super.beforeEach()
  }

  "GET /check-your-answers" should {
    "return 200 (OK) when the user is authorised and has the correct keys in session for crime" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the crime happen?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Has this crime been reported to the police?"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Yes"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for loss of staff" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person become unavailable?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the technology issues begin?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "When did the technology issues end?"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "2 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for fire or flood" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for crime - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for fire or flood - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for loss of staff - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
        SessionKeys.lateAppealReason -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for other" when {
      "no file upload - no late appeal" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "Not provided"
      }

      "file upload - no late appeal" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "4321",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val callBackModel: UploadJourney = UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )
        await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
      }

      "multiple file upload - no late appeal" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "4321",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val callBackModel: UploadJourney = UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )
        val callBackModel2: UploadJourney = UploadJourney(
          reference = "ref2",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file2.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )
        await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
        await(repository.updateStateOfFileUpload("4321", callBackModel2, isInitiateCall = true))
        //Used to get around a race condition
        eventually {
          await(repository.getUploadsForJourney(Some("4321")).map(_.get.find(_.reference == "ref1").get)).fileStatus shouldBe UploadStatusEnum.READY
          await(repository.getUploadsForJourney(Some("4321")).map(_.get.find(_.reference == "ref2").get)).fileStatus shouldBe UploadStatusEnum.READY
        }
        val request: Future[Result] = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt, file2.txt"
      }

      "no file upload - late appeal" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is why the appeal is late.",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "Not provided"
        parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "Reason for appealing after 30 days"
        parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "This is why the appeal is late."
      }

      "file upload - late appeal" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "4321",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is why the appeal is late.",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val callBackModel: UploadJourney = UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )
        await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
        parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "Reason for appealing after 30 days"
        parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "This is why the appeal is late."
      }

      "user selected to not upload a file but already has uploaded files - do not show the 'Evidence to support this appeal' row" in {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "4321",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.isUploadEvidence -> "no"
        )
        val callBackModel: UploadJourney = UploadJourney(
          reference = "ref1",
          fileStatus = UploadStatusEnum.READY,
          downloadUrl = Some("download.file/url"),
          uploadDetails = Some(UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
            checksum = "check1234",
            size = 2
          ))
        )
        await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "No"
        parsedBody.select("#main-content dl > div:nth-child(5)").isEmpty shouldBe true
      }
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for bereavement" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "bereavement",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidThePersonDie -> "2021-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person die?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2021"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for bereavement - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "bereavement",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidThePersonDie -> "2021-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "Lorem ipsum",
        SessionKeys.journeyId -> "1234",
        SessionKeys.isUploadEvidence -> "yes"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal - with file upload" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "4321",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "Lorem ipsum",
        SessionKeys.isUploadEvidence -> "yes"
      )
      val callBackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "Yes"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Evidence to support this appeal"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal - selected no to file upload" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "4321",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "Lorem ipsum",
        SessionKeys.isUploadEvidence -> "no"
      )
      val callBackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("4321", callBackModel, isInitiateCall = true))
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "No"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for LPP - agent" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.agentSessionVrn -> "VRN1234",
        SessionKeys.appealType -> "Late_Payment",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "bereavement",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidThePersonDie -> "2021-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person die?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2021"
    }


    "return 500 (ISE) when the user hasn't selected a reasonable excuse option" in {
      val fakeRequestWithMissingReasonableExcuse: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithMissingReasonableExcuse))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user has selected a reasonable excuse option but hasn't completed the journey" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/check-your-answers").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /check-your-answers" should {
    "redirect the user to the confirmation page on success for crime" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for fire or flood" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for loss of staff" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for technical issues" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for health" when {
      "there is no hospital stay" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2021-01-01T12:00:00",
          SessionKeys.journeyId -> "1234"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there is a ongoing hospital stay" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00",
          SessionKeys.journeyId -> "1234"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there has been a hospital stay that has ended" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "yes",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00",
          SessionKeys.whenHealthIssueEnded -> "2021-01-02T12:00:00",
          SessionKeys.journeyId -> "1234"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

    }

    "redirect the user to the confirmation page on success for other" when {
      "the user hasn't uploaded a file" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "the user has uploaded a file" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "no file upload - late appeal" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is a reason for late appeal",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "file upload - late appeal" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is a reason for late appeal",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Payment",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is a reason for late appeal",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP - agent" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.agentSessionVrn -> "VRN1234",
          SessionKeys.appealType -> "Late_Payment",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is a reason for late appeal",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP Additional - agent" in {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.agentSessionVrn -> "VRN1234",
          SessionKeys.appealType -> "Additional",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason",
          SessionKeys.lateAppealReason -> "This is a reason for late appeal",
          SessionKeys.journeyId -> "1234",
          SessionKeys.isUploadEvidence -> "yes"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }
    }

    "redirect the user to the confirmation page on success for bereavement" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "bereavement",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidThePersonDie -> "2021-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for obligation appeal" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys = UserRequest("123456789")(FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "some text",
        SessionKeys.journeyId -> "1234",
        SessionKeys.isUploadEvidence -> "yes"
      ))
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page (regardless of reason) and delete all uploads for that user" in {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val uploadJourneyModel: UploadJourney = UploadJourney(
        reference = "file1234", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("/upload"), uploadDetails = Some(UploadDetails(
          fileName = "file1.txt", fileMimeType = "text/plain", uploadTimestamp = LocalDateTime.now(), checksum = "check1", size = 1024
        ))
      )
      await(repository.updateStateOfFileUpload("1234", uploadJourneyModel, isInitiateCall = true))
      val fakeRequestWithCorrectKeys = UserRequest("123456789")(FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "some text",
        SessionKeys.journeyId -> "1234"
      ))
      await(repository.collection.countDocuments().toFuture()) shouldBe 1
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the InternalServerError on failure" in {
      PenaltiesStub.failedAppealSubmissionWithFault(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys = UserRequest("123456789")(FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.otherRelevantInformation -> "some-text",
        SessionKeys.journeyId -> "1234"
      ))
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "redirect to service unavailable page when downstream returns SERVICE_UNAVAILABLE" in {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(SERVICE_UNAVAILABLE))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onSubmit()(fakeRequestWithCorrectKeys)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.ServiceUnavailableController.onPageLoad().url
    }

    "redirect to duplicate appeal page when downstream returns CONFLICT" in {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(CONFLICT))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = controller.onSubmit()(fakeRequestWithCorrectKeys)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.DuplicateAppealController.onPageLoad().url
    }

    "redirect to the ProblemWithService page when the appeal fails from a payload issue" in {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(BAD_REQUEST))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "show an ISE when the appeal fails" in {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234")
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "redirect to the ProblemWithService page when the appeal fails from an issue with the service" in {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(INTERNAL_SERVER_ERROR))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/check-your-answers").post("{}"))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-confirmation" should {
    "redirect the user to the confirmation page on success" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        authToken -> "1234",
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForConfirmation()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
      SessionKeys.allKeys.toSet.subsetOf(request.session(fakeRequestWithCorrectKeys).data.values.toSet) shouldBe false

    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/check-your-answers").post("{}"))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
