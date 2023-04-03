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

import models.PenaltyTypeEnum
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.jsoup.{Jsoup, nodes}
import org.mongodb.scala.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.{AuthStub, PenaltiesStub}
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
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
    "return 200 (OK) when the user is authorised and has the correct keys in session for crime" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the crime happen?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Has this crime been reported to the police?"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Yes"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for loss of staff" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "lossOfStaff",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person leave the business?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "technicalIssues",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
      SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the technology issues begin?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "When did the technology issues end?"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "2 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for fire or flood" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "fireOrFlood",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for crime - for a late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.lateAppealReason -> "Lorem ipsum",
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for fire or flood - for a late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "fireOrFlood",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.lateAppealReason -> "Lorem ipsum",
      SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for loss of staff - for a late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "lossOfStaff",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.lateAppealReason -> "Lorem ipsum",
      SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues - for a late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "technicalIssues",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.lateAppealReason -> "Lorem ipsum",
      SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
      SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for other" when {
      "no file upload - no late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "yes"
      ))) {
        val request = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "Not provided"
      }

      "file upload - no late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "yes"
      ))) {
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
        await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
      }

      "multiple file upload - no late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "yes"
      ))) {
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
        await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
        await(repository.updateStateOfFileUpload("1234", callBackModel2, isInitiateCall = true))
        //Used to get around a race condition
        eventually {
          await(repository.getUploadsForJourney(Some("1234")).map(_.get.find(_.reference == "ref1").get)).fileStatus shouldBe UploadStatusEnum.READY
          await(repository.getUploadsForJourney(Some("1234")).map(_.get.find(_.reference == "ref2").get)).fileStatus shouldBe UploadStatusEnum.READY
        }
        val request: Future[Result] = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt, file2.txt"
      }

      "no file upload - late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.lateAppealReason -> "This is why the appeal is late.",
        SessionKeys.isUploadEvidence -> "yes"
      ))) {
        val request = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "Not provided"
        parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "Reason for appealing after 30 days"
        parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "This is why the appeal is late."
      }

      "file upload - late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.lateAppealReason -> "This is why the appeal is late.",
        SessionKeys.isUploadEvidence -> "yes"
      ))) {
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
        await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Yes"
        parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Evidence to support this appeal"
        parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
        parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "Reason for appealing after 30 days"
        parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "This is why the appeal is late."
      }

      "user selected to not upload a file but already has uploaded files - do not show the 'Evidence to support this appeal' row" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
        SessionKeys.isUploadEvidence -> "no"
      ))) {
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
        await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
        val request: Future[Result] = controller.onPageLoad()(fakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody: nodes.Document = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
        parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "No"
        parsedBody.select("#main-content dl > div:nth-child(5)").isEmpty shouldBe true
      }
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for bereavement" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person die?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for bereavement - for a late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.lateAppealReason -> "Lorem ipsum",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.otherRelevantInformation -> "Lorem ipsum",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal - with file upload" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.otherRelevantInformation -> "Lorem ipsum",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
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
      await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "Yes"
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Evidence to support this appeal"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
    }

    "return 200 (OK) when the user is authorised and has the correct keys for obligation appeal - selected no to file upload" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.otherRelevantInformation -> "Lorem ipsum",
      SessionKeys.isUploadEvidence -> "no"
    ))) {
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
      await(repository.updateStateOfFileUpload("1234", callBackModel, isInitiateCall = true))
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "No"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for LPP - agent" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2021-01-01")
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person die?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2021"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for LPP - multiple penalties appeal available" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().plusDays(1),
      SessionKeys.secondPenaltyCommunicationDate -> LocalDate.now().plusDays(1),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2021-01-01"),
      SessionKeys.doYouWantToAppealBothPenalties -> "yes"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "Do you intend to appeal both penalties for the same reason?"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "Yes"
    }


    "return 303 (SEE_OTHER) when the user hasn't selected a reasonable excuse option" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
    }

    "return 303 (SEE_OTHER) when the user has selected a reasonable excuse option but hasn't completed the journey" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/check-your-answers").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /check-your-answers" should {
    "redirect the user to the confirmation page on success for crime" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for fire or flood" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "fireOrFlood",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for loss of staff" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "lossOfStaff",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for technical issues" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "technicalIssues",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
      SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for health" when {
      "there is no hospital stay" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "health",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.wasHospitalStayRequired -> "no",
        SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there is a ongoing hospital stay" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "health",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.wasHospitalStayRequired -> "yes",
        SessionKeys.hasHealthEventEnded -> "no",
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there has been a hospital stay that has ended" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "health",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.wasHospitalStayRequired -> "yes",
        SessionKeys.hasHealthEventEnded -> "yes",
        SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
        SessionKeys.whenHealthIssueEnded -> LocalDate.parse("2022-01-02")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

    }

    "redirect the user to the confirmation page on success for other" when {
      "the user hasn't uploaded a file" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "the user has uploaded a file" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "no file upload - late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.lateAppealReason -> "This is a reason for late appeal",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          authToken -> "1234",
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01",
          SessionKeys.endDateOfPeriod -> "2020-01-01",
          SessionKeys.dueDateOfPeriod -> "2020-02-07",
          SessionKeys.dateCommunicationSent -> "2020-02-08",
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

      "file upload - late appeal" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.lateAppealReason -> "This is a reason for late appeal",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.lateAppealReason -> "This is a reason for late appeal",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val request = await(controller.onSubmit()(fakeRequest))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP - agent" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.lateAppealReason -> "This is a reason for late appeal",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = fakeRequest.withSession(
          SessionKeys.agentSessionVrn -> "VRN1234"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "for LPP Additional - agent" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Additional,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason",
        SessionKeys.lateAppealReason -> "This is a reason for late appeal",
        SessionKeys.isUploadEvidence -> "yes",
        SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
      ))) {
        PenaltiesStub.successfulAppealSubmission(isLPP = true, "1234")
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = fakeRequest.withSession(
          SessionKeys.agentSessionVrn -> "VRN1234"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }
    }

    "redirect the user to the confirmation page on success for bereavement" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "bereavement",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for obligation appeal" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page (regardless of reason) and delete all uploads for that user" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.successfulAppealSubmission(isLPP = false, "1234")
      val uploadJourneyModel: UploadJourney = UploadJourney(
        reference = "file1234", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("/upload"), uploadDetails = Some(UploadDetails(
          fileName = "file1.txt", fileMimeType = "text/plain", uploadTimestamp = LocalDateTime.now(), checksum = "check1", size = 1024
        ))
      )
      await(repository.updateStateOfFileUpload("1234", uploadJourneyModel, isInitiateCall = true))
      await(repository.collection.countDocuments().toFuture()) shouldBe 1
      val request = await(controller.onSubmit()(fakeRequest))
      await(repository.collection.countDocuments().toFuture()) shouldBe 0
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the service unavailable page on unmatched fault" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "no"
    ))) {
      PenaltiesStub.failedAppealSubmissionWithFault(isLPP = false, "1234")
      val request = controller.onSubmit()(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "redirect to service unavailable page when downstream returns SERVICE_UNAVAILABLE" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(SERVICE_UNAVAILABLE))
      val request = controller.onSubmit()(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.ServiceUnavailableController.onPageLoad().url
    }

    "redirect to duplicate appeal page when downstream returns CONFLICT" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(CONFLICT))
      val request = controller.onSubmit()(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.DuplicateAppealController.onPageLoad().url
    }

    "redirect to the ProblemWithService page when downstream returns UNPROCESSABLE_ENTITY" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(UNPROCESSABLE_ENTITY))
      val request = controller.onSubmit()(fakeRequest)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "redirect to the ProblemWithService page when the appeal fails from a payload issue" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(BAD_REQUEST))
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "show an ISE when the appeal fails" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234")
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.ProblemWithServiceController.onPageLoad().url
    }

    "redirect to the ProblemWithService page when the appeal fails from an issue with the service" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.otherRelevantInformation -> "some text",
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      PenaltiesStub.failedAppealSubmission(isLPP = false, "1234", status = Some(INTERNAL_SERVER_ERROR))
      val request = await(controller.onSubmit()(fakeRequest))
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
    "redirect the user to the confirmation page on success" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      val request = await(controller.onPageLoadForConfirmation()(fakeRequest.withSession(
        SessionKeys.confirmationAppealType -> PenaltyTypeEnum.Late_Submission.toString,
        SessionKeys.confirmationStartDate -> LocalDate.parse("2020-01-01").toString,
        SessionKeys.confirmationEndDate -> LocalDate.parse("2020-01-01").toString,
        SessionKeys.confirmationMultipleAppeals -> "no",
        SessionKeys.confirmationObligation -> "false",
        SessionKeys.confirmationIsAgent -> "false")))
      request.header.status shouldBe Status.OK
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/check-your-answers").post("{}"))
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
