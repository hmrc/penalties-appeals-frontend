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
import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.SessionKeys
import utils.SessionKeys._

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val testAction: Request[_] => Future[Result] = _ => Future.successful(Ok(""))

  class Harness(requiredAction: DataRequiredAction,
                request: UserRequest[_] = UserRequest("123456789", active = true, None, userAnswers(correctUserAnswers))(fakeRequest)) {
    def onPageLoad(): Future[Result] = requiredAction.invokeBlock(request, testAction)
  }

  private def buildRequestWithAnswersAndSessionData(userAnswers: UserAnswers, sessionData: (String, String)*): UserRequest[_] =
    UserRequest("123456789", answers = userAnswers)(FakeRequest("GET", "/").withSession(sessionData: _*))

  private def buildControllerWithRequest(request: UserRequest[_]): Harness = new Harness(
    requiredAction = new DataRequiredActionImpl(errorHandler),
    request = request
  )

  "refine" should {
    "return a success with user request" when {
      "the user has a journeyId and the UserAnswers contain all the required fields" in {
        val requestWithConfirmationSessionKeys = buildRequestWithAnswersAndSessionData(
          userAnswers(correctUserAnswers),
          sessionData = SessionKeys.journeyId -> "1234"
        )
        val controller = buildControllerWithRequest(requestWithConfirmationSessionKeys)

        val result = await(controller.onPageLoad())

        result.header.status shouldBe OK
      }
    }

    "redirect to the YouCannotGoBackToAppeal page" when {
      "the user has a journeyId and has seen the appeal confirmation page so 'penaltiesHasSeenConfirmationPage' is defined in session" in {
        val requestWithConfirmationSessionKeys = buildRequestWithAnswersAndSessionData(
          userAnswers(correctUserAnswers),
          sessionData = SessionKeys.penaltiesHasSeenConfirmationPage -> "true",
          SessionKeys.journeyId -> "1234"
        )

        val result = await(buildControllerWithRequest(requestWithConfirmationSessionKeys).onPageLoad())

        result.header.status shouldBe SEE_OTHER
        result.header.headers(LOCATION) shouldBe controllers.routes.YouCannotGoBackToAppealController.onPageLoad().url
      }
    }

    val requiredUserAnswerFields = Seq(penaltyNumber, appealType, startDateOfPeriod, endDateOfPeriod, dueDateOfPeriod)

    "return an InternalServerError" when {
      requiredUserAnswerFields.foreach { field =>
        s"missing required UserAnswers field: '$field'" in {
          val userAnswersWithMissingField = correctUserAnswers - field
          val requestWithMissingAnswer = buildRequestWithAnswersAndSessionData(
            userAnswers(userAnswersWithMissingField),
            sessionData = SessionKeys.journeyId -> "1234"
          )

          val result = await(buildControllerWithRequest(requestWithMissingAnswer).onPageLoad())

          result.header.status shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "journeyId is missing" in {
        val requestWithMissingAnswers = buildRequestWithAnswersAndSessionData(
          userAnswers(correctUserAnswers)
        )

        val result = await(buildControllerWithRequest(requestWithMissingAnswers).onPageLoad())

        result.header.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
