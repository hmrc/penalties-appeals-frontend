/*
 * Copyright 2023 HM Revenue & Customs
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

  def call(isAuthorised: Boolean): Future[Result] = {
    mockOrganisationAuthorised()
    controller.signOut(isAuthorised)(fakeRequest)
  }

  "GET /sign-out" should {
    "redirect to the sign out with feedback url" when {
      "the user is authorised" in {
        val result = call(isAuthorised = true)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe appConfig.signOutUrl
      }
    }

    "redirect to the sign out without feedback url" when {
      "the user is unauthorised" in {
        val result = call(isAuthorised = false)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe appConfig.signOutUrlUnauthorised
      }
    }
  }
}