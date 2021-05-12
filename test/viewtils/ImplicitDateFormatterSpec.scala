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

package viewtils

import org.scalatest.{Matchers, WordSpec}

import java.time.{LocalDate, LocalDateTime}

class ImplicitDateFormatterSpec extends WordSpec with Matchers {
  "dateToString" should {
    "convert a LocalDate to a string - with the correct format" in {
      val sampleLocalDate: LocalDate = LocalDate.of(2020, 1, 1)
      ImplicitDateFormatter.dateToString(sampleLocalDate) shouldBe "1 January 2020"
    }

    "convert a LocalDateTime to a string - with the correct format" in {
      val sampleLocalDateTime: LocalDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0)
      ImplicitDateFormatter.dateTimeToString(sampleLocalDateTime) shouldBe "1 January 2020"
    }

    "convert a LocalDateTime to a string - (month year)" in {
      val sampleLocalDateTime: LocalDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0)
      ImplicitDateFormatter.dateTimeToMonthYearString(sampleLocalDateTime) shouldBe "January 2020"
    }
  }
}
