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

import controllers.testHelpers.AuthorisationTest
import models.{NormalMode, PenaltyTypeEnum}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class AppealAgainstObligationControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: AppealAgainstObligationController = injector.instanceOf[AppealAgainstObligationController]

  "GET /other-relevant-information" should {
    "return 200 (OK) when the user is authorised" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId-> "1234")
      val request = await(controller.onPageLoad(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    runControllerPredicateTests(controller.onPageLoad(NormalMode), "GET", "/other-relevant-information")
  }

  "POST /other-relevant-information" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new UserAnswersSetup(userAnswers()) {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information")
        .withSession(
          authToken -> "1234",
          SessionKeys.journeyId -> "1234")
        .withFormUrlEncodedBody("other-relevant-information-text" -> "Other Reason")
      val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[String](SessionKeys.otherRelevantInformation).get shouldBe "Other Reason"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/other-relevant-information")
          .withSession(authToken -> "1234",
            SessionKeys.journeyId -> "1234")
        val request = await(controller.onSubmit(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe Status.BAD_REQUEST
      }

      "invalid characters are entered" in new UserAnswersSetup(userAnswers()) {
        val fakeRequestWithInvalidChars: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("other-relevant-information-text" -> "コし")

        val result: Result = await(controller.onSubmit(NormalMode)(fakeRequestWithInvalidChars))
        result.header.status shouldBe Status.BAD_REQUEST
      }
    }

    runControllerPredicateTests(controller.onSubmit(NormalMode), "POST", "/other-relevant-information")
  }
}
