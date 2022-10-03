/*
 * Copyright 2022 HM Revenue & Customs
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
import models.NormalMode
import models.pages.ReasonableExcuseSelectionPage
import navigation.Navigation
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Call
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.Future

class PreviousPageControllerSpec extends SpecBase {
  val mockNavigator = mock(classOf[Navigation])
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector, mockSessionService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller = new PreviousPageController(mockNavigator)(mcc, authPredicate, dataRetrievalAction, dataRequiredAction)
  }

  "previousPage" should {
    "redirect to the previous page relative to the given page" in new Setup(AuthTestModels.successfulAuthResult) {
      when(mockNavigator.previousPage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Call("", "/url"))
      when(mockSessionService.getUserAnswers(any()))
        .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
      val result = controller.previousPage(ReasonableExcuseSelectionPage.toString, NormalMode, false)(userRequestWithCorrectKeys)
      status(result) shouldBe SEE_OTHER
    }
  }
}
