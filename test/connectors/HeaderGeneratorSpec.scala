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

package connectors

import config.MockAppConfigTrait
import org.scalatest.{Matchers, WordSpec}
import play.api.http.HeaderNames
import uk.gov.hmrc.http.HeaderCarrier


class HeaderGeneratorSpec extends WordSpec with Matchers with HeaderNames with MockAppConfigTrait {

  val testHeaderGenerator = new HeaderGenerator(mockAppConfig)
  val bearerToken           = "someToken"

  "HeaderGenerator.headersForPEGA" must {

      "include the Authorization header if it is supplied in AppConfig" in {
      MockAppConfig.pegaBearerToken returns bearerToken
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val headers                    = testHeaderGenerator.headersForPEGA().toMap

      headers(AUTHORIZATION) shouldBe s"Bearer $bearerToken"
    }

    "not include the Authorization header if it is empty string in AppConfig" in {
      MockAppConfig.pegaBearerToken returns ""
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val headers                    = testHeaderGenerator.headersForPEGA().toMap
      headers.contains(AUTHORIZATION) shouldBe false
    }
  }
}
