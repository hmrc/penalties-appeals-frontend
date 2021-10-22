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

import models.upload.{UploadFormTemplateRequest, UpscanInitiateRequest, UpscanInitiateResponseModel}
import play.api.libs.json.Json
import play.api.test.Helpers._
import stubs.UpscanStub._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import scala.concurrent.ExecutionContext

class UpscanInitiateConnectorISpec extends IntegrationSpecCommonBase {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val upscanConnector: UpscanConnector = injector.instanceOf[UpscanConnector]

  val requestModelNoUrl: UpscanInitiateRequest = UpscanInitiateRequest(
    callbackUrl = "",
    successRedirect = None,
    errorRedirect = None,
    minimumFileSize = Some(1),
    maximumFileSize = Some(1024)
  )
  val upscanResponseModel: UpscanInitiateResponseModel =
    UpscanInitiateResponseModel(
      "123456789",
      UploadFormTemplateRequest("bar", Map("doo" -> "dar"))
    )

  "initiateToUpscan" should {

    val responseBody = Json
      .obj(
        "reference" -> "123456789",
        "uploadRequest" -> Json.obj(
          "href" -> "bar",
          "fields" -> Json.obj(
            "doo" -> "dar"
          )
        )
      )
      .toString()

    "return a Right when a successful call is made" in {
      successfulInitiateCall(responseBody)
      val requestModel = UpscanInitiateRequest(
        callbackUrl = "https://foo/wrongUrl",
        successRedirect = None,
        errorRedirect = None,
        minimumFileSize = Some(1),
        maximumFileSize = Some(1024)
      )
      val result = await(upscanConnector.initiateToUpscan(requestModel))
      result.isRight shouldBe true
      result.right.get shouldBe upscanResponseModel
    }

    "return a Right invalid json error when invalid JSON is parsed " in {
      successfulCallInvalidJson
      val result = await(upscanConnector.initiateToUpscan(requestModelNoUrl))
      result.isRight shouldBe false
      result.left.get.status shouldBe BAD_REQUEST
      result.left.get.body shouldBe "Invalid JSON received"
    }

    "return a Left when an unsuccessful call is made" in {
      failedInitiateCall(responseBody)
      val result = await(upscanConnector.initiateToUpscan(requestModelNoUrl))
      result.isRight shouldBe false
      result.left.get.status shouldBe BAD_REQUEST
    }
  }

}
