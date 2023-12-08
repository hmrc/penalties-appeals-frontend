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
import org.mockito.Mockito.{mock, reset, when}
import play.api.Configuration
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import services.TimeToPayService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TimeToPayControllerSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val hc: HeaderCarrier = HeaderCarrier()
  val mockTTPService: TimeToPayService = mock(classOf[TimeToPayService])
  val mockConfig: Configuration = mock(classOf[Configuration])

  class Setup(featureSwitch: Boolean = true) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockTTPService)
    reset(mockAppConfig)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(AuthTestModels.successfulAuthResult)

    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFindOutHowToAppealJourney))).thenReturn(featureSwitch)

    val controller = new TimeToPayController(mcc, mockTTPService, errorHandler)(ec, mockAppConfig, authPredicate, dataRetrievalAction, mockConfig)
  }

  "redirect" should {
    "the user is authorised" must {
      "return 303 (SEE_OTHER)" in new Setup {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockConfig.get[Boolean](ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(true)
        when(mockTTPService.retrieveRedirectUrl(hc, ec, mockAppConfig)).thenReturn(Future.successful(Right("/correct-url")))
        val result: Future[Result] = controller.redirect(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
