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

import config.featureSwitches.{FeatureSwitching, ShowViewAppealDetailsPage}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout}
import repositories.UploadJourneyRepository
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ViewAppealDetailsControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {

  val controller: ViewAppealDetailsController = injector.instanceOf[ViewAppealDetailsController]
  val uploadRepository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  val fakeRequestWithPreviousId: FakeRequest[AnyContent] = FakeRequest("POST", "/").withSession(
    authToken -> "1234",
    SessionKeys.previouslySubmittedJourneyId -> "5678"
  )
  
  private def metadataRowsTest(body: Document): Unit = {
    body.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "VAT registration number (VRN)"
    body.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "123456789"
    body.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Penalty appealed"
    body.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "Late submission penalty: 1 January 2023 to 31 January 2023"
    body.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Appeal date"
    body.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "14 August 2023"
    body.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Statutory review period (45 days) ends on"
    body.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "27 September 2023"
  }

  "GET /appeal-details" should {
    "return 200 (OK) when the user is authorised and display correct appeal details (non-upload journey)" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2023-11-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ), journeyId = Some("5678"))) {
      enableFeatureSwitch(ShowViewAppealDetailsPage)
      setFeatureDate(Some(LocalDate.of(2023, 8 ,14)))
      val request: Future[Result] = controller.onPageLoad()(fakeRequestWithPreviousId)
      await(request).header.status shouldBe Status.OK
      val parsedBody: Document = Jsoup.parse(contentAsString(request))
      metadataRowsTest(parsedBody)
      parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Reason for missing the VAT deadline"
      parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "Crime"
      parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "When did the crime happen?"
      parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "1 November 2023"
      parsedBody.select("#main-content dl > div:nth-child(7) > dt").text() shouldBe "Has this crime been reported to the police?"
      parsedBody.select("#main-content dl > div:nth-child(7) > dd.govuk-summary-list__value").text() shouldBe "Yes"
      parsedBody.select("#main-content dl > div:nth-child(8) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(8) > dd.govuk-summary-list__value").text() shouldBe "I forgot"
    }

    "return 200 (OK) when the user is authorised and display correct appeal details (other journey)" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "other",
      SessionKeys.whenDidBecomeUnable -> "2023-11-01",
      SessionKeys.whyReturnSubmittedLate -> "My staff forgot",
      SessionKeys.isUploadEvidence -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.lateAppealReason -> "I forgot"
    ), journeyId = Some("5678"))) {
      await(uploadRepository.updateStateOfFileUpload(journeyId = "5678", callbackModel = fileUploadModel, isInitiateCall = true))
      val request: Future[Result] = controller.onPageLoad()(fakeRequestWithPreviousId)
      await(request).header.status shouldBe Status.OK
      val parsedBody: Document = Jsoup.parse(contentAsString(request))
      metadataRowsTest(parsedBody)
      parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Reason for missing the VAT deadline"
      parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "The reason does not fit into any of the other categories"
      parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "When did the issue first stop you submitting the VAT Return?"
      parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "1 November 2023"
      parsedBody.select("#main-content dl > div:nth-child(7) > dt").text() shouldBe "Why was the return submitted late?"
      parsedBody.select("#main-content dl > div:nth-child(7) > dd.govuk-summary-list__value").text() shouldBe "My staff forgot"
      parsedBody.select("#main-content dl > div:nth-child(8) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(8) > dd.govuk-summary-list__value").text() shouldBe "Yes"
      parsedBody.select("#main-content dl > div:nth-child(9) > dt").text() shouldBe "Evidence to support this appeal"
      parsedBody.select("#main-content dl > div:nth-child(9) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
      parsedBody.select("#main-content dl > div:nth-child(10) > dt").text() shouldBe "Reason for appealing after 30 days"
      parsedBody.select("#main-content dl > div:nth-child(10) > dd.govuk-summary-list__value").text() shouldBe "I forgot"
    }

    "return 200 (OK) when the user is authorised and display correct appeal details (obligation journey)" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.otherRelevantInformation -> "This is some relevant information",
      SessionKeys.isUploadEvidence -> "yes"
    ), journeyId = Some("5678"))) {
      await(uploadRepository.updateStateOfFileUpload(journeyId = "5678", callbackModel = fileUploadModel, isInitiateCall = true))
      val request: Future[Result] = controller.onPageLoad()(fakeRequestWithPreviousId)
      await(request).header.status shouldBe Status.OK
      val parsedBody: Document = Jsoup.parse(contentAsString(request))
      metadataRowsTest(parsedBody)
      parsedBody.select("#main-content dl > div:nth-child(5) > dt").text() shouldBe "Tell us why you want to appeal the penalty"
      parsedBody.select("#main-content dl > div:nth-child(5) > dd.govuk-summary-list__value").text() shouldBe "This is some relevant information"
      parsedBody.select("#main-content dl > div:nth-child(6) > dt").text() shouldBe "Do you want to upload evidence to support your appeal?"
      parsedBody.select("#main-content dl > div:nth-child(6) > dd.govuk-summary-list__value").text() shouldBe "Yes"
      parsedBody.select("#main-content dl > div:nth-child(7) > dt").text() shouldBe "Evidence to support this appeal"
      parsedBody.select("#main-content dl > div:nth-child(7) > dd.govuk-summary-list__value").text() shouldBe "file1.txt"
    }

    "return 303 (SEE_OTHER) when user does not have 'previouslySubmittedJourneyId' session key" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ), journeyId = Some("5678"))){
      enableFeatureSwitch(ShowViewAppealDetailsPage)
      val request: Future[Result] = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.SEE_OTHER
      await(request).header.headers("Location") shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
    }

    "return 303 (SEE_OTHER) when user does not have any answers in Mongo" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ), journeyId = Some("5678"))) {
      val request: Future[Result] = controller.onPageLoad()(fakeRequest.withSession(SessionKeys.previouslySubmittedJourneyId -> "1235"))
      await(request).header.status shouldBe Status.SEE_OTHER
      await(request).header.headers("Location") shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/appeal-details").get())
      request.status shouldBe Status.SEE_OTHER
    }

    s"return 404 (NOT_FOUND) when the feature switch {$ShowViewAppealDetailsPage} is disabled"  in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.previouslySubmittedJourneyId -> "1234",
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      disableFeatureSwitch(ShowViewAppealDetailsPage)
      val request: Future[Result] = controller.onPageLoad()(fakeRequestWithPreviousId)
      await(request).header.status shouldBe Status.NOT_FOUND
    }
  }
}
