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
import connectors.httpParsers.UnexpectedFailure
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.mockito.ArgumentMatchers
import play.api.Configuration
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import services.PayNowService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class PayNowControllerSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val hc: HeaderCarrier = HeaderCarrier()
  val mockPayNowService: PayNowService = mock(classOf[PayNowService])
  val mockConfig: Configuration = mock(classOf[Configuration])


  class Setup(featureSwitch: Boolean = true) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockPayNowService)
    reset(mockAppConfig)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(AuthTestModels.successfulAuthResult)

    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(featureSwitch)

    val controller = new PayNowController(mcc, mockPayNowService, errorHandler)(ec, mockAppConfig, authPredicate, dataRetrievalAction, mockConfig)
  }

  "redirect" should {
  "the user is authorised" must {
  "return 303 (SEE_OTHER) when the feature switch is on" in new Setup {
  when(mockSessionService.getUserAnswers(any()))
  .thenReturn(Future.successful(Some(userAnswers(findOutHowToAppealLPPNonCaAnswers))))
  when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
  when(mockPayNowService.retrieveRedirectUrl(any, any, any, any)(any, any)).thenReturn(Future.successful(Right("/correct-url")))
  val result: Future[Result] = controller.redirect(fakeRequest)
  status(result) shouldBe SEE_OTHER
  redirectLocation(result) shouldBe Some("/correct-url")
}

  "return 500 (ISE) when service returns an error" in new Setup {
  when(mockSessionService.getUserAnswers(any()))
  .thenReturn(Future.successful(Some(userAnswers(findOutHowToAppealLPPNonCaAnswers))))
  when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
  when(mockPayNowService.retrieveRedirectUrl(any, any, any, any)(any, any)).thenReturn(Future.successful(Left(UnexpectedFailure(500, "/correct-url"))))
  val result: Future[Result] = controller.redirect(fakeRequest)
  status(result) shouldBe INTERNAL_SERVER_ERROR
}

  "return 404 (NOT_FOUND) when the feature switch is off" in new Setup(false) {
  when(mockSessionService.getUserAnswers(any()))
  .thenReturn(Future.successful(Some(userAnswers(findOutHowToAppealLPPNonCaAnswers))))
  when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
  when(mockPayNowService.retrieveRedirectUrl(any, any, any, any)(any, any)).thenReturn(Future.successful(Right("/correct-url")))
  val result: Future[Result] = controller.redirect(fakeRequest)
  status(result) shouldBe NOT_FOUND
}
}
}
}
