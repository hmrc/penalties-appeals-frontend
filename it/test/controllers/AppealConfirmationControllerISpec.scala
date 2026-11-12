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

import org.mongodb.scala.Document
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class AppealConfirmationControllerISpec extends IntegrationSpecCommonBase {
  val controller: AppealConfirmationController = injector.instanceOf[AppealConfirmationController]
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(Document()).toFuture())
    super.beforeEach()
  }

  "GET /appeal-confirmation" should {
    "show the appeal confirmation page when the user has answers in Mongo" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      val fakeRequest = FakeRequest("GET", "/").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1235",
        SessionKeys.previouslySubmittedJourneyId -> "1234",
        SessionKeys.penaltiesHasSeenConfirmationPage -> "true"
      )
      val result = controller.onPageLoad()(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "show the appeal confirmation page when the user has no answers in Mongo" in new UserAnswersSetup(userAnswers(Json.obj(
      SessionKeys.reasonableExcuse -> "crime",
      SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
      SessionKeys.hasConfirmedDeclaration -> true,
      SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
      SessionKeys.isUploadEvidence -> "yes"
    ))) {
      val fakeRequest = FakeRequest("GET", "/").withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1236",
        SessionKeys.previouslySubmittedJourneyId -> "1235",
        SessionKeys.penaltiesHasSeenConfirmationPage -> "true"
      )
      val result = controller.onPageLoad()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.IncompleteSessionDataController.onPageLoadWithNoJourneyData().url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val result = await(buildClientForRequestToApp(uri = "/appeal-confirmation").get())
      result.status shouldBe Status.SEE_OTHER
    }
  }

}
