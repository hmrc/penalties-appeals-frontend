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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import utils.EnrolmentKeys

object AuthStub {
  private val authoriseUri = "/auth/authorise"

  def authorised(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
              "affinityGroup" -> "Organisation",
              "allEnrolments" -> Json.arr(
                Json.obj(
                  "key" -> EnrolmentKeys.mtdVATEnrolmentKey,
                  "identifiers" -> Json.arr(
                    Json.obj(
                      "key" -> EnrolmentKeys.vrnId,
                      "value" -> "123456789",
                      "state" -> EnrolmentKeys.activated
                    )
                  )
                )
              ),
              "internalId" -> "internal",
              "groupIdentifier" -> "123456789-ABCD-123456789",
              "credentialRole" -> "role1",
              "optionalCredentials" -> Json.obj(
                "providerId" -> "12345",
                "providerType" -> "credType"
              )
            ).toString()
          )
      )
    )
  }

  def agentAuthorised(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            Json.obj(
              "affinityGroup"-> "Agent",
              "allEnrolments" -> Json.arr(
                Json.obj(
                  "key" -> EnrolmentKeys.agentEnrolmentKey,
                  "identifiers" -> Json.arr(
                    Json.obj(
                      "key" -> "AgentReferenceNumber",
                      "value" -> "123456789",
                      "state" -> EnrolmentKeys.activated
                    )
                  )
                )
              ),
              "internalId" -> "internal",
              "groupIdentifier" -> "123456789-ABCD-123456789",
              "credentialRole" -> "role1",
              "optionalCredentials" -> Json.obj(
                "providerId" -> "12345",
                "providerType" -> "credType"
              )
            ).toString()
          )
      )
    )
  }

  def unauthorised(): StubMapping = {
    stubFor(post(urlMatching(authoriseUri))
      .willReturn(
        aResponse()
          .withStatus(Status.UNAUTHORIZED)
      )
    )
  }
}