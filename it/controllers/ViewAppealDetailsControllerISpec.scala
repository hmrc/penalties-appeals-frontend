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

import java.time.LocalDate

import config.featureSwitches.{FeatureSwitching, ShowViewAppealDetailsPage}
import controllers.testHelpers.AuthorisationTest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import scala.concurrent.Future

class ViewAppealDetailsControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest with FeatureSwitching {

  val controller: ViewAppealDetailsController = injector.instanceOf[ViewAppealDetailsController]

  val fakeRequestWithPreviousId: FakeRequest[AnyContent] = FakeRequest("POST", "/").withSession(
    authToken -> "1234",
    SessionKeys.previouslySubmittedJourneyId -> "1234"
  )

  "GET /appeal-details" should {
    "return 200 (OK) when the user is authorised and display correct appeal details" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.previouslySubmittedJourneyId -> "1234",
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))) {
      enableFeatureSwitch(ShowViewAppealDetailsPage)
      val request: Future[Result] = controller.onPageLoad()(fakeRequestWithPreviousId)
      await(request).header.status shouldBe Status.OK
      val parsedBody: Document = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content dl > div:nth-child(1) > dt").text() shouldBe "VAT registration number (VRN)"
      parsedBody.select("#main-content dl > div:nth-child(1) > dd.govuk-summary-list__value").text() shouldBe "123456789"
      parsedBody.select("#main-content dl > div:nth-child(2) > dt").text() shouldBe "Penalty appealed"
      parsedBody.select("#main-content dl > div:nth-child(2) > dd.govuk-summary-list__value").text() shouldBe "Late submission penalty: 1 January 2023 to 31 January 2023"
    }

    "return 303 (SEE_OTHER) when user does not have 'previouslySubmittedJourneyId' session key" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.lateAppealReason -> "I forgot"
    ))){
      enableFeatureSwitch(ShowViewAppealDetailsPage)
      val request: Future[Result] = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.SEE_OTHER
      await(request).header.headers("Location") shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
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
