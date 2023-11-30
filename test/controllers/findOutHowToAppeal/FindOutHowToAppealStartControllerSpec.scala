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

package controllers.findOutHowToAppeal

import base.SpecBase
import config.featureSwitches.ShowFindOutHowToAppealJourney
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status.{FORBIDDEN, NOT_FOUND, SEE_OTHER}
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.{ExecutionContext, Future}

class FindOutHowToAppealStartControllerSpec extends SpecBase {
  val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(true)

    val controller = new FindOutHowToAppealStartController(errorHandler, mockAppConfig)(mcc, authPredicate,
      dataRetrievalAction, config, ec)
  }

  "FindOutHowToAppealStartController" should {
    "startFindOutHowToAppeal" when {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) and redirect to the correct view" when {
          "Appealing a LPP (Non CA) as a trader" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(findOutHowToAppealLPPNonCaAnswers))))
            val result: Future[Result] = controller.startFindOutHowToAppeal()(vatTraderLPPUserRequest)
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad().url
          }
        }

        "return 404 (NOT_FOUND) when the feature switch is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(findOutHowToAppealLPPNonCaAnswers))))
          when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(false)
          val result: Future[Result] = controller.startFindOutHowToAppeal()(vatTraderLPPUserRequest)
          status(result) shouldBe NOT_FOUND
        }
      }

      "the user is unauthorised" must {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.startFindOutHowToAppeal()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.startFindOutHowToAppeal()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}
