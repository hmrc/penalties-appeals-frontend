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
import models.AuthRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val testAction: Request[_] => Future[Result] = _ => Future.successful(Ok(""))

  class Harness(retrievalAction: DataRetrievalAction, request: AuthRequest[_] = AuthRequest("123456789",
    active = true, None)(fakeRequest)) {
    def onPageLoad(): Future[Result] = retrievalAction.invokeBlock(request, testAction)
  }

  "refine" should {
    "return OK" when {
      "there is session data" in {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(None))
        val requestWithNoSessionKeys = AuthRequest("123456789")(fakeRequest)
        val fakeController = new Harness(
          retrievalAction = new DataRetrievalActionImpl(
            mockSessionService,
            errorHandler
          ),
          request = requestWithNoSessionKeys)
        val result = fakeController.onPageLoad()
        status(result) shouldBe OK
      }

      "there is no session data" in {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(None))
        val requestWithNoSessionKeys = AuthRequest("123456789")(fakeRequest)
        val fakeController = new Harness(
          retrievalAction = new DataRetrievalActionImpl(
            mockSessionService,
            errorHandler
          ),
          request = requestWithNoSessionKeys)
        val result = fakeController.onPageLoad()
        status(result) shouldBe OK
      }
    }

    "navigate to the incomplete session data page" when {
      "no journey ID is in the session" in {
        val requestWithNoSessionKeys = AuthRequest("123456789")(FakeRequest("GET", "/"))
        val fakeController = new Harness(
          retrievalAction = new DataRetrievalActionImpl(
            mockSessionService,
            errorHandler
          ),
          request = requestWithNoSessionKeys)
        val result = fakeController.onPageLoad()
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
      }
    }

    "render an ISE" when {
      "data cannot be retrieved from the session service" in {
        when(mockSessionService.getUserAnswers(any())).thenReturn(Future.failed(new Exception("I broke!")))
        val fakeController = new Harness(
          retrievalAction = new DataRetrievalActionImpl(
            mockSessionService,
            errorHandler
          ))
        val result = fakeController.onPageLoad()
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
