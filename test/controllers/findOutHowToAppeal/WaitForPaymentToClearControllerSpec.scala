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
import models.session.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.findOutHowToAppeal.WaitForPaymentToClearPage

import scala.concurrent.{ExecutionContext, Future}

class WaitForPaymentToClearControllerSpec extends SpecBase {
  val WaitForPaymentToClearPage: WaitForPaymentToClearPage = injector.instanceOf[WaitForPaymentToClearPage]
  val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]], extraSessionData: JsObject = Json.obj()) {
    val sessionAnswers: UserAnswers = userAnswers(correctUserAnswers ++ extraSessionData)
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockSessionService.getUserAnswers(any()))
      .thenReturn(Future.successful(Some(sessionAnswers)))

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller = new WaitForPaymentToClearController(WaitForPaymentToClearPage, errorHandler)(mcc, mockAppConfig,
      authPredicate, dataRetrievalAction, config)
  }

  "WaitForPaymentToClearController" should {
    "onPageLoad" when {
      "the user is authorised" must {
        "return 200 (OK) and the correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.isCaLpp -> false)))))

          val result: Future[Result] = controller.onPageLoad()(fakeRequest)
          status(result) shouldBe OK
        }

        "return 404 (NOT_FOUND) when not an Non CA Appeal" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.isCaLpp -> true)))))
          val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe NOT_FOUND
        }
      }
    }
  }
}
