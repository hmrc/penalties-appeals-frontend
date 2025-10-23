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

package config

import base.SpecBase
import views.html.errors.ErrorTemplate
import scala.concurrent.Await
import scala.concurrent.duration._

class ErrorHandlerSpec extends SpecBase {
  val errorTemplate: ErrorTemplate = injector.instanceOf[ErrorTemplate]

  "standardErrorTemplate" should {
    "return HTML for the standard error template" in {
      lazy val expectedResult = errorTemplate.apply("Error!", "Something went wrong!", "We are unable to process this request.")
      lazy val actualResult = errorHandler.standardErrorTemplate("Error!", "Something went wrong!", "We are unable to process this request.")
      Await.result(actualResult, 5.seconds) shouldBe expectedResult
    }
  }
}