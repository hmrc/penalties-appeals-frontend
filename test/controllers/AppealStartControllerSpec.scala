/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.Mockito._
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.AppealStartPage

import scala.concurrent.Future

class AppealStartControllerSpec extends SpecBase {

  val page: AppealStartPage = injector.instanceOf[AppealStartPage]

  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)
  }

  object Controller extends AppealStartController(
    page
  )(stubMessagesControllerComponents(), implicitly, authPredicate, dataRequiredAction)

  "IndexController" should {

    "onPageLoad" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "user does not have the correct session keys to start an appeal" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
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
