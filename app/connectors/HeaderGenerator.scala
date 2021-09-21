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

import config.AppConfig
import play.api.http.HeaderNames._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logger.logger
import utils.UUIDGenerator

import javax.inject.{Inject, Singleton}

@Singleton
class HeaderGenerator @Inject()(appConfig: AppConfig, idGenerator: UUIDGenerator) {

  def headersForPEGA()(implicit hc: HeaderCarrier): Seq[(String, String)] = {

    val correlationId = idGenerator.generateUUID
    val headers = Seq(
      "CorrelationId" -> correlationId,
      "Environment" -> appConfig.pegaEnvironment
    )
    logger.info(s"CorrelationId  $correlationId")
    logger.debug(s"[HeaderGenerator] [headersForPEGA] $headers")
    appConfig.pegaBearerToken match {
      case "" => headers
      case bearerToken => headers ++ Seq(AUTHORIZATION -> s"Bearer $bearerToken")
    }
  }
}
