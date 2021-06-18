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
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.{mock, reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import services.AppealService
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.{AppealConfirmationPage, CheckYourAnswersPage}

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends SpecBase {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val page: CheckYourAnswersPage = injector.instanceOf[CheckYourAnswersPage]
  val confirmationPage: AppealConfirmationPage = injector.instanceOf[AppealConfirmationPage]
  val mockAppealService: AppealService = mock(classOf[AppealService])

  val fakeRequestForCrimeJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01")
  )

  val fakeRequestForLossOfStaffJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "lossOfStaff",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01")
  )

  val fakeRequestForTechnicalIssuesJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "technicalIssues",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
    SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02")
  )

  val fakeRequestForHealthNoHospitalStayJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "health",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.wasHospitalStayRequired -> "no",
    SessionKeys.whenHealthIssueHappened -> "2022-01-02")
  )

  val fakeRequestForHealthOngoingHospitalStayJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "health",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.wasHospitalStayRequired -> "yes",
    SessionKeys.isHealthEventOngoing -> "yes",
    SessionKeys.whenHealthIssueStarted -> "2022-01-02")
  )

  val fakeRequestForHealthEndedHospitalStayJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "health",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.wasHospitalStayRequired -> "yes",
    SessionKeys.isHealthEventOngoing -> "no",
    SessionKeys.whenHealthIssueStarted -> "2022-01-02",
    SessionKeys.whenHealthIssueEnded -> "2022-01-03")
  )

  val fakeRequestForCrimeJourneyNoReasonableExcuse = fakeRequestConverter(fakeRequestWithCorrectKeys
    .withSession(
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01"
    )
  )

  val fakeRequestForCrimeJourneyWithoutSomeAnswers = fakeRequestConverter(fakeRequestWithCorrectKeys
    .withSession(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01"
    )
  )

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    reset(mockAppealService)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
  }

  object Controller extends CheckYourAnswersController(
    page,
    mockAppealService,
    confirmationPage,
    errorHandler
  )(stubMessagesControllerComponents(), implicitly, implicitly, authPredicate, dataRequiredAction)

  "onPageLoad" should {
    "the user is authorised" must {

      "return OK and correct view - when all answers exist for crime" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForCrimeJourney)
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for loss of staff" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForLossOfStaffJourney)
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for technical issues" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForTechnicalIssuesJourney)
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - no hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForHealthNoHospitalStayJourney)
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - ongoing hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForHealthOngoingHospitalStayJourney)
        status(result) shouldBe OK
      }

      "return OK and correct view - when all answers exist for health - ended hospital stay" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForHealthEndedHospitalStayJourney)
        status(result) shouldBe OK
      }

      "return ISE" when {
        "the user has not selected a reasonable excuse" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequestForCrimeJourneyNoReasonableExcuse)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "the user has not completed all required answers" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequestForCrimeJourneyWithoutSomeAnswers)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "the user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
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

  "onSubmit" should {
    "the user is authorised" must {
      "redirect the user to the confirmation page on success" in new Setup(AuthTestModels.successfulAuthResult) {
        when(mockAppealService.submitAppeal(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(true))
        val result: Future[Result] = Controller.onSubmit()(fakeRequestForCrimeJourney)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe controllers.routes.CheckYourAnswersController.onPageLoadForConfirmation().url
      }

      "redirect the user to an ISE" when {

        "the appeal submission fails" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockAppealService.submitAppeal(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(false))
          val result: Future[Result] = Controller.onSubmit()(fakeRequestForCrimeJourney)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "there is no reasonable excuse selected" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onSubmit()(fakeRequestForCrimeJourneyNoReasonableExcuse)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "not all session keys are present" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onSubmit()(fakeRequestForCrimeJourneyWithoutSomeAnswers)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "onPageLoadForConfirmation" should {
    "the user is authorised" when {
      "show the confirmation page and remove all custom session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoadForConfirmation()(fakeRequestForCrimeJourney)
        await(result).header.status shouldBe OK
        SessionKeys.allKeys.toSet.subsetOf(await(result).session.data.values.toSet) shouldBe false
      }
    }

    "the user is unauthorised" when {

      "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe FORBIDDEN
      }

      "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

}
