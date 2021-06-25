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
import views.html.reasonableExcuseJourneys.health.{WhenDidHospitalStayBeginPage}

import scala.concurrent.Future

class HospitalStayBeginControllerSpec extends SpecBase {

  val whenDidHospitalStayBeginPage = injector.instanceOf[WhenDidHospitalStayBeginPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: HospitalStayBeginController = new HospitalStayBeginController(
      mainNavigator,
      whenDidHospitalStayBeginPage
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "HospitalStayBeginController" should {
    "onPageLoadForWhenDidHospitalStayBegin" when {

      "the user is authorised" must {

        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }

        "return OK and correct view (pre-populated date when present in session)" in new Setup(AuthTestModels.successfulAuthResult) {
          val result = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(SessionKeys.whenHospitalStayBegin -> "2021-01-01")))
          status(result) shouldBe OK
          val documentParsed = Jsoup.parse(contentAsString(result))
          documentParsed.select(".govuk-date-input__input").get(0).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(1).attr("value") shouldBe "1"
          documentParsed.select(".govuk-date-input__input").get(2).attr("value") shouldBe "2021"
        }

        "user does not have the correct session keys" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the user is unauthorised" when {
        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "onSubmitForWhenDidHospitalStayBegin" should {

      "the user is authorised" must {
        "return 500" when {
          "the user does not have the required keys in the session" in new Setup(AuthTestModels.successfulAuthResult) {
            val result = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest.withJsonBody(
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

        }
      }

      "the user is unauthorised" when {

        "return 403 (FORBIDDEN) when user has no enrolments" in new Setup(AuthTestModels.failedAuthResultNoEnrolments) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe FORBIDDEN
        }

        "return 303 (SEE_OTHER) when user can not be authorised" in new Setup(AuthTestModels.failedAuthResultUnauthorised) {
          val result: Future[Result] = controller.onSubmitForWhenDidHospitalStayBegin(NormalMode)(fakeRequest)
          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }

}
