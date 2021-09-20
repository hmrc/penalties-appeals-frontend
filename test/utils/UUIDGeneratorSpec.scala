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

package utils

import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{MustMatchers, WordSpec}

class UUIDGeneratorSpec extends WordSpec with MustMatchers {

  val uuidGenerator = new UUIDGenerator

  "IdGenerator" when {
    "generating submissionIds" when {
      "generate ids that are different" in {
        val num = 1000000
        val ids = for (_ <- 1 to num) yield uuidGenerator.generateUUID

        ids.toSet.size shouldBe num
      }

      "generate ids that are uuids" in {
        uuidGenerator.generateUUID must fullyMatch regex """([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})""".r
      }
    }
  }
}
