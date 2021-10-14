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
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{status, _}
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.CancelVATRegistrationPage

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class CancelVATRegistrationControllerSpec extends SpecBase {
  val cancelVATRegistrationPage: CancelVATRegistrationPage = injector.instanceOf[CancelVATRegistrationPage]

  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit val appealService: AppealService = injector.instanceOf[AppealService]

  class Setup(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: CancelVATRegistrationController = new CancelVATRegistrationController(
      cancelVATRegistrationPage,
      mainNavigator
    )(authPredicate, dataRequiredAction, appealService, appConfig, ec, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "CancelVATRegistrationController" should {
    "onPageLoadForCancelVATRegistration" should {
      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }
      }
      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "return OK and correct view (pre-populated option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoadForCancelVATRegistration()(fakeRequestConverter(fakeRequestWithCorrectKeys.
          withSession(SessionKeys.cancelVATRegistration -> "yes")))
        status(result) shouldBe OK
      }
      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }
        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForCancelVATRegistration" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing when a valid option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": "yes"
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.cancelVATRegistration).get shouldBe "yes"
        }
      }
      "the user is unauthorised" when {
        "return 400 (BAD_REQUEST) when a no option is selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": ""
                |}
                |""".stripMargin))))
          status(result) shouldBe BAD_REQUEST
        }
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }
        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForCancelVATRegistration()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}
