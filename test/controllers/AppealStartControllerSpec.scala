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
import config.featureSwitches.ShowFullAppealAgainstTheObligation
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.AppealStartPage

import scala.concurrent.Future

class AppealStartControllerSpec extends SpecBase {

  val page: AppealStartPage = injector.instanceOf[AppealStartPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockAppConfig)

    when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(true)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
  }

  object Controller extends AppealStartController(
    page
  )(stubMessagesControllerComponents(), implicitly, implicitly, authPredicate, dataRequiredAction, dataRetrievalAction, checkObligationAvailabilityAction)

  "AppealStartController" should {

    "onPageLoad" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = Controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "user does not have the correct session keys to start an appeal" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        s"redirect to appeal by letter page when the feature switch ($ShowFullAppealAgainstTheObligation) is disabled" +
          s" and its an obligation appeal" in new Setup(AuthTestModels.successfulAuthResult) {
          val answers: JsObject = correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)
          when(mockSessionService.getUserAnswers(any())).thenReturn(
            Future.successful(Some(userAnswers(answers))))
          when(mockAppConfig.isEnabled(ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation))).thenReturn(false)
          val result: Future[Result] = Controller.onPageLoad()(userRequestWithCorrectKeys.copy(answers = userAnswers(answers)))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
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
}
