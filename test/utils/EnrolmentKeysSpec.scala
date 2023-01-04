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

class EnrolmentKeysSpec extends AnyWordSpec with Matchers {
  "constructMTDVATEnrolmentKey" should {
    "construct a valid enrolment key with the provided VRN" in {
      val vrnToPassIn: String = "123456789"
      val result = EnrolmentKeys.constructMTDVATEnrolmentKey(vrnToPassIn)
      result shouldBe "HMRC-MTD-VAT~VRN~123456789"
    }
  }
}
