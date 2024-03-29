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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import views.html.errors.ServiceUnavailablePage

import scala.concurrent.Future

class ServiceUnavailableControllerSpec extends SpecBase {
  val serviceUnavailablePage: ServiceUnavailablePage = injector.instanceOf[ServiceUnavailablePage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: ServiceUnavailableController = new ServiceUnavailableController(
      serviceUnavailablePage
    )(mcc, appConfig, authPredicate, dataRetrievalAction)
  }

  "onPageLoad" should {
    "show the page with 500 status code" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockSessionService.getUserAnswers(any()))
        .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
      val result: Future[Result] = controller.onPageLoad()(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "the user is unauthorised" when {
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
