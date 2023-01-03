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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CurrencyFormatterSpec extends AnyWordSpec with Matchers {
  object Formatter extends CurrencyFormatter

  "currencyFormatAsNonHTMLString" should {
    "convert a BigDecimal with more than 2dp to a string formatted to 2dp and prepend with £" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.70419)
      val actualResult = Formatter.currencyFormatAsNonHTMLString(bigDecimalToPassToFormatter)
      actualResult shouldBe "£419.70"
    }

    "convert a BigDecimal with 2dp to a string formatted to 2dp and prepend with £" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.71)
      val actualResult = Formatter.currencyFormatAsNonHTMLString(bigDecimalToPassToFormatter)
      actualResult shouldBe "£419.71"
    }

    "convert a BigDecimal with 2dp with the final dp being a 0 to a string with a leading 0 and prepend with £" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.20)
      val actualResult = Formatter.currencyFormatAsNonHTMLString(bigDecimalToPassToFormatter)
      actualResult shouldBe "£419.20"
    }

    "convert a BigDecimal with 1dp to a string formatted to 2dp and prepend with £" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.2)
      val actualResult = Formatter.currencyFormatAsNonHTMLString(bigDecimalToPassToFormatter)
      actualResult shouldBe "£419.20"
    }

    "convert a BigDecimal with leading .00 to a whole and prepend with £" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.00)
      val actualResult = Formatter.currencyFormatAsNonHTMLString(bigDecimalToPassToFormatter)
      actualResult shouldBe "£419"
    }
  }

  "parseBigDecimalToFriendlyValue" should {
    "parse a BigDecimal with leading .00 if it is whole" in {
      val bigDecimalToPassToFormatter = BigDecimal(419)
      val actualResult = Formatter.parseBigDecimalToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419.00"
    }

    "parse a BigDecimal to 2dp if it is currently 1dp" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.2)
      val actualResult = Formatter.parseBigDecimalToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419.20"
    }

    "do not change a BigDecimal with 2dp" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.12)
      val actualResult = Formatter.parseBigDecimalToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419.12"
    }
  }

  "parseBigDecimalNoPaddedZeroToFriendlyValue" should {
    "parse a BigDecimal and keep it whole" in {
      val bigDecimalToPassToFormatter = BigDecimal(419)
      val actualResult = Formatter.parseBigDecimalNoPaddedZeroToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419"
    }

    "parse a BigDecimal to 2dp if it is currently 1dp" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.2)
      val actualResult = Formatter.parseBigDecimalNoPaddedZeroToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419.20"
    }

    "do not change a BigDecimal with 2dp" in {
      val bigDecimalToPassToFormatter = BigDecimal(419.12)
      val actualResult = Formatter.parseBigDecimalNoPaddedZeroToFriendlyValue(bigDecimalToPassToFormatter)
      actualResult shouldBe "419.12"
    }
  }
}
