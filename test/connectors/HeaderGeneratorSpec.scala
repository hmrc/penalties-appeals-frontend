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

import base.SpecBase
import config.AppConfig
import org.mockito.Mockito.{mock, when}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.HeaderNames
import uk.gov.hmrc.http.HeaderCarrier
import utils.UUIDGenerator


class HeaderGeneratorSpec extends AnyWordSpec with HeaderNames with SpecBase {

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  val mockUUIDGenerator: UUIDGenerator = mock(classOf[UUIDGenerator])
  val testHeaderGenerator = new HeaderGenerator(mockAppConfig,mockUUIDGenerator)
  val bearerToken = "someToken"
  val correlationId = "someUUID"
  val env = "someEnv"

  "HeaderGenerator.headersForPEGA" must {

    "create headers" in {
      when(mockAppConfig.pegaEnvironment) thenReturn env
      when(mockUUIDGenerator.generateUUID).thenReturn(correlationId)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val headers = testHeaderGenerator.headersForPEGA().toMap
      headers("CorrelationId") shouldBe  correlationId
      headers("Environment") shouldBe env
    }

    "include the Authorization header if it is supplied in AppConfig" in {
      when(mockAppConfig.pegaEnvironment) thenReturn env
      when(mockAppConfig.pegaBearerToken).thenReturn(bearerToken)
      when(mockUUIDGenerator.generateUUID).thenReturn(correlationId)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val headers = testHeaderGenerator.headersForPEGA().toMap
      headers("CorrelationId") shouldBe  correlationId
      headers("Environment") shouldBe env
      headers(AUTHORIZATION) shouldBe s"Bearer $bearerToken"
    }

    "not include the Authorization header if it is empty string in AppConfig" in {
      when(mockAppConfig.pegaEnvironment) thenReturn env
      when(mockAppConfig.pegaBearerToken).thenReturn("")
      when(mockUUIDGenerator.generateUUID).thenReturn(correlationId)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val headers = testHeaderGenerator.headersForPEGA().toMap
      headers("CorrelationId") shouldBe  correlationId
      headers("Environment") shouldBe env
      headers.contains(AUTHORIZATION) shouldBe false
    }
  }
}
