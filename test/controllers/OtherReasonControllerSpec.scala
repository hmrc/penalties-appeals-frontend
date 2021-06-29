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
import models.{CheckMode, NormalMode}
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
import views.html.reasonableExcuseJourneys.other._

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OtherReasonControllerSpec extends SpecBase {
  val whenDidYouBecomeUnablePage: WhenDidBecomeUnablePage = injector.instanceOf[WhenDidBecomeUnablePage]
  val whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage = injector.instanceOf[WhyReturnSubmittedLatePage]
  val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: OtherReasonController = new OtherReasonController(
      whenDidYouBecomeUnablePage,
      whyReturnSubmittedLatePage,
      uploadEvidencePage,
      mainNavigator
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "OtherReasonController" should {
    "onPageLoadForWhenDidBecomeUnable" when {

      "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(userRequestWithCorrectKeys)
        status(result) shouldBe OK
      }

      "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.whenDidBecomeUnable -> "2021-01-01")))
        status(result) shouldBe OK
        val documentParsed = Jsoup.parse(contentAsString(result))
        documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
        documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
      }

      "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
        val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenDidBecomeUnable" should {
      "the user is authorised" must {
        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to reason page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          await(result).session.get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
          "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
            Json.parse(
              """
                |{
                | "date.day": 1,
                | "date.month": 2,
                | "date.year": 2021
                |}
                |""".stripMargin))))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe "" //TODO change to CYA page route
          await(result).session.get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.of(2021, 2, 1).toString
        }

        "return 400 (BAD_REQUEST)" when {

          "passed string values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": "what",
                  | "date.month": "is",
                  | "date.year": "this"
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed an invalid values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 31,
                  | "date.month": 2,
                  | "date.year": 2021
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }

          "passed illogical dates as values for keys" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "date.day": 124356,
                  | "date.month": 432567,
                  | "date.year": 3124567
                  |}
                  |""".stripMargin))))
            status(result) shouldBe BAD_REQUEST
          }
        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "OtherReasonController" should {
      "onPageLoadForUploadEvidence" when {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(SessionKeys.evidenceFileName -> "test.png")))
          status(result) shouldBe OK
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onSubmitForUploadEvidence(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }

      "onSubmitForUploadEvidence" should {
        "the user is authorised" must {
          "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
            "- routing to late appeal or CYA page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "upload-evidence": "test.png"
                  |}
                  |""".stripMargin))))
            status(result) shouldBe SEE_OTHER
            await(result).session.get(SessionKeys.evidenceFileName).get shouldBe "test.png"
          }

          "return 303 (SEE_OTHER) adding the key to the session when the body is correct " +
            "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys.withJsonBody(
              Json.parse(
                """
                  |{
                  | "upload-evidence": "test.png"
                  |}
                  |""".stripMargin))))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe "" //TODO change to CYA page route url
            await(result).session.get(SessionKeys.evidenceFileName).get shouldBe "test.png"
          }

          "return 303 (SEE_OTHER) adding the key to the session when the body is empty " +
            "- routing to late appeal or CYA been reported page when in Normal Mode" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys))
            status(result) shouldBe SEE_OTHER
            await(result).session.get(SessionKeys.evidenceFileName).get shouldBe ""
          }

          "return 303 (SEE_OTHER) adding the key to the session when the body is empty " +
            "- routing to CYA page when in Check Mode" in new Setup(AuthTestModels.successfulAuthResult) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys))
            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get shouldBe "" //TODO change to CYA page route url
            await(result).session.get(SessionKeys.evidenceFileName).get shouldBe ""
          }
        }

        "the user is unauthorised" when {

          "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(NormalMode)(fakeRequest)
            status(result) shouldBe FORBIDDEN
          }

          "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
            val result: Future[Result] = controller.onSubmitForUploadEvidence(NormalMode)(fakeRequest)
            status(result) shouldBe SEE_OTHER
          }
        }
      }
    }
  }
}