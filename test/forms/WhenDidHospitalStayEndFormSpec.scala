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

package forms

import base.SpecBase
import play.api.data.{Form, FormError}

import java.time.LocalDate

class WhenDidHospitalStayEndFormSpec extends FormBehaviours with SpecBase {
  val sampleStartDate: LocalDate = LocalDate.parse("2021-01-01")
  val form: Form[LocalDate] = WhenDidHospitalStayEndForm.whenDidHospitalStayEndForm(sampleStartDate)
  "WhenDidHospitalStayEndForm" should {
    behave like dateForm(form, "date", errorType => s"healthReason.whenDidHospitalStayEnd.error.$errorType")
    "not bind when the date entered is earlier than the date provided previously" in {
      val result = form.bind(
        Map(
          "date.day" -> "31",
          "date.month" -> "12",
          "date.year" -> "2020"
        )
      )
      result.errors.size shouldBe 1
      result.errors.head shouldBe FormError("date.day", "healthReason.whenDidHospitalStayEnd.error.endDateLessThanStartDate", Seq("day", "month", "year"))
    }
  }
}
