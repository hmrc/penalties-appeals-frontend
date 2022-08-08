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

import models.{NormalMode, PenaltyTypeEnum}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class HonestyDeclarationControllerISpec extends IntegrationSpecCommonBase {
  val controller: HonestyDeclarationController = injector.instanceOf[HonestyDeclarationController]

  "GET /honesty-declaration" should {
    "return 200 (OK) when the user is authorised and has the correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 200 (OK) when the user is authorised and has the correct keys - and show one more bullet when reasonable excuse is 'lossOfStaff'" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "lossOfStaff"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(
        "#main-content .govuk-list--bullet > li:nth-child(2)").text() shouldBe "the staff member did not return or get replaced before the due date"
    }

    "return 200 (OK) when the user is authorised and has the correct keys - and no custom no extraText when reasonable excuse is 'other'" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "other"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text() should startWith("I was unable to submit the VAT Return due on ")
    }

    "return 200 (OK) when the user is authorised and has the correct keys - and show the correct text when the user is appealing an obligation" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.isObligationAppeal -> true,
      SessionKeys.reasonableExcuse -> "obligation",
      SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
      SessionKeys.whatCausedYouToMissTheDeadline -> "client"
    ))) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text() should startWith("HMRC has been asked to cancel the VAT registration")
      parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(2)").text() should startWith("I believe there was no VAT Return due for the period")
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/honesty-declaration").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/honesty-declaration").get())
      request.status shouldBe Status.SEE_OTHER
    }


    "when an agent is authorised and has the correct keys" must {
      "return 200 (OK) and the correct message - show agent context messages" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.whatCausedYouToMissTheDeadline -> "client"
      ))) {
        AuthStub.agentAuthorised()
        val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
        val request = controller.onPageLoad()(agentFakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text should startWith("because my client was affected by a crime")
      }


      "return 200 (OK) when the user is authorised and has the correct keys - and show the correct text when the agent is appealing an obligation" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
        SessionKeys.isObligationAppeal -> true
      ))) {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
        val request = controller.onPageLoad()(fakeRequestWithCorrectKeys)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text() should startWith("HMRC has been asked to cancel the VAT registration")
        parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(2)").text() should startWith("I believe there was no VAT Return due for the period")
      }
    }
  }

  "POST /honesty-declaration" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.reasonableExcuse -> "crime"
    ))) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "true")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get shouldBe true
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/honesty-declaration").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      val request = await(controller.onSubmit()(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/honesty-declaration").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

}
