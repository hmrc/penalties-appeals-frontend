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

import java.time.{LocalDate, LocalDateTime}

class OtherAppealInformationSpec extends SpecBase {
  val uploadJourneyModel: UploadJourney = callbackModel.copy(reference = "ref1", lastUpdated = LocalDateTime.of(2021, 2, 2, 2, 2))
  val uploadJourneyModel2: UploadJourney = callbackModel.copy(reference = "ref2", lastUpdated = LocalDateTime.of(2021, 4, 4, 4, 4))

  "otherAppealInformationWrites" should {
    "write to JSON - no late appeal" in {
      val modelToConvertToJson = OtherAppealInformation(
        reasonableExcuse = "other",
        honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
        statement = Some("I was late. Sorry."),
        supportingEvidence = Some(Evidence(noOfUploadedFiles = 2)),
        lateAppeal = false,
        lateAppealReason = None,
        isClientResponsibleForSubmission = Some(false),
        isClientResponsibleForLateSubmission = Some(true),
        uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
      )
      val expectedResult = Json.parse(
        """
          |{
          | "reasonableExcuse": "other",
          | "honestyDeclaration": true,
          | "startDateOfEvent": "2022-01-01T00:00:00",
          | "statement": "I was late. Sorry.",
          | "supportingEvidence": {
          |   "noOfUploadedFiles": 2
          | },
          | "lateAppeal": false,
          | "isClientResponsibleForSubmission": false,
          | "isClientResponsibleForLateSubmission": true,
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
          |     {
          |       "reference": "ref2",
          |       "fileStatus": "READY",
          |       "downloadUrl": "download.file/url",
          |       "uploadDetails": {
          |         "fileName": "file1.txt",
          |         "fileMimeType": "text/plain",
          |         "uploadTimestamp": "2018-01-01T01:01:00",
          |         "checksum": "check1234",
          |         "size": 2
          |       },
          |       "lastUpdated": "2021-04-04T04:04:00"
          |     }
          |   ]
          |}
          |
          |
          |""".stripMargin)
      val result = Json.toJson(modelToConvertToJson)
      result shouldBe expectedResult
    }

    "write to JSON - late appeal" in {
      val modelToConvertToJson = OtherAppealInformation(
        reasonableExcuse = "other",
        honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
        statement = Some("I was late. Sorry."),
        supportingEvidence = Some(Evidence(noOfUploadedFiles = 2)),
        lateAppeal = true,
        lateAppealReason = Some("This is a reason"),
        isClientResponsibleForSubmission = Some(false),
        isClientResponsibleForLateSubmission = Some(true),
        uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
      )
      val expectedResult = Json.parse(
        """
          |{
          | "reasonableExcuse": "other",
          | "honestyDeclaration": true,
          | "startDateOfEvent": "2022-01-01T00:00:00",
          | "statement": "I was late. Sorry.",
          | "supportingEvidence": {
          |   "noOfUploadedFiles": 2
          | },
          | "lateAppeal": true,
          | "lateAppealReason": "This is a reason",
          | "isClientResponsibleForSubmission": false,
          | "isClientResponsibleForLateSubmission": true,
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
          |     {
          |       "reference": "ref2",
          |       "fileStatus": "READY",
          |       "downloadUrl": "download.file/url",
          |       "uploadDetails": {
          |         "fileName": "file1.txt",
          |         "fileMimeType": "text/plain",
          |         "uploadTimestamp": "2018-01-01T01:01:00",
          |         "checksum": "check1234",
          |         "size": 2
          |       },
          |       "lastUpdated": "2021-04-04T04:04:00"
          |     }
          |   ]
          |}
          |""".stripMargin)
      val result = Json.toJson(modelToConvertToJson)
      result shouldBe expectedResult
    }

    "write to JSON - no evidence" in {
      val modelToConvertToJson = OtherAppealInformation(
        reasonableExcuse = "other",
        honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
        statement = Some("I was late. Sorry."),
        supportingEvidence = None,
        lateAppeal = false,
        lateAppealReason = None,
        isClientResponsibleForSubmission = Some(false),
        isClientResponsibleForLateSubmission = Some(true),
        uploadedFiles = None
      )
      val expectedResult = Json.parse(
        """
          |{
          | "reasonableExcuse": "other",
          | "honestyDeclaration": true,
          | "startDateOfEvent": "2022-01-01T00:00:00",
          | "statement": "I was late. Sorry.",
          | "lateAppeal": false,
          | "isClientResponsibleForSubmission": false,
          | "isClientResponsibleForLateSubmission": true
          |}
          |""".stripMargin)
      val result = Json.toJson(modelToConvertToJson)
      result shouldBe expectedResult
    }
  }

}
