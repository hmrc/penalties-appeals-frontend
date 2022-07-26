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

package models.appeals

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class MultiplePenaltiesDataSpec extends AnyWordSpec with Matchers {

  "MultiplePenaltiesData" should {
    "be readable from JSON" in {
      val multiplePenaltiesJson = Json.parse(
        """
          |{
          | "firstPenaltyChargeReference": "123456789",
          | "firstPenaltyAmount": 101.01,
          | "secondPenaltyChargeReference": "123456790",
          | "secondPenaltyAmount": 101.02
          |}
          |""".stripMargin
      )

      val model: MultiplePenaltiesData = MultiplePenaltiesData(
        firstPenaltyChargeReference = "123456789",
        firstPenaltyAmount = 101.01,
        secondPenaltyChargeReference = "123456790",
        secondPenaltyAmount = 101.02
      )
      val result = Json.fromJson(multiplePenaltiesJson)(MultiplePenaltiesData.format)
      result.isSuccess shouldBe true
      result.get shouldBe model
    }
  }

}
