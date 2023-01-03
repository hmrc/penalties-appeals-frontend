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
import models.ReasonableExcuse
import models.session.UserAnswers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test.Helpers._
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.ReasonableExcuseSelectionPage

import scala.concurrent.{ExecutionContext, Future}

class ReasonableExcuseControllerSpec extends SpecBase {
  val mockAppealService: AppealService = mock(classOf[AppealService])
  val reasonableExcusePage: ReasonableExcuseSelectionPage = injector.instanceOf[ReasonableExcuseSelectionPage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAppealService, mockSessionService, mockAuthConnector)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: ReasonableExcuseController = new ReasonableExcuseController(
      reasonableExcusePage,
      mockAppealService,
      errorHandler,
      mockSessionService
    )(mcc, appConfig, authPredicate, dataRetrievalAction, dataRequiredAction, ec)
  }

  "onPageLoad" should {
    val seqOfReasonableExcuses: Seq[ReasonableExcuse] = Seq(
      ReasonableExcuse(
        `type` = "bereavement",
        descriptionKey = "reasonableExcuses.bereavementReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "crime",
        descriptionKey = "reasonableExcuses.crimeReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "other",
        descriptionKey = "reasonableExcuses.otherReason",
        isOtherOption = true
      )
    )

    "call the service and retrieve the list of reasonable excuses" when {
      "the call succeeds - return OK with the list of radio options (no previous answer - " +
        "has penalty info in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
          .thenReturn(Future.successful(
            Some(seqOfReasonableExcuses)
          ))

        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "the call succeeds - return OK with the list of radio options (pre-selected option when present in session)" in
        new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "bereavement")))))
          when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
            .thenReturn(Future.successful(
              Some(seqOfReasonableExcuses)
            ))

          val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
          val documentParsed: Document = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value").hasAttr("checked") shouldBe true
        }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the call fails to retrieve the reasonable excuse list" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
          .thenReturn(Future.successful(
            None
          ))

        val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
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
    val seqOfReasonableExcuses: Seq[ReasonableExcuse] = Seq(
      ReasonableExcuse(
        `type` = "bereavement",
        descriptionKey = "reasonableExcuses.bereavementReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "crime",
        descriptionKey = "reasonableExcuses.crimeReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "other",
        descriptionKey = "reasonableExcuses.otherReason",
        isOtherOption = true
      )
    )

    "user submits the form" when {
      "the validation is performed against possible values - redirect on success and set the session key value" in
        new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
            .thenReturn(Future.successful(
              Some(seqOfReasonableExcuses)
            ))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val answerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          when(mockSessionService.updateAnswers(answerCaptor.capture()))
            .thenReturn(Future.successful(true))
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest
            .withFormUrlEncodedBody("value" -> "bereavement")))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.HonestyDeclarationController.onPageLoad().url
          answerCaptor.getValue.data shouldBe correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "bereavement")
        }

      "the validation is performed against possible values - value does not appear in reasonable excuse list" in
        new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
            .thenReturn(Future.successful(
              Some(seqOfReasonableExcuses)
            ))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest
            .withFormUrlEncodedBody("value" -> "this_is_fake")))
          status(result) shouldBe BAD_REQUEST
        }

      "the validation is performed against an empty value - value is an empty string" in
        new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
            .thenReturn(Future.successful(
              Some(seqOfReasonableExcuses)
            ))
          when(mockSessionService.getUserAnswers(any()))
            .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
          val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest
            .withFormUrlEncodedBody("value" -> "")))
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) should include("There is a problem")
          contentAsString(result) should include("Select the reason for missing the VAT deadline")
        }
    }

    "return 500" when {
      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(Json.obj()))))
        val result: Future[Result] = controller.onSubmit()(fakeRequest.withFormUrlEncodedBody("value" -> "bereavement"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the call fails to retrieve the reasonable excuse list" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockAppealService.getReasonableExcuseListAndParse()(any(), any()))
          .thenReturn(Future.successful(
            None
          ))
        when(mockSessionService.getUserAnswers(any()))
          .thenReturn(Future.successful(Some(userAnswers(correctUserAnswers))))
        val result: Future[Result] = controller.onSubmit()(fakeRequestConverter(correctUserAnswers, fakeRequest.withFormUrlEncodedBody("value" -> "bereavement")))
        status(result) shouldBe INTERNAL_SERVER_ERROR
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
