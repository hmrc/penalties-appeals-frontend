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

package models.appeals.submission

import base.SpecBase
import models.appeals.Evidence
import models.upload.UploadJourney
import play.api.libs.json.Json

import java.time.LocalDateTime

class ObligationAppealInformationSpec extends SpecBase {
  val uploadJourneyModel: UploadJourney = callbackModel.copy(reference = "ref1", lastUpdated = LocalDateTime.of(2021, 2, 2, 2, 2))
  val uploadJourneyModel2: UploadJourney = callbackModel.copy(reference = "ref2", lastUpdated = LocalDateTime.of(2021, 2, 2, 2, 2))

  "obligationAppealInformationWrites" should {
    "write to JSON" in {
      val modelToConvertToJson = ObligationAppealInformation(
        reasonableExcuse = "obligation",
        honestyDeclaration = true,
        statement = Some("I was late. Sorry."),
        supportingEvidence = Some(
          Evidence(
            noOfUploadedFiles = 2
          )
        ),
        uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
      )
      val expectedResult = Json.parse(
        """
          |{
          | "reasonableExcuse": "obligation",
          | "honestyDeclaration": true,
          | "statement": "I was late. Sorry.",
          | "supportingEvidence": {
          |   "noOfUploadedFiles": 2
          | },
          | "uploadedFiles": [
          |   {
          |     "reference": "ref1",
          |     "fileStatus": "READY",
          |     "downloadUrl": "download.file/url",
          |     "uploadDetails": {
          |         "fileName": "file1.txt",
          |         "fileMimeType": "text/plain",
          |         "uploadTimestamp": "2018-01-01T01:01:00",
          |         "checksum": "check1234",
          |         "size": 2
          |       },
          |       "lastUpdated": "2021-02-02T02:02:00"
          |     },
          |   {
          |     "reference": "ref2",
          |     "fileStatus": "READY",
          |     "downloadUrl": "download.file/url",
          |     "uploadDetails": {
          |         "fileName": "file1.txt",
          |         "fileMimeType": "text/plain",
          |         "uploadTimestamp": "2018-01-01T01:01:00",
          |         "checksum": "check1234",
          |         "size": 2
          |       },
          |       "lastUpdated": "2021-02-02T02:02:00"
          |     }
          |   ]
          |}
          |""".stripMargin)
      val result = Json.toJson(modelToConvertToJson)
      result shouldBe expectedResult
    }

    "write to JSON - no evidence" in {
      val modelToConvertToJson = ObligationAppealInformation(
        reasonableExcuse = "obligation",
        honestyDeclaration = true,
        statement = Some("I was late. Sorry."),
        supportingEvidence = None,
        uploadedFiles = None
      )
      val expectedResult = Json.parse(
        """
          |{
          | "reasonableExcuse": "obligation",
          | "honestyDeclaration": true,
          | "statement": "I was late. Sorry."
          |}
          |""".stripMargin)
      val result = Json.toJson(modelToConvertToJson)
      result shouldBe expectedResult
    }
  }

}
