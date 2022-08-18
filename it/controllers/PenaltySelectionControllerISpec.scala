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

import config.featureSwitches.FeatureSwitching
import models.{CheckMode, NormalMode, PenaltyTypeEnum}
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

class PenaltySelectionControllerISpec extends IntegrationSpecCommonBase with FeatureSwitching {
  val controller: PenaltySelectionController = injector.instanceOf[PenaltySelectionController]

  "GET /multiple-penalties-for-this-period" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.firstPenaltyAmount -> "100.01",
      SessionKeys.secondPenaltyAmount -> "100.02",
      SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30)
    ))) {
      val request = await(controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val request = await(controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/multiple-penalties-for-this-period").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "POST /multiple-penalties-for-this-period" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - routing to single penalty page when the answer is no" in
      new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.firstPenaltyAmount -> "100.01",
        SessionKeys.firstPenaltyChargeReference -> "123456789",
        SessionKeys.secondPenaltyAmount -> "100.02",
        SessionKeys.secondPenaltyChargeReference -> "123456790",
        SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30),
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      ))) {
        val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectBody))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers("Location") shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
        await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).get shouldBe "no"
      }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - routing to single penalty page when the answer is yes" in
      new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.firstPenaltyAmount -> "100.01",
        SessionKeys.secondPenaltyAmount -> "100.02",
        SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30),
        SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
      ))) {
        val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectBody))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers("Location") shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
        await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).get shouldBe "yes"
      }

    "return 400 (BAD_REQUEST)" when {
      "the value is empty" in new UserAnswersSetup(userAnswers(Json.obj(
        SessionKeys.penaltyNumber -> "1234",
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
        SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
        SessionKeys.firstPenaltyAmount -> "100.01",
        SessionKeys.secondPenaltyAmount -> "100.02",
        SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
      ))) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/multiple-penalties-for-this-period").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234")
      val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> "2020-01-01",
      SessionKeys.endDateOfPeriod -> "2020-01-01"
    ))) {
      val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/multiple-penalties-for-this-period").post(""))
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-single-penalty" should {
    "return 200 (OK) when the user is authorised - showing the correct link" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
      SessionKeys.firstPenaltyAmount -> "100.01",
      SessionKeys.secondPenaltyAmount -> "100.02"
    ))) {
      val normalModeRequest = controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForSinglePenaltySelection(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/appeal-single-penalty")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "1234")
      val request = await(controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/appeal-single-penalty").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }

  "GET /appeal-cover-for-both-penalties" should {
    "return 200 (OK) when the user is authorised - appeal < 30 days late" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      setFeatureDate(Some(LocalDate.parse("2020-02-28")))
      val normalModeRequest = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForAppealCoverBothPenalties(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
    }

    "return 200 (OK) when the user is authorised - appeal > 30 days late" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
      SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
    ))) {
      setFeatureDate(None)
      val normalModeRequest = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForAppealCoverBothPenalties(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(userAnswers(Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/appeal-cover-for-both-penalties").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
    ))) {
      val request = await(controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/appeal-cover-for-both-penalties").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
