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
import play.api.http.Status
import play.api.test.Helpers._
import testUtils.AuthTestModels
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.HelloWorldPage

import scala.concurrent.Future

class HelloWorldControllerSpec extends SpecBase {

  val helloWorldPage: HelloWorldPage = injector.instanceOf[HelloWorldPage]

  class Setup(authResult: Future[~[Option[AffinityGroup], Enrolments]]) {

    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)

    object Controller extends HelloWorldController(
      appConfig,
      helloWorldPage
    )(stubMessagesControllerComponents(), authPredicate)
  }



  "GET /" should {
    "return 200" in new Setup(AuthTestModels.successfulAuthResult) {
      val result = Controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in new Setup(AuthTestModels.successfulAuthResult) {
      val result = Controller.helloWorld(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }
}
