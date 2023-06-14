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

package connectors.httpParsers

import base.SpecBase
import models.appeals.AppealSubmissionResponseModel
import play.api.http.Status
import play.api.http.Status.{CONFLICT, OK, REQUEST_TIMEOUT}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.PagerDutyHelper.PagerDutyKeys

class AppealSubmissionParserSpec extends SpecBase with LogCapturing {

  val appealSubmissionResponseModel: AppealSubmissionResponseModel = AppealSubmissionResponseModel(
    caseId = Some("PR-1234"), status = OK, error = Some("No error, just testing")
  )

  val appealSubmissionJson: JsValue = Json.parse(
    """
      |{
      | "caseId": "PR-1234",
      | "status": 200,
      | "error": "No error, just testing"
      |}
      |""".stripMargin)

  val conflictJsonResponse: JsValue = Json.parse(
    """
      |{
      |  "failures": [
      |    {
      |      "code": "DUPLICATE_SUBMISSION",
      |      "reason": "Some reason"
      |    }
      |  ]
      |}
      |""".stripMargin)

  val requestTimeoutJsonResponse: JsValue = Json.parse(
    """
      |{
      | "failures" : [
      |   {
      |     "code": "REQUEST_TIMEOUT",
      |     "reason": "Timing out..."
      |    }
      | ]
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.obj("foo" -> "bar")

  class Setup(status: Int, optJson: Option[JsValue] = None, responseHeaders: Map[String, Seq[String]] = Map.empty) {

    val httpResponse: AnyRef with HttpResponse = HttpResponse.apply(status, optJson.get, responseHeaders)
    private val httpMethod = "GET"
    private val url = "/"

    def readResponse: AppealSubmissionHTTPParser.AppealSubmissionResponse =
      AppealSubmissionHTTPParser.AppealSubmissionReads.read(httpMethod, url, httpResponse)
  }

  "reads" should {
    s"return the model if the status is ${Status.OK} and the model is parsed" in new Setup(Status.OK, Some(appealSubmissionJson)) {
      readResponse shouldBe Right(appealSubmissionResponseModel)
    }

    s"return the model if the status is ${Status.MULTI_STATUS} and the model is parsed" in new Setup(Status.MULTI_STATUS, Some(appealSubmissionJson)) {
      readResponse shouldBe Right(appealSubmissionResponseModel)
    }

    s"return InvalidJson if the status is ${Status.OK} but the model can not be parsed" in new Setup(Status.OK, Some(invalidJson)) {
      withCaptureOfLoggingFrom(logger) {
        logs => {
          readResponse shouldBe Left(InvalidJson)
          logs.exists(_.getMessage.contains(PagerDutyKeys.INVALID_JSON_RECEIVED_FROM_PENALTIES.toString)) shouldBe true
        }
      }
    }

    s"return $UnexpectedFailure if status is ${Status.CONFLICT}" in new Setup(Status.CONFLICT, optJson = Some(conflictJsonResponse)) {
      val responseAsString: String =
        """
          |{
          |  "failures": [
          |    {
          |      "code": "DUPLICATE_SUBMISSION",
          |      "reason": "Some reason"
          |    }
          |  ]
          |}
          |""".stripMargin
        readResponse.isLeft shouldBe true
        readResponse.left.toOption.get.status shouldBe CONFLICT
        readResponse.left.toOption.get.body.replaceAll("\\s", "") shouldBe responseAsString.replaceAll("\\s", "")
    }

    s"return $UnexpectedFailure if a non OK status code is returned" in new Setup(Status.REQUEST_TIMEOUT, optJson = Some(requestTimeoutJsonResponse)) {
      val responseAsString: String =
        """
          |{
          | "failures": [
          |     {
          |       "code": "REQUEST_TIMEOUT",
          |       "reason": "Timing out..."
          |     }
          |   ]
          |}
          |""".stripMargin
      withCaptureOfLoggingFrom(logger) {
        logs => {
          readResponse.isLeft shouldBe true
          readResponse.left.toOption.get.status shouldBe REQUEST_TIMEOUT
          readResponse.left.toOption.get.body.replaceAll("\\s",  "") shouldBe responseAsString.replaceAll("\\s", "")
          logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES.toString)) shouldBe true

        }
      }
    }
  }
}
