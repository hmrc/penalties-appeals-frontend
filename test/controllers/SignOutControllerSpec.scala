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
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import testUtils.AuthMocks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignOutControllerSpec extends SpecBase with AuthMocks {
  val controller = new SignOutController(
    mcc
  )

  lazy val result: Future[Result] = {
    mockOrganisationAuthorised()
    controller.signOut()(fakeRequest)
  }

  "GET /sign-out" should {
    "redirect to the sign out url" in {
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe appConfig.signOutUrl
    }
  }
}