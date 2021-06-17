/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.{AuthStub, PenaltiesStub}
import utils.{IntegrationSpecCommonBase, SessionKeys}

class CheckYourAnswersControllerISpec extends IntegrationSpecCommonBase {
  val controller: CheckYourAnswersController = injector.instanceOf[CheckYourAnswersController]

  "GET /check-your-answers" should {
    "return 200 (OK) when the user is authorised and has the correct keys in session for crime" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
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
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "When did the person become unavailable?"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "1 January 2022"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
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
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.OK
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for crime - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Why you did not appeal sooner"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for fire or flood - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Why you did not appeal sooner"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for loss of staff - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Lorem ipsum"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(3) > dt").text() shouldBe "Why you did not appeal sooner"
      parsedBody.select("#main-content dl > div:nth-child(3) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 200 (OK) when the user is authorised and has the correct keys in session for technical issues - for a late appeal" in {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
        SessionKeys.lateAppealReason -> "Lorem ipsum"
      )
      val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(4) > dt").text() shouldBe "Why you did not appeal sooner"
      parsedBody.select("#main-content dl > div:nth-child(4) > dd.govuk-summary-list__value").text() shouldBe "Lorem ipsum"
    }

    "return 500 (ISE) when the user hasn't selected a reasonable excuse option" in {
      val fakeRequestWithMissingReasonableExcuse: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
      )
      val request = await(controller.onPageLoad()(fakeRequestWithMissingReasonableExcuse))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user has selected a reasonable excuse option but hasn't completed the journey" in {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
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
      PenaltiesStub.successfulAppealSubmission
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for fire or flood" in {
      PenaltiesStub.successfulAppealSubmission
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "fireOrFlood",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfFireOrFlood -> "2022-01-01"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for loss of staff" in {
      PenaltiesStub.successfulAppealSubmission
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "lossOfStaff",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for technical issues" in {
      PenaltiesStub.successfulAppealSubmission
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "technicalIssues",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
        SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
    }

    "redirect the user to the confirmation page on success for health" when {
      "there is no hospital stay" in {
        PenaltiesStub.successfulAppealSubmission
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2021-01-01T12:00:00"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there is a ongoing hospital stay" in {
        PenaltiesStub.successfulAppealSubmission
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.isHealthEventOngoing -> "yes",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "there has been a hospital stay that has ended" in {
        PenaltiesStub.successfulAppealSubmission
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
          SessionKeys.penaltyId -> "1234",
          SessionKeys.appealType -> "Late_Submission",
          SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
          SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
          SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.isHealthEventOngoing -> "no",
          SessionKeys.whenHealthIssueStarted -> "2021-01-01T12:00:00",
          SessionKeys.whenHealthIssueEnded -> "2021-01-02T12:00:00"
        )
        val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers(LOCATION) shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

    }

    "show an ISE when the appeal fails" in {
      PenaltiesStub.failedAppealSubmission
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/check-your-answers").withSession(
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
      )
      val request = await(controller.onSubmit()(fakeRequestWithCorrectKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
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
        SessionKeys.penaltyId -> "1234",
        SessionKeys.appealType -> "Late_Submission",
        SessionKeys.startDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.endDateOfPeriod -> "2020-01-01T12:00:00",
        SessionKeys.dueDateOfPeriod -> "2020-02-07T12:00:00",
        SessionKeys.dateCommunicationSent -> "2020-02-08T12:00:00",
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01"
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
