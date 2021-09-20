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

package controllers.internal

import base.SpecBase
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import uk.gov.hmrc.mongo.cache.DataKey

import java.time.LocalDateTime

class UpscanCallbackControllerSpec extends SpecBase {
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]
  val controller: UpscanCallbackController = new UpscanCallbackController(repository)(implicitly, stubMessagesControllerComponents())

  val invalidJsonBody: String =
    """
      |{
      | this: is invalid
      |}
      |
      |""".stripMargin

  val validJsonButInvalidModel: JsValue = Json.parse(
    """
      |{
      | "not": "recognised"
      |}
      |
      |""".stripMargin)

  val validCallbackFromUpscan: JsValue = Json.parse(
    """
      |{
      |    "reference": "ref1",
      |    "downloadUrl": "download.url",
      |    "fileStatus": "READY",
      |    "uploadDetails": {
      |        "fileName": "file1.txt",
      |        "fileMimeType": "text/plain",
      |        "uploadTimestamp": "2018-04-24T09:30:00Z",
      |        "checksum": "check12345678",
      |        "size": 987
      |    }
      |}
      |""".stripMargin
  )

  val uploadJourneyModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 4, 24, 9, 30),
      checksum = "check12345678",
      size = 987
    ))
  )

  "UpscanController" should {
    "return an ISE" when {
      s"the body can not be parsed to an $UploadJourney model" in {
        val result = controller.callbackFromUpscan("12345")(fakeRequest.withBody(validJsonButInvalidModel))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return NO CONTENT" when {
      "the body is valid and state has been updated" in {
        val result = controller.callbackFromUpscan("12345")(fakeRequest.withBody(validCallbackFromUpscan))
        status(result) shouldBe NO_CONTENT
        val modelInRepo: UploadJourney = await(repository.get[UploadJourney]("12345")(DataKey("ref1"))).get
        modelInRepo shouldBe uploadJourneyModel
      }
    }
  }
}