/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import views.html.AppealCoverBothPenaltiesPage

import scala.concurrent.Future

class AppealCoverPenaltiesControllerSpec extends SpecBase {
  val appealCoverBothPenaltiesPage: AppealCoverBothPenaltiesPage = injector.instanceOf[AppealCoverBothPenaltiesPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      any(), any())
    ).thenReturn(authResult)
    val controller: AppealCoverPenaltiesController = new AppealCoverPenaltiesController(
      appealCoverBothPenaltiesPage
    )(authPredicate, dataRequiredAction, appConfig, mcc)
  }
  "AppealCoverPenaltiesController" should {
    "onPageLoadForAppealCoverBothPenalties" should {
      "the user is authorised" must {
        "return OK and correct view" in new Setup(AuthTestModels.successfulAuthResult) {
          val result: Future[Result] = controller.onPageLoadForAppealCoverBothPenalties()(userRequestWithCorrectKeys)
          status(result) shouldBe OK
        }
      }
    }
  }
}
