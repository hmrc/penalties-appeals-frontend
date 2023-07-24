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

package views.components

import base.SpecBase
import views.behaviours.ViewBehaviours
import views.html.components.Header

class HeaderSpec extends SpecBase with ViewBehaviours {
  val headerHtml: Header = injector.instanceOf[Header]

  "Header" should {
    "have the correct sign out link" when {
      "the user is authorised" in {
        val html = headerHtml.apply(isAuthorised = true)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-sign-out-nav__link").attr("href") shouldBe controllers.routes.SignOutController.signOut(true).url
      }

      "the user is not authorised" in {
        val html = headerHtml.apply(isAuthorised = false)
        val htmlDocument = asDocument(html)
        htmlDocument.select(".hmrc-sign-out-nav__link").attr("href") shouldBe controllers.routes.SignOutController.signOut(false).url
      }
    }
  }
}