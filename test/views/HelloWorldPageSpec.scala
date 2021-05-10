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

package views

import base.SpecBase
import org.jsoup.Jsoup
import views.html.HelloWorldPage

//TODO: Remove when we have other pages
class HelloWorldPageSpec extends SpecBase {
  val helloWorldPage = injector.instanceOf[HelloWorldPage]

  "HelloWorldPage" should {
    lazy val result = helloWorldPage.apply()
    val bodyAsHtml = Jsoup.parse(result.body)
    "have the service name as the header" in {
      bodyAsHtml.select("h1").text() shouldBe messages("service.name")
    }

    "have some placeholder text" in {
      bodyAsHtml.select("p.govuk-body").text() shouldBe messages("service.text")
    }

    "have the correct page title" in {
      bodyAsHtml.title.contains(messages("service.name")) shouldBe true
    }
  }
}