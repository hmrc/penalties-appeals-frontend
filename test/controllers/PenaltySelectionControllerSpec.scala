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
import navigation.Navigation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, status}
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.PenaltySelectionPage
import play.api.test.Helpers._

import scala.concurrent.Future

class PenaltySelectionControllerSpec extends SpecBase {
  val mockNavigator = mock(classOf[Navigation])
  val page = injector.instanceOf[PenaltySelectionPage]
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
  }
  val controller = new PenaltySelectionController(page, mockNavigator)(stubMessagesControllerComponents(), implicitly, authPredicate, dataRequiredAction)

  "onPageLoadForPenaltySelection" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated radio option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(
          fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.doYouWantToAppealBothPenalties -> "yes")))
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#value").hasAttr("checked") shouldBe true
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onPageLoadForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onSubmitForPenaltySelection" should {
    "the user is authorised" when {
      "redirect the user to the single penalty page when no is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody("value" -> "no"))
        status(result) shouldBe SEE_OTHER
        //TODO: check that the redirect is correct
        redirectLocation(result).get shouldBe ""
      }

      "redirect the user to the multiple penalty page when yes is selected" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody("value" -> "yes"))
        status(result) shouldBe SEE_OTHER
        //TODO: check that the redirect is correct
        redirectLocation(result).get shouldBe ""
      }

      "return a 400 (BAD REQUEST) and show page with error when no option has been selected" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequestWithCorrectKeys
          .withFormUrlEncodedBody("value" -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = controller.onSubmitForPenaltySelection(NormalMode)(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
