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

import uk.gov.hmrc.http.HeaderCarrier
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory

trait MockHeaderGenerator extends MockFactory{
  val mockHeaderGenerator: HeaderGenerator = mock[HeaderGenerator]

  object MockHeaderGenerator {
    def headersForPEGA(): CallHandler[Seq[(String, String)]] =
      (mockHeaderGenerator.headersForPEGA()(_: HeaderCarrier)) expects *
  }
}