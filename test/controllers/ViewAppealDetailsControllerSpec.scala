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
import helpers.SessionAnswersHelper
import models.UserRequest
import models.appeals.QuestionAnswerRow
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.Configuration
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import services.SessionService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import utils.SessionKeys
import views.html.ViewAppealDetailsPage

import scala.concurrent.{ExecutionContext, Future}

class ViewAppealDetailsControllerSpec extends SpecBase {
  val viewAppealDetailsPage: ViewAppealDetailsPage = injector.instanceOf[ViewAppealDetailsPage]
  val sessionService: SessionService = injector.instanceOf[SessionService]
  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]
  val mockConfig: Configuration = mock(classOf[Configuration])
  val mockSessionAnswersHelper: SessionAnswersHelper = mock(classOf[SessionAnswersHelper])

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockSessionService)
    reset(mockConfig)
    reset(mockAppConfig)
    reset(mockSessionAnswersHelper)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    when(mockConfig.getOptional[String](ArgumentMatchers.eq("feature.switch.time-machine-now"))(any())).thenReturn(Some("2023-08-14"))
    when(mockSessionAnswersHelper.getSubmittedAnswers(any())(any(), any())).thenReturn(Seq(QuestionAnswerRow("Key", "Value", "")))
    when(mockSessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(any())(any(), any())).thenReturn(Seq(QuestionAnswerRow("Key2", "Value2", "")))
    val controller = new ViewAppealDetailsController(viewAppealDetailsPage, mockSessionAnswersHelper, mockSessionService, errorHandler)(mcc, mockConfig, mockAppConfig, authPredicate, ec)
    val userRequest: UserRequest[AnyContent] = fakeRequestConverter(fakeRequest = fakeRequest.withSession(SessionKeys.previouslySubmittedJourneyId -> "PreviousJourney1"))
  }

  "ViewAppealDetailsController" should {
    "onPageLoad" when {
      "the user is authorised" must {
        "return 200 (OK) and the correct view (non-upload journey)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(crimeAnswers))))
          val result: Future[Result] = controller.onPageLoad()(userRequest)
          status(result) shouldBe OK
        }

        "return 200 (OK) and the correct view (obligation journey)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(obligationAnswers))))
          when(mockSessionAnswersHelper.getPreviousUploadsFileNames(any())).thenReturn(Future.successful("file.txt, file2.txt"))
          val result: Future[Result] = controller.onPageLoad()(userRequest)
          status(result) shouldBe OK
          verify(mockSessionAnswersHelper, times(1)).getPreviousUploadsFileNames(any())
        }

        "return 200 (OK) and the correct view (other journey)" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(Some(userAnswers(otherAnswers))))
          when(mockSessionAnswersHelper.getPreviousUploadsFileNames(any())).thenReturn(Future.successful("file.txt, file2.txt"))
          val result: Future[Result] = controller.onPageLoad()(userRequest)
          status(result) shouldBe OK
          verify(mockSessionAnswersHelper, times(1)).getPreviousUploadsFileNames(any())
        }

        "return 303 (SEE_OTHER) when they do not have user answers" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockSessionService.getUserAnswers(any())).thenReturn(Future.successful(None))
          val result: Future[Result] = controller.onPageLoad()(userRequest)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
        }

        "return 303 (SEE_OTHER) when there is no 'previouslySubmittedJourneyId' in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoad()(userRequestWithCorrectKeys)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
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
  }
}
