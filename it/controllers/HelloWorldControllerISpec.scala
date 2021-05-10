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

import org.scalatest.concurrent.ScalaFutures.whenReady
import play.api.http.Status
import stubs.AuthStub
import utils.IntegrationSpecCommonBase

class HelloWorldControllerISpec extends IntegrationSpecCommonBase {
  "GET /hello-world" should {
    "return 200 (OK) when the user is authorised" in {
      val request = await(buildClientForRequestToApp(uri = "/hello-world").get())
      request.status shouldBe Status.OK
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/hello-world").get())
      request.status shouldBe Status.SEE_OTHER
    }
  }
}