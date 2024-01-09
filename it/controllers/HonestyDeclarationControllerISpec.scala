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
import models.NormalMode
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import utils.{IntegrationSpecCommonBase, SessionKeys}

class HonestyDeclarationControllerISpec extends IntegrationSpecCommonBase with AuthorisationTest {
  val controller: HonestyDeclarationController = injector.instanceOf[HonestyDeclarationController]

  "GET /honesty-declaration" should {
    "return 200 (OK) when the user is authorised and has the correct keys" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.reasonableExcuse -> "crime"
      )
    )) {
      val request = await(controller.onPageLoad()(fakeRequest))
      request.header.status shouldBe Status.OK
    }

    "return 200 (OK) when the user is authorised and has the correct keys - and show one more bullet when reasonable excuse is 'lossOfStaff'" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.reasonableExcuse -> "lossOfStaff"
      )
    )) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(
        "#main-content .govuk-list--bullet > li:nth-child(2)").text() shouldBe "the staff member did not return or get replaced before the due date"
    }

    "return 200 (OK) when the user is authorised and has the correct keys - and no custom no extraText when reasonable excuse is 'other'" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.reasonableExcuse -> "other"
      )
    )) {
      val request = controller.onPageLoad()(fakeRequest)
      await(request).header.status shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text() should startWith("I was unable to submit the VAT Return due on ")
    }


    "when an agent is authorised and has the correct keys" must {
      "return 200 (OK) and the correct message - show agent context messages" in new UserAnswersSetup(userAnswers(
        answers = Json.obj(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"
        )
      )) {
        AuthStub.agentAuthorised()
        val agentFakeRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")
        val request = controller.onPageLoad()(agentFakeRequest)
        await(request).header.status shouldBe Status.OK
        val parsedBody = Jsoup.parse(contentAsString(request))
        parsedBody.select("#main-content .govuk-list--bullet > li:nth-child(1)").text should startWith("because my client was affected by a crime")
      }
      
    }
  }

  "POST /honesty-declaration" should {
    "return 303 (SEE OTHER) when the user POSTs valid data - and the calls succeed - adding the key to the session" in new UserAnswersSetup(userAnswers(
      answers = Json.obj(
        SessionKeys.reasonableExcuse -> "crime"
      )
    )) {
      val fakeRequestWithCorrectBody: FakeRequest[AnyContent] = fakeRequest.withFormUrlEncodedBody("value" -> "true")
      val request = await(controller.onSubmit()(fakeRequestWithCorrectBody))
      request.header.status shouldBe Status.SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      await(userAnswersRepository.getUserAnswer("1234")).get.getAnswer[Boolean](SessionKeys.hasConfirmedDeclaration).get shouldBe true
    }

    runControllerPredicateTests(controller.onSubmit(), "POST", "/honesty-declaration")

  }

}
