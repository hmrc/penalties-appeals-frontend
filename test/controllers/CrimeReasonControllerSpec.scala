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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.crime.WhenDidCrimeHappenPage

import java.time.LocalDate
import scala.concurrent.Future

class CrimeReasonControllerSpec extends SpecBase {
  val page: WhenDidCrimeHappenPage = injector.instanceOf[WhenDidCrimeHappenPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
  }

  object Controller extends CrimeReasonController(
    page
  )(authPredicate, dataRequiredAction, implicitly, stubMessagesControllerComponents())

  "CrimeReasonController" should {
    "onPageLoadForWhenCrimeHappened" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoadForWhenCrimeHappened()(fakeRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-selected option when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = Controller.onPageLoadForWhenCrimeHappened()(fakeRequestWithCorrectKeys.withSession(SessionKeys.dateOfCrime -> "2021-01-01"))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onPageLoadForWhenCrimeHappened()(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = Controller.onPageLoadForWhenCrimeHappened()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = Controller.onPageLoadForWhenCrimeHappened()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenCrimeHappened" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin)))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.dateOfCrime).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": "what",
                  | "date.month": "is",
                  | "date.year": "this"
                  |}
                  |""".stripMargin)))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 31,
                  | "date.month": 2,
                  | "date.year": 2021
                  |}
                  |""".stripMargin)))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 124356,
                  | "date.month": 432567,
                  | "date.year": 3124567
                  |}
                  |""".stripMargin)))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = Controller.onSubmitForWhenCrimeHappened()(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }
}