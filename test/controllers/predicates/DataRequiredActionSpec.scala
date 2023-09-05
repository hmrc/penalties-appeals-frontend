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

package controllers.predicates

import base.SpecBase
import models.UserRequest
import models.session.UserAnswers
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SessionKeys

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val testAction: Request[_] => Future[Result] = _ => Future.successful(Ok(""))

  class Harness(requiredAction: DataRequiredAction, request: UserRequest[_] = UserRequest("123456789",
    active = true, None, userAnswers(correctUserAnswers))(fakeRequest)) {
      def onPageLoad(): Future[Result] = requiredAction.invokeBlock(request, testAction)
  }

  "refine" should {
    s"show an ISE (${Status.INTERNAL_SERVER_ERROR}) when all of the data is missing as part of the session" in {
        val requestWithNoSessionKeys = UserRequest("123456789", answers = userAnswers(Json.obj()))
        val fakeController = new Harness(
          requiredAction = new DataRequiredActionImpl(
            errorHandler
          ),
          request = requestWithNoSessionKeys)

      val result = await(fakeController.onPageLoad())
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    s"show an ISE (${Status.INTERNAL_SERVER_ERROR}) when some of the data is missing as part of the session" in {
      val requestWithPartSessionKeys = UserRequest("123456789", answers = userAnswers(
        Json.obj(
          SessionKeys.penaltyNumber -> "123",
          SessionKeys.appealType -> "this is an appeal",
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01")
        )
      ))
      val fakeController = new Harness(
        requiredAction = new DataRequiredActionImpl(
          errorHandler
        ),
        request = requestWithPartSessionKeys)

      val result = await(fakeController.onPageLoad())
      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to the appeal has already been submitted page" when {
      "the user has seen the appeal confirmation page" in {
        val requestWithConfirmationSessionKeys = UserRequest("123456789", answers = UserAnswers("")
        )(FakeRequest("GET", "/").withSession(SessionKeys.journeyId -> "journey1", SessionKeys.penaltiesAppealHasBeenSubmitted -> "true"))
        val fakeController = new Harness(
          requiredAction = new DataRequiredActionImpl(
            errorHandler
          ),
          request = requestWithConfirmationSessionKeys
        )

        val result = await(fakeController.onPageLoad())
        result.header.status shouldBe SEE_OTHER
        result.header.headers(LOCATION) shouldBe controllers.routes.YouCannotGoBackToAppealController.onPageLoad().url
      }
    }

    "run the block when all of the correct data is present in the session" in {
      val fakeController = new Harness(
        requiredAction = new DataRequiredActionImpl(
          errorHandler
        ),
        request = userRequestWithCorrectKeys)

      val result = await(fakeController.onPageLoad())
      result.header.status shouldBe OK
    }
  }
}
