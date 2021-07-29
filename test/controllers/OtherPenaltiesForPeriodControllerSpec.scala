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
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import testUtils.AuthTestModels
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import views.html.OtherPenaltiesForPeriodPage

import scala.concurrent.Future

class OtherPenaltiesForPeriodControllerSpec extends SpecBase {
  val otherPenaltiesForPeriodPage: OtherPenaltiesForPeriodPage = injector.instanceOf[OtherPenaltiesForPeriodPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: OtherPenaltiesForPeriodController = new OtherPenaltiesForPeriodController(
      otherPenaltiesForPeriodPage,
      errorHandler,
      mainNavigator
    )(mcc, appConfig, authPredicate, dataRequiredAction)
  }

  "OtherPenaltiesForPeriodController" should {

    "onPageLoad" when {
      "the user is authorised" must {
        "return 200 (OK) and the correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
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

    "onSubmit" when {
      "the user selects 'continue'" must {
        "return 300 (SEE_OTHER) and redirect to the correct page" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withJsonBody(
              Json.parse(
                """
                  |{
                  | "value": "true"
                  |}
                  |""".stripMargin
              )))
          )
          status(result) shouldBe SEE_OTHER
          await(result).header
        }

        "return 400 (BAD_REQUEST)" when {
          "the user has posted an empty body" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmit()(userRequestWithCorrectKeys)
            status(result) shouldBe BAD_REQUEST
          }

          "the user has changed the hidden value" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
              .withJsonBody(
                Json.parse(
                  """
                    |{
                    | "value": "fake_value"
                    |}
                    |""".stripMargin)
              )))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmit()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmit()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }

      }
    }

  }
}