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

package helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

trait WiremockHelper {

  val stubPort = 11111
  val stubHost = "localhost"

  var wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  def host(): String = s"http://$stubHost:$stubPort"

  def start(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
      WireMock.configureFor(stubHost, stubPort)
    }
  }

  def stop(): Unit = {
    wireMockServer.stop()
  }

  def resetAll(): Unit = {
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
    wireMockServer.resetAll()
  }
}
