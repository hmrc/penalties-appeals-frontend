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
import views.html.errors.TechnicalErrorPage
import org.mockito.ArgumentMatchers.any
import play.api.http.Status.FORBIDDEN
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class TechnicalErrorControllerSpec extends SpecBase {

  val technicalErrorPage: TechnicalErrorPage = injector.instanceOf[TechnicalErrorPage]

  val controller = new TechnicalErrorController(
    technicalErrorPage
  )(mcc, appConfig)

  "onPageLoad" should {
    "show the page with a 403 status code (FORBIDDEN)" in {
      status(controller.onPageLoad()(userRequestWithCorrectKeys)) shouldBe FORBIDDEN
    }
  }
}
