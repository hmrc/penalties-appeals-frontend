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

package connectors

import base.SpecBase
import org.mockito.Mockito.{mock, when}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.HeaderNames
import utils.UUIDGenerator


class HeaderGeneratorSpec extends AnyWordSpec with HeaderNames with SpecBase {

  val mockUUIDGenerator: UUIDGenerator = mock(classOf[UUIDGenerator])
  val testHeaderGenerator = new HeaderGenerator(mockUUIDGenerator)
  val correlationId = "someUUID"

  "HeaderGenerator.headersForPEGA" must {

    "create headers" in {
      when(mockUUIDGenerator.generateUUID).thenReturn(correlationId)
      val headers = testHeaderGenerator.headersForPEGA().toMap
      headers("CorrelationId") shouldBe  correlationId
    }
  }
}
