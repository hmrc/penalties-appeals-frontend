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
import views.html.MakingALateAppealPage

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class MakingALateAppealControllerSpec extends SpecBase {
  val makingALateAppealPage: MakingALateAppealPage = injector.instanceOf[MakingALateAppealPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockDateTimeHelper)
    reset(mockSessionService)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    val controller = new MakingALateAppealController(
      makingALateAppealPage,
      mockDateTimeHelper,
      mockSessionService
    )(mcc, appConfig, authPredicate, dataRetrievalAction, dataRequiredAction, ec)
  }

  "onPageLoad" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated textbox when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.lateAppealReason -> "This is a reason.")))))
        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe OK
        val documentParsed: Document = Jsoup.parse(contentAsString(result))
        documentParsed.select("#late-appeal-text").first().text() shouldBe "This is a reason."
      }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is unauthorised" when {

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

  "onSubmit" should {
    "the user is authorised" when {
      "redirect the user to the CYA page when an appeal reason has been entered " +
        "- adding the key to the session with a non-empty value" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionService.updateAnswers(answerCaptor.capture()))
          .thenReturn(Future.successful(true))
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "crime"), fakeRequest.withFormUrlEncodedBody("late-appeal-text" -> "Royale with cheese")))
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.lateAppealReason -> "Royale with cheese")
      }

      "return a 400 (BAD REQUEST) and show page with error when an appeal reason has NOT been entered" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "crime"), fakeRequest.withFormUrlEncodedBody("late-appeal-text" -> "")))
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) should include("There is a problem")
        contentAsString(result) should include("You must provide some information about why you did not appeal sooner")
      }

      "return 400 (BAD_REQUEST) when the user enters an invalid character" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody(
          "late-appeal-text" -> "コし")))
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) should include("The text must contain only letters, numbers and standard special characters")
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "getHeadingAndTitle" should {
    "show the single penalty text" when {
      "the user has selected no to appealing both penalties" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: String = controller.getHeadingAndTitle()(fakeRequestConverter(
          correctUserAnswers ++ Json.obj(SessionKeys.doYouWantToAppealBothPenalties -> "no"), fakeRequest
        ), implicitly)
        result shouldBe "This penalty was issued more than 30 days ago"
      }

      "the user hasn't been given the option to appeal both penalties" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: String = controller.getHeadingAndTitle()(userRequestWithCorrectKeys, implicitly)
        result shouldBe "This penalty was issued more than 30 days ago"
      }
      "the user has selected maybe to appealing both penalties" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = intercept[MatchError](controller.getHeadingAndTitle()(fakeRequestConverter(
          correctUserAnswers ++ Json.obj(SessionKeys.doYouWantToAppealBothPenalties -> "maybe"), fakeRequest
        ), implicitly))
        result.getMessage.contains("[MakingALateAppealController][getHeadingAndTitle] - unknown answer Some(maybe)") shouldBe true
      }

    }

    "show the first penalty text" when {
      "the user is appealing both penalties but the first penalty is late" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 5, 3))
        val result: String = controller.getHeadingAndTitle()(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.doYouWantToAppealBothPenalties -> "yes",
          SessionKeys.secondPenaltyCommunicationDate -> LocalDate.parse("2022-05-01"),
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.parse("2022-04-01")
        )), implicitly)
        result shouldBe "The first penalty was issued more than 30 days ago"
      }
    }

    "show the multiple penalties text" when {
      "the user is appealing both penalties and both are late" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 7, 3))
        val result: String = controller.getHeadingAndTitle()(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.doYouWantToAppealBothPenalties -> "yes",
          SessionKeys.secondPenaltyCommunicationDate -> LocalDate.parse("2022-05-01"),
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.parse("2022-04-01")
        )), implicitly)
        result shouldBe "The penalties were issued more than 30 days ago"
      }
    }
  }
}
