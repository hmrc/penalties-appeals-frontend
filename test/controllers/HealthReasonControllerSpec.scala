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

import java.time.{LocalDate, LocalDateTime}

import base.SpecBase
import models.NormalMode
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.health.{WasHospitalStayRequiredPage, WhenDidHealthReasonHappenPage}

import scala.concurrent.Future

class HealthReasonControllerSpec extends SpecBase {

  val hospitalStayPage = injector.instanceOf[WasHospitalStayRequiredPage]
  val whenHealthIssueHappenedPage = injector.instanceOf[WhenDidHealthReasonHappenPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: HealthReasonController = new HealthReasonController(
      mainNavigator,
      hospitalStayPage,
      whenHealthIssueHappenedPage
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "HealthReasonController" should {
    "onPageLoadForWasHospitalStayRequired" when {

      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated option when present in session) - when answer is no" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(SessionKeys.wasHospitalStayRequired -> "no")))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select("#value-2").get(0).hasAttr("checked") shouldBe true
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }

    }

    "onSubmitForWasHospitalStayRequired" should {

      "user submits the form" when {
        "the validation is performed against possible values " +
          "- redirects to when health issue happened page and set the session key value" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "no"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe SEE_OTHER
            //TODO - change to when did health issue happen page when navigation added in PRM-329
            redirectLocation(result).get shouldBe "#"
            await(result).session.get(SessionKeys.wasHospitalStayRequired).get shouldBe "no"
          }

        // change this to redirect to when hospital stay began when added in as part of another story

//        "the validation is performed against possible values " +
//          "- redirect when hospital stay began page and set the session key value" in new Setup(AuthTestModels.successfulAuthResult) {
//            val result = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
//              .withJsonBody(
//                Json.parse(
//                  """
//                    |{
//                    |   "value": "yes"
//                    |}
//                    |""".stripMargin
//                )
//              )))
//            status(result) shouldBe SEE_OTHER
//            //TODO - change to when hospital stay began page
//            redirectLocation(result).get shouldBe "#"
//            await(result).session.get(SessionKeys.wasHospitalStayRequired).get shouldBe "yes"
//          }

        "the validation is performed against possible values - value does not appear in options list" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": "this_is_fake"
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe BAD_REQUEST
            contentAsString(result) should include("There is a problem")
            contentAsString(result) should include("Tell us if you or someone else was admitted to hospital unexpectedly")
          }

        "the validation is performed against an empty value - value is an empty string" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  |   "value": ""
                  |}
                  |""".stripMargin
              )
            )))
            status(result) shouldBe BAD_REQUEST
            contentAsString(result) should include("There is a problem")
            contentAsString(result) should include("Tell us if you or someone else was admitted to hospital unexpectedly")
          }
      }

      "return 500 when the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest.withJsonBody(
            Json.parse(
              """
                |{
                |   "value": "no"
                |}
                |""".stripMargin
            )
          ))
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWasHospitalStayRequired(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onPageLoadForWhenHealthReasonHappened" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(SessionKeys.whenHealthIssueHappened -> "2021-01-01")))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenHealthReasonHappened" should {

      "the user is authorised" must {

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- redirects to CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          //TODO - change to CYA page when navigation added in PRM-329
          redirectLocation(result).get shouldBe "#"
          await(result).session.get(SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
        "redirects to late appeal page when appeal > 30 days late" in new Setup(AuthTestModels.successfulAuthResult) {
          when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
          val result = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin
            )
          )))
          status(result) shouldBe SEE_OTHER
          //TODO - change to late appeal page when navigation added in PRM-329
          redirectLocation(result).get shouldBe "#"
          await(result).session.get(SessionKeys.whenHealthIssueHappened).get shouldBe LocalDate.of(2021, 2, 1).toString
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenHealthReasonHappened(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }

}
