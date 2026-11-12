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

import config.featureSwitches.FeatureSwitching
import controllers.testHelpers.AuthorisationTest
import models.{CheckMode, NormalMode, PenaltyTypeEnum}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class PenaltySelectionControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {
  val controller: PenaltySelectionController = injector.instanceOf[PenaltySelectionController]

  "GET /multiple-penalties-for-this-period" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.firstPenaltyAmount -> "100.01",
        SessionKeys.secondPenaltyAmount -> "100.02",
        SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30)
      )
    )) {
      val request = await(controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    runControllerPredicateTests(controller.onPageLoadForPenaltySelection(NormalMode), "GET", "/multiple-penalties-for-this-period")
  }

  "POST /multiple-penalties-for-this-period" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - routing to single penalty page when the answer is no" in
      new UserAnswersSetup(userAnswers(
        answers = Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.firstPenaltyAmount -> "100.01",
          SessionKeys.firstPenaltyChargeReference -> "123456789",
          SessionKeys.secondPenaltyAmount -> "100.02",
          SessionKeys.secondPenaltyChargeReference -> "123456790",
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30),
          SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
        )
      )) {
        val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "no")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectBody))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers("Location") shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
        await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).get shouldBe "no"
      }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - routing to single penalty page when the answer is yes" in
      new UserAnswersSetup(userAnswers(
        answers = Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.firstPenaltyAmount -> "100.01",
          SessionKeys.secondPenaltyAmount -> "100.02",
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().minusDays(30),
          SessionKeys.dateCommunicationSent -> LocalDate.now().minusDays(20)
        )
      )) {
        val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "yes")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectBody))
        request.header.status shouldBe Status.SEE_OTHER
        request.header.headers("Location") shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
        await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).get shouldBe "yes"
      }

    "return 400 (BAD_REQUEST)" when {
      "the value is empty" in new UserAnswersSetup(userAnswers(
        answers = Json.obj(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.firstPenaltyAmount -> "100.01",
          SessionKeys.secondPenaltyAmount -> "100.02",
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08")
        )
      )) {
        val fakeRequestWithInvalidBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "")
        val request = await(controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithInvalidBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmitForPenaltySelection(NormalMode), "POST", "/multiple-penalties-for-this-period")
  }

  "GET /appeal-single-penalty" should {
    "return 200 (OK) when the user is authorised - showing the correct link" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
        SessionKeys.firstPenaltyAmount -> "100.01",
        SessionKeys.secondPenaltyAmount -> "100.02"
      )
    )) {
      val normalModeRequest = controller.onPageLoadForSinglePenaltySelection(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForSinglePenaltySelection(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
    }

    runControllerPredicateTests(controller.onPageLoadForSinglePenaltySelection(NormalMode), "GET", "/appeal-single-penalty")
  }

  "GET /appeal-cover-for-both-penalties" should {
    "return 200 (OK) when the user is authorised - appeal < 30 days late" in new UserAnswersSetup(userAnswers()) {
      setFeatureDate(Some(LocalDate.parse("2020-02-28")))
      val normalModeRequest = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForAppealCoverBothPenalties(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
    }

    "return 200 (OK) when the user is authorised - appeal > 30 days late" in new UserAnswersSetup(userAnswers()) {
      setFeatureDate(None)
      val normalModeRequest = controller.onPageLoadForAppealCoverBothPenalties(NormalMode)(fakeRequest)
      await(normalModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(normalModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url

      val checkModeRequest = controller.onPageLoadForAppealCoverBothPenalties(CheckMode)(fakeRequest)
      await(checkModeRequest).header.status shouldBe Status.OK
      Jsoup.parse(contentAsString(checkModeRequest)).select("#main-content a.govuk-button").attr("href") shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }

    runControllerPredicateTests(controller.onPageLoadForAppealCoverBothPenalties(NormalMode), "GET", "/appeal-cover-for-both-penalties")
  }
}
