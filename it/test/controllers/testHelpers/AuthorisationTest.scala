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

package controllers.testHelpers

import models.PenaltyTypeEnum
import models.session.UserAnswers
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStub
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{IntegrationSpecCommonBase, SessionKeys}
import java.time.LocalDate

trait AuthorisationTest {
  _: IntegrationSpecCommonBase =>
  def runControllerPredicateTests(result: => Action[AnyContent], method: String, url: String): Unit = {
    "redirect to Internal Server Error Page when the user is authorised but the session does not contain the correct keys" in new UserAnswersSetup(UserAnswers("1234", Json.obj())) {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest(method, url).withSession(
        authToken -> "1234",
        SessionKeys.journeyId -> "1234"
      )
      val request = await(result.apply(fakeRequestWithNoKeys))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "redirect to Internal Server Error Page when the user is authorised but the session does not contain ALL correct keys" in new UserAnswersSetup(UserAnswers("1234", Json.obj(
      SessionKeys.penaltyNumber -> "1234",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    ))) {
      val request = await(result.apply(fakeRequest))
      request.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = method match {
        case "GET" => await(buildClientForRequestToApp(uri = url).get())
        case "POST" => await(buildClientForRequestToApp(uri = url).post(""))
        case _ => await(buildClientForRequestToApp(uri = url).get())
      }
      request.status shouldBe Status.SEE_OTHER
    }
  }
}
