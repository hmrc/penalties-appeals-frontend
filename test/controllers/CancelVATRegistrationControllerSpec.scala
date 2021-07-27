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
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import views.html.CancelVATRegistrationPage

import java.time.LocalDateTime
import scala.concurrent.Future

class CancelVATRegistrationControllerSpec extends SpecBase {
  val cancelVATRegistrationPage: CancelVATRegistrationPage = injector.instanceOf[CancelVATRegistrationPage]
  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    val controller: CancelVATRegistrationController = new CancelVATRegistrationController(
      cancelVATRegistrationPage
    )(authPredicate, dataRequiredAction, appConfig, mcc)

    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }
}
