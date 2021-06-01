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
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.CheckYourAnswersPage

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {
  val page: CheckYourAnswersPage = injector.instanceOf[CheckYourAnswersPage]

  val fakeRequestForCrimeJourney = fakeRequestWithCorrectKeys
    .withSession(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01"
    )

  val fakeRequestForCrimeJourneyNoReasonablExcuse = fakeRequestWithCorrectKeys
    .withSession(
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01"
    )

  val fakeRequestForCrimeJourneyWithoutSomeAnswers = fakeRequestWithCorrectKeys
    .withSession(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasConfirmedDeclaration -> "true",
      SessionKeys.dateOfCrime -> "2022-01-01"
    )

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
  }

  object Controller extends CheckYourAnswersController(
    page,
    errorHandler
  )(stubMessagesControllerComponents(), implicitly, authPredicate, dataRequiredAction)
  
  "onPageLoad" should {
    "the user is authorised" must {

      "return OK and correct view - when all answers exist for crime" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = Controller.onPageLoad()(fakeRequestForCrimeJourney)
        status(result) shouldBe OK
      }

      "return ISE" when {
        "the user has not selected a reasonable excuse" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoad()(fakeRequestForCrimeJourneyNoReasonablExcuse)
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
        val result: Future[Result] = Controller.onSubmit()(fakeRequestForCrimeJourney)
        status(result) shouldBe SEE_OTHER
        //TODO: change to confirmation success page
        redirectLocation(result).get shouldBe ""
      }

      "redirect the user to an ISE when the appeal submission fails" in new Setup(AuthTestModels.successfulAuthResult) {
        //TODO: Implement when the confirmation page and relevant appeal submission functionality is done
//        val result: Future[Result] = Controller.onSubmit()(fakeRequest)
//        status(result) shouldBe INTERNAL_SERVER_ERROR
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
