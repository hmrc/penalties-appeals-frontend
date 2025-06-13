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

import config.featureSwitches.{FeatureSwitching, ShowReasonableExcuseHintText}
import controllers.testHelpers.AuthorisationTest
import models.PenaltyTypeEnum
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class ReasonableExcuseControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {
  val controller: ReasonableExcuseController = injector.instanceOf[ReasonableExcuseController]

  "GET /reason-for-missing-deadline" should {
    "return 200 (OK) when the user is authorised and the reasonable excuses can be fetched" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.regime -> "HMRC-MTD-VAT",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    //Testing at this level because the agent auth predicates are not setup at unit test level
    "the call succeeds - user is agent and client planned to return - show ’your client’ hint text" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whoPlannedToSubmitVATReturn -> "client"
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.agentAuthorised()
      val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
      val result = controller.onPageLoad()(agentFakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your client’s ability to meet the deadline."
      documentParsed.select(".govuk-radios__hint").get(0).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
    }

    "the call succeeds -  user is agent and agent missed deadline and was delayed - show ’your’ hint text" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
      SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.agentAuthorised()
      val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
      val result = controller.onPageLoad()(agentFakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your ability to meet the deadline."
      documentParsed.select(".govuk-radios__hint").get(0).text() shouldBe "You should only choose this if your reason is not covered by any of the other options."
    }

    "the call succeeds - user is agent and client missed the deadline - show ’your client’ hint text" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
      SessionKeys.whatCausedYouToMissTheDeadline -> "client"
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.agentAuthorised()
      val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
      val result = controller.onPageLoad()(agentFakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your client’s ability to meet the deadline."
      documentParsed.select(".govuk-radios__hint").get(0).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
    }

    "the call succeeds - user is agent appealing a LPP - show 'your client' hint text" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.agentAuthorised()
      val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
      val result = controller.onPageLoad()(agentFakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your client’s ability to meet the deadline."
      documentParsed.select(".govuk-radios__hint").get(0).text() shouldBe "You should only choose this if the reason is not covered by any of the other options."
    }

    "the call succeeds - user is agent appealing a trader - show 'your' hint text" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.authorised()
      val result = controller.onPageLoad()(fakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").text() shouldBe "If more than one reason applies, choose the one that had the most direct impact on your ability to meet the deadline."
      documentParsed.select(".govuk-radios__hint").get(0).text() shouldBe "You should only choose this if your reason is not covered by any of the other options."
    }

    "the call succeeds - not show hint text when feature switch is disabled" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      disableFeatureSwitch(ShowReasonableExcuseHintText)
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      AuthStub.authorised()
      val result = controller.onPageLoad()(fakeRequest)
      status(result) shouldBe Status.OK
      val documentParsed = Jsoup.parse(contentAsString(result))
      documentParsed.select("#value-hint").isEmpty shouldBe true
      documentParsed.select(".govuk-radios__hint").isEmpty shouldBe true
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in new UserAnswersSetup(userAnswers(Json.obj())) {
      failedFetchReasonableExcuseListResponse("HMRC-MTD-VAT")
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234",
        SessionKeys.regime -> "HMRC-MTD-VAT"
      )
      val request = await(controller.onPageLoad()(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onPageLoad(), "GET", "/reason-for-missing-deadline")
  }

  "POST /reason-for-missing-deadline" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "type1")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.HonestyDeclarationController.onPageLoad().url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.reasonableExcuse).get shouldBe "type1"
    }

    "return 400 (BAD REQUEST) when the user POSTs invalid data" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      successfulFetchReasonableExcuseResponse("HMRC-MTD-VAT")
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "this_is_fake")

      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.BAD_REQUEST
    }

    "return 500 (ISE) when the list can not be retrieved from the backend" in new UserAnswersSetup(userAnswers(Json.obj())) {
      failedFetchReasonableExcuseListResponse("HMRC-MTD-VAT")
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/reason-for-missing-deadline").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234",
        SessionKeys.regime -> "HMRC-MTD-VAT",
      )
      val request = await(controller.onSubmit()(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    runControllerPredicateTests(controller.onSubmit(), "POST", "/reason-for-missing-deadline")
  }
}
