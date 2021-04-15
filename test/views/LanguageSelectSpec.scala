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
import views.html.LanguageSelect

class LanguageSelectSpec extends SpecBase {
  val languageSelectPage = injector.instanceOf[LanguageSelect]

  "LanguageSelectPage" should {
    "return HTML and have English language toggle (no link) and Welsh language toggle (link)" in {
      lazy val result = languageSelectPage.apply()
      val bodyAsHtml = Jsoup.parse(result.body)
      val englishLangToggle = bodyAsHtml.select(".hmrc-language-select__list-item > span")
      val welshLangToggle = bodyAsHtml.select(".hmrc-language-select__list-item > a")
      englishLangToggle.text() shouldBe "English"
      welshLangToggle.attr("href") shouldBe "/penalties-frontend/language/cy"
      result.contentType shouldBe "text/html"
    }
  }
}
