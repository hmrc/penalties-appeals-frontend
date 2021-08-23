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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.SessionKeys
import views.html.HonestyDeclarationPage

import scala.concurrent.Future

class HonestyDeclarationControllerSpec extends SpecBase {

 val honestyDeclarationPage: HonestyDeclarationPage = injector.instanceOf[HonestyDeclarationPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)

    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)

    val controller: HonestyDeclarationController = new HonestyDeclarationController(
      honestyDeclarationPage,
      errorHandler,
      mainNavigator
    )(mcc, appConfig, authPredicate, dataRequiredAction)
  }

  "onPageLoad" should {
    "return 200" when {
      "the user is authorised and has the correct keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        status(result) shouldBe OK
      }
    }

    "return 500" when {
      "the user hasn't selected an option on the reasonable excuse page" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onPageLoad()(fakeRequest)
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
    "return 303" when {
      "the reasonable excuse selected is 'crime' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "crime"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "value": "true"
                |}
                |""".stripMargin)
          )))

        status(result) shouldBe SEE_OTHER
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).isDefined shouldBe true
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
      }

      "the reasonable excuse selected is 'fireOrFlood' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "fireOrFlood"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "value": "true"
                |}
                |""".stripMargin)
          )))

        status(result) shouldBe SEE_OTHER
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).isDefined shouldBe true
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
        await(result).session.get(SessionKeys.reasonableExcuse).get shouldBe "fireOrFlood"
      }

      "the reasonable excuse selected is 'lossOfStaff' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "lossOfStaff"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "value": "true"
                |}
                |""".stripMargin)
          )))

        status(result) shouldBe SEE_OTHER
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).isDefined shouldBe true
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
        await(result).session.get(SessionKeys.reasonableExcuse).get shouldBe "lossOfStaff"
      }

      "the reasonable excuse selected is 'technicalIssues' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "technicalIssues"))
          .withJsonBody(
            Json.parse(
              """
                |{
                | "value": "true"
                |}
                |""".stripMargin)
          )))

        status(result) shouldBe SEE_OTHER
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).isDefined shouldBe true
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
        await(result).session.get(SessionKeys.reasonableExcuse).get shouldBe "technicalIssues"
      }

      "the reasonable excuse selected is 'health' and the JSON body is valid" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
        .withSession((SessionKeys.reasonableExcuse, "health"))
        .withJsonBody(
          Json.parse(
          """
            |{
            | "value": "true"
            |}
            |""".stripMargin)
        )))
        status(result) shouldBe SEE_OTHER
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).isDefined shouldBe true
        await(result).session.get(SessionKeys.hasConfirmedDeclaration).get shouldBe "true"
        await(result).session.get(SessionKeys.reasonableExcuse).get shouldBe "health"
      }
    }

    "return 400" when {
      "the user has posted an empty body" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "crime"))))
        status(result) shouldBe BAD_REQUEST
      }

      "the user has changed the hidden value" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession((SessionKeys.reasonableExcuse, "crime"))
          .withJsonBody(
          Json.parse(
            """
              |{
              | "value": "royale_with_cheese"
              |}
              |""".stripMargin)
        )))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return 500" when {
      "the user hasn't selected an option on the reasonable excuse page" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(userRequestWithCorrectKeys)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
        val result = controller.onSubmit()(fakeRequest)
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
