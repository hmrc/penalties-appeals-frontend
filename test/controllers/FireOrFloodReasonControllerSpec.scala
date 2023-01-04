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
import models.session.UserAnswers
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.fireOrFlood.WhenDidFireOrFloodHappenPage

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FireOrFloodReasonControllerSpec extends SpecBase {
  val fireOrFloodPage: WhenDidFireOrFloodHappenPage = injector.instanceOf[WhenDidFireOrFloodHappenPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector, mockSessionService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: FireOrFloodReasonController = new FireOrFloodReasonController(
      fireOrFloodPage,
      mainNavigator,
      mockSessionService
    )(authPredicate, dataRequiredAction, dataRetrievalAction, ec, appConfig, mcc)

    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))
  }

  "FireOrFloodReasonController" should {
    "onPageLoad" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2021-01-01"))))))
          val result: Future[Result] = controller.onPageLoad(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoad(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }

      }

    }

    "onSubmit" when {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.dateOfFireOrFlood -> LocalDate.of(2021, 2, 1)
          )
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmit(CheckMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
            "date.day" -> "1",
            "date.month" -> "2",
            "date.year" -> "2021"
          )))
          status(result) shouldBe SEE_OTHER
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(
            SessionKeys.dateOfFireOrFlood -> LocalDate.of(2021, 2, 1)
          )
          redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }

        "return 400 (BAD_REQUEST)" when {
          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "what",
              "date.month" -> "is",
              "date.year" -> "this"
            )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "31",
              "date.month" -> "2",
              "date.year" -> "2021"
            )))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            when(mockSessionService.getUserAnswers(any()))
              .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
            val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequestConverter(fakeRequest = fakeRequest.withFormUrlEncodedBody(
              "date.day" -> "123456",
              "date.month" -> "789101",
              "date.year" -> "123456789"
            )))
            status(result) shouldBe BAD_REQUEST
          }
        }

      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmit(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }

    }
  }
}
