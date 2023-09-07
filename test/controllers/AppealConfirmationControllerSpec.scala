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
import models.UserRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.AppealConfirmationPage

import scala.concurrent.{ExecutionContext, Future}

class AppealConfirmationControllerSpec extends SpecBase {
  val page: AppealConfirmationPage = injector.instanceOf[AppealConfirmationPage]
  val sessionAnswersHelper: SessionAnswersHelper = injector.instanceOf[SessionAnswersHelper]
  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  def fakeRequest(answers: JsObject, fakeRequest: FakeRequest[AnyContent] = fakeRequest): UserRequest[AnyContent] = fakeRequestConverter(answers, fakeRequest)

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
  }

  object Controller extends AppealConfirmationController(
    mockSessionService,
    page
  )(implicitly, implicitly, implicitly, stubMessagesControllerComponents(), authPredicate)

  "onPageLoad" should {
    "the user is authorised" when {
      "show the confirmation page and remove all non-confirmation screen session keys (loading data from Mongo)" in new Setup(AuthTestModels.successfulAuthResult) {
        val fakeRequestWithPreviouslySubmittedJourneyIdKey: FakeRequest[AnyContent] = FakeRequest("GET", "/").withSession(SessionKeys.previouslySubmittedJourneyId -> "1235")
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(crimeAnswers, fakeRequestWithPreviouslySubmittedJourneyIdKey))
        await(result).header.status shouldBe OK
        await(result).session.data.keys.toSet.subsetOf(SessionKeys.allKeys.toSet) shouldBe false
      }

      "redirect the user to the no journey data error page when the user is missing the 'previouslySubmittedJourneyId' key" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest(crimeAnswers, fakeRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
