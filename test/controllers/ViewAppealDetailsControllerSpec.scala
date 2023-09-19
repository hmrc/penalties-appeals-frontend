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

import base.SpecBase
import helpers.SessionAnswersHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.SessionService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import views.html.ViewAppealDetailsPage
import config.featureSwitches.ShowViewAppealDetailsPage
import org.mockito.ArgumentMatchers

import scala.concurrent.{ExecutionContext, Future}

class ViewAppealDetailsControllerSpec extends SpecBase {
  val viewAppealDetailsPage: ViewAppealDetailsPage = injector.instanceOf[ViewAppealDetailsPage]
  val sessionAnswersHelper: SessionAnswersHelper = injector.instanceOf[SessionAnswersHelper]
  val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

  val controller = new ViewAppealDetailsController(viewAppealDetailsPage, sessionAnswersHelper, mockSessionService, errorHandler)(mcc, config, appConfig, authPredicate, ec)

  "ViewAppealDetailsController" should {

    "onPageLoad" when {
      "the user is authorised" must {
        "return 200 (OK) and the correct view" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
            status(result) shouldBe OK
          }

        "return 303 (SEE_OTHER) when they do not have user answers" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(None))
            val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoad().url
          }

        s"return 404 (NOT_FOUND) when the feature switch {$ShowViewAppealDetailsPage} is disabled" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowViewAppealDetailsPage))).thenReturn(false)
            val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
            status(result) shouldBe NOT_FOUND
          }
        }

        "the user is unauthorised" must {
          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoad()(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoad()(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }
    }
  }
}
