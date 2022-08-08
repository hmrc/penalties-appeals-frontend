/*
 * Copyright 2022 HM Revenue & Customs
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

package viewtils

import base.SpecBase
import messages.YouCannotAppealMessages._
import org.jsoup.Jsoup

class YouCannotAppealHelperSpec extends SpecBase {
  val youCannotAppealHelper: YouCannotAppealHelper = injector.instanceOf[YouCannotAppealHelper]

  "getHeaderAndTitle" when {
    "the user is an agent" when {
      "the appeal type is LPP" should {
        "return the correct value" in {
          val result = youCannotAppealHelper.getHeaderAndTitle(agentUserLPP)
          result shouldBe "agent.youCannotAppeal.headingAndTitle.lpp"
        }
      }
      "the appeal type is LSP" should {
        "return the correct value" in {
          val result = youCannotAppealHelper.getHeaderAndTitle(agentUserLSP)
          result shouldBe "agent.youCannotAppeal.headingAndTitle.lsp"
        }
      }
    }

    "the user is a vat trader" when {
      "the appeal type is LPP" should {
        "return the correct value" in {
          val result = youCannotAppealHelper.getHeaderAndTitle(vatTraderUserLPP)
          result shouldBe "youCannotAppeal.headingAndTitle.lpp"
        }
      }
      "the appeal type is LSP" should {
        "return the correct value" in {
          val result = youCannotAppealHelper.getHeaderAndTitle(vatTraderUserLSP)
          result shouldBe "youCannotAppeal.headingAndTitle.lsp"
        }
      }
    }
  }

  "getContent" when {
    "the user is an agent" when {
      "the appeal type is LPP" should {
        "return the correct content" in {
          val result = youCannotAppealHelper.getContent(implicitly, agentUserLPP)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe p1
          parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe agentLPPp2
          parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe agentLPPp3
          parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe agentP4
          parsedHtmlResult.select("p > .govuk-link").text() shouldBe returnToClientVATDetails
          parsedHtmlResult.select("p > .govuk-link").attr("href") shouldBe "http://localhost:9152/vat-through-software/vat-overview"
        }
      }
      "the appeal type is LSP" when {
        "returns the correct content" in {
          val result = youCannotAppealHelper.getContent(implicitly, agentUserLSP)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe p1
          parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe agentLSPp2
          parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe agentLSPp3
          parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe agentP4
          parsedHtmlResult.select("p > .govuk-link").text() shouldBe returnToClientVATDetails
          parsedHtmlResult.select("p > .govuk-link").attr("href") shouldBe "http://localhost:9152/vat-through-software/vat-overview"
        }
      }
    }
    "the user is a trader" when {
      "the appeal type is LPP" when {
        "returns the correct content" in {
          val result = youCannotAppealHelper.getContent(implicitly, vatTraderUserLPP)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe p1
          parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe traderLPPp2
          parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe traderLPPp3
          parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe traderP4
          parsedHtmlResult.select("p > .govuk-link").text() shouldBe checkWhatYouOwe
          parsedHtmlResult.select("p > .govuk-link").attr("href") shouldBe "http://localhost:9152/vat-through-software/what-you-owe"
        }
      }
      "the appeal type is LSP" when {
        "returns the correct content" in {
          val result = youCannotAppealHelper.getContent(implicitly, vatTraderUserLSP)
          val parsedHtmlResult = Jsoup.parse(result.body)
          parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe p1
          parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe traderLSPp2
          parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe traderLSPp3
          parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe traderP4
          parsedHtmlResult.select("p > .govuk-link").text() shouldBe returnToVATAccount
          parsedHtmlResult.select("p > .govuk-link").attr("href") shouldBe "http://localhost:9152/vat-through-software/vat-overview"
        }
      }
    }
  }
}
