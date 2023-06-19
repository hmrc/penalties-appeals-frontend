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
import config.featureSwitches.ShowFullAppealAgainstTheObligation
import models.UserRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import utils.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

class CheckObligationAvailabilityActionSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val testAction: Request[_] => Future[Result] = _ => Future.successful(Ok(""))

  class Harness(requiredAction: CheckObligationAvailabilityAction, request: UserRequest[_] = UserRequest("123456789",
    active = true, None, userAnswers(correctUserAnswers))(fakeRequest)) {
      def onPageLoad(): Future[Result] = requiredAction.invokeBlock(request, testAction)
  }

  class Setup {
    reset(mockAppConfig)
  }

  "refine" should {
    "run the block" when {
      "the appeal is not due to obligation" in new Setup {
        val request = UserRequest("123456789", answers = userAnswers(Json.obj()))
        val fakeController = new Harness(
          requiredAction = new CheckObligationAvailabilityActionImpl(
            mockAppConfig
          ),
          request = request
        )
        when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
        val result = await(fakeController.onPageLoad())
        result.header.status shouldBe OK
      }

      s"the appeal is due to obligation and the feature switch ($ShowFullAppealAgainstTheObligation) is enabled" in new Setup {
        val request = UserRequest("123456789", answers = userAnswers(Json.obj(
          SessionKeys.isObligationAppeal -> true
        )))
        val fakeController = new Harness(
          requiredAction = new CheckObligationAvailabilityActionImpl(
            mockAppConfig
          ),
          request = request
        )
        when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(true)
        val result = await(fakeController.onPageLoad())
        result.header.status shouldBe OK
      }
    }

    s"redirect to appeal by letter page when its an obligation appeal and the feature switch ($ShowFullAppealAgainstTheObligation) is disabled" in new Setup {
      val request = UserRequest("123456789", answers = userAnswers(Json.obj(
        SessionKeys.isObligationAppeal -> true
      )))
      val fakeController = new Harness(
        requiredAction = new CheckObligationAvailabilityActionImpl(
          mockAppConfig
        ),
        request = request
      )
      when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
      val result = await(fakeController.onPageLoad())
      result.header.status shouldBe SEE_OTHER
      result.header.headers(LOCATION) shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
    }
  }
}
