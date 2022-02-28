/*
 * Copyright 2022 HM Revenue & Customs
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

package models.appeals

import java.time.LocalDateTime

import base.SpecBase
import models.UserRequest
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import play.api.libs.json.{JsValue, Json}
import utils.SessionKeys

class AppealSubmissionSpec extends SpecBase {

  val crimeAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "crime",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "reportedIssueToPolice": true,
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val lossOfStaffAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "lossOfStaff",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val technicalIssuesAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "technicalIssues",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "endDateOfEvent": "2021-04-24T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val fireOrFloodAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "fireOrFlood",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val healthAppealInformationHospitalStayNotOngoingJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "health",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "endDateOfEvent": "2021-04-24T18:25:43.511Z",
      |   "eventOngoing": false,
      |   "hospitalStayInvolved": true,
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val healthAppealInformationHospitalStayOngoingJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "health",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "eventOngoing": true,
      |   "hospitalStayInvolved": true,
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val healthAppealInformationNoHospitalStayJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "health",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "hospitalStayInvolved": false,
      |   "eventOngoing": false,
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val otherAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "other",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "statement": "This is a statement.",
      |   "supportingEvidence": {
      |     "noOfUploadedFiles": 1
      |   },
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true,
      |   "uploadedFiles":[
      |     {
      |       "reference":"xyz",
      |       "fileStatus":"READY",
      |       "downloadUrl":"xyz.com",
      |       "uploadDetails": {
      |         "fileName":"filename.txt",
      |         "fileMimeType":"txt",
      |         "uploadTimestamp":"2020-01-01T01:01:00",
      |         "checksum":"abcde",
      |         "size":1
      |       },
      |       "lastUpdated":"2021-02-02T02:02:00"
      |     },
      |     {
      |       "reference":"abc",
      |       "fileStatus":"READY",
      |       "downloadUrl":"abc.com",
      |       "uploadDetails": {
      |         "fileName":"filename2.pdf",
      |         "fileMimeType":"pdf",
      |         "uploadTimestamp":"2020-03-03T03:03:00",
      |         "checksum":"zyxwv",
      |         "size":1
      |       },
      |       "lastUpdated":"2021-04-04T04:04:00"
      |     }
      |    ]
      |}
      |""".stripMargin
  )

  val otherAppealInformationJsonNoEvidence: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "other",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "statement": "This is a statement.",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val obligationAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "obligation",
      |   "honestyDeclaration": true,
      |   "statement": "This is a statement.",
      |   "supportingEvidence":{
      |     "noOfUploadedFiles": 1
      |   },
      |   "uploadedFiles":[
      |     {
      |       "reference":"xyz",
      |       "fileStatus":"READY",
      |       "downloadUrl":"xyz.com",
      |       "uploadDetails": {
      |         "fileName":"filename.txt",
      |         "fileMimeType":"txt",
      |         "uploadTimestamp":"2020-01-01T01:01:00",
      |         "checksum":"abcde",
      |         "size":1
      |       },
      |       "lastUpdated":"2021-02-02T02:02:00"
      |     },
      |     {
      |       "reference":"abc",
      |       "fileStatus":"READY",
      |       "downloadUrl":"abc.com",
      |       "uploadDetails": {
      |         "fileName":"filename2.pdf",
      |         "fileMimeType":"pdf",
      |         "uploadTimestamp":"2020-03-03T03:03:00",
      |         "checksum":"zyxwv",
      |         "size":1
      |       },
      |       "lastUpdated":"2021-04-04T04:04:00"
      |     }
      |    ]
      |}
      |""".stripMargin
  )

  val obligationAppealInformationJsonNoEvidence: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "obligation",
      |   "honestyDeclaration": true,
      |   "statement": "This is a statement."
      |}
      |""".stripMargin
  )

  val uploadJourneyModel: UploadJourney = UploadJourney(reference = "xyz", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("xyz.com"),
    uploadDetails =
      Some(UploadDetails(
        fileName = "filename.txt",
        fileMimeType = "txt",
        uploadTimestamp = LocalDateTime.of(2020,1,1,1,1),
        checksum = "abcde", size = 1)),
    failureDetails = None, lastUpdated = LocalDateTime.of(2021,2,2,2,2))

  val uploadJourneyModel2: UploadJourney = UploadJourney(reference = "abc", fileStatus = UploadStatusEnum.READY, downloadUrl = Some("abc.com"),
    uploadDetails =
      Some(UploadDetails(
        fileName = "filename2.pdf",
        fileMimeType = "pdf",
        uploadTimestamp = LocalDateTime.of(2020,3,3,3,3),
        checksum = "zyxwv", size = 1)),
    failureDetails = None, lastUpdated = LocalDateTime.of(2021,4,4,4,4))

  "parseAppealInformationToJson" should {
    "for crime" must {
      "parse the appeal information model into a JsObject" in {
        val model = CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssueToPolice = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe crimeAppealInformationJson
      }
    }

    "for fire or flood" must {
      "parse the appeal information model into a JsObject" in {
        val model = FireOrFloodAppealInformation(
          reasonableExcuse = "fireOrFlood",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe fireOrFloodAppealInformationJson
      }
    }

    "for loss of staff" must {
      "parse the appeal information model into a JsObject" in {
        val model = LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfStaff",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe lossOfStaffAppealInformationJson
      }
    }

    "for technical issues" must {
      "parse the appeal information model into a JsObject" in {
        val model = TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssues",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          endDateOfEvent = "2021-04-24T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe technicalIssuesAppealInformationJson
      }
    }

    "for health" must {
      "parse the appeal information model into a JsObject (when a startDateOfEvent and endDateOfEvent is present NOT dateOfEvent)" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
          eventOngoing = false,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationHospitalStayNotOngoingJson
      }

      "parse the appeal information model into a JsObject (when a startDateOfEvent is present NOT dateOfEvent AND endDateOfEvent i.e. " +
        "event ongoing hospital stay)" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          endDateOfEvent = None,
          eventOngoing = true,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationHospitalStayOngoingJson
      }

      "parse the appeal information model into a JsObject (when a dateOfEvent is present NOT startDateOfEvent AND endDateOfEvent i.e. " +
        "no hospital stay)" in {
        val model = HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          endDateOfEvent = None,
          eventOngoing = false,
          hospitalStayInvolved = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationNoHospitalStayJson
      }
    }

    "for other" must {
      "parse the appeal information model into a JsObject" in {
        val model = OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = Some("This is a statement."),
          supportingEvidence = Some(Evidence(
            noOfUploadedFiles = 1
          )),
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true),
          Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe otherAppealInformationJson
      }

      "parse the appeal information model into a JsObject - when no evidence provided" in {
        val model = OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = Some("This is a statement."),
          supportingEvidence = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true),
          uploadedFiles = None
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe otherAppealInformationJsonNoEvidence
      }
    }

    "for obligation" must {
      "parse the appeal information model into a JsObject" in {
        val model = ObligationAppealInformation(
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          statement = Some("This is a statement."),
          supportingEvidence = Some(Evidence(
            noOfUploadedFiles = 1)),
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe obligationAppealInformationJson
      }

      "parse the appeal information model into a JsObject - when no evidence is provided" in {
        val model = ObligationAppealInformation(
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          statement = Some("This is a statement."),
          supportingEvidence = None,
          uploadedFiles = None
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe obligationAppealInformationJsonNoEvidence
      }
    }
  }

  "constructModelBasedOnReasonableExcuse" should {
    "show submitted by agent - when the user is an agent" in {
      val fakeAgentRequestForCrimeJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.whatCausedYouToMissTheDeadline -> "client")
      )

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = false,
        agentReferenceNo = Some("1234567890"), None)(fakeAgentRequestForCrimeJourney)
      result.appealSubmittedBy shouldBe "agent"
      result.agentReferenceNo shouldBe Some("1234567890")
      result.appealInformation shouldBe CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
        startDateOfEvent = "2022-01-01", reportedIssueToPolice = true, statement = None, lateAppeal = false,
        lateAppealReason = None, isClientResponsibleForSubmission = Some(false), isClientResponsibleForLateSubmission = Some(true)
      )
    }

    "show submitted by client - when the user is a client" in {
      val fakeRequestForCrimeJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01",
        SessionKeys.lateAppealReason -> "Some Reason")
      )

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = true,
        None, None)(fakeRequestForCrimeJourney)
      result.appealSubmittedBy shouldBe "client"
      result.appealInformation shouldBe CrimeAppealInformation(
        reasonableExcuse = "crime",
        honestyDeclaration = true,
        startDateOfEvent = "2022-01-01",
        reportedIssueToPolice = true,
        statement = None,
        lateAppeal = true,
        lateAppealReason = Some("Some Reason"),
        isClientResponsibleForSubmission = None,
        isClientResponsibleForLateSubmission = None
      )
    }

    "for bereavement" must {
      "show only the event date (when the bereavement occurred)" in {
        val fakeRequestForBereavementJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01")
        )
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement",
          isLateAppeal = false, None, None)(fakeRequestForBereavementJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe BereavementAppealInformation(
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val fakeRequestForBereavementJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01",
          SessionKeys.whoPlannedToSubmitVATReturn -> "client")
        )
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement",
          isLateAppeal = false, None, None)(fakeRequestForBereavementJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe BereavementAppealInformation(
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for crime" must {
      "change reported issue to false - when the answer for has crime been reported to police is NOT yes" in {
        val fakeRequestForCrimeJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "unknown",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.dateOfCrime -> "2022-01-01")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = false,
          None, None)(fakeRequestForCrimeJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          reportedIssueToPolice = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for fire or flood" must {
      "construct a FireOrFlood model when passed the fire or flood reasonable excuse" in {
        val fakeRequestForFireOrFloodJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "fireOrFlood",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.dateOfFireOrFlood -> "2022-01-01")
        )
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("fireOrFlood",
          isLateAppeal = false, None, None)(fakeRequestForFireOrFloodJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe FireOrFloodAppealInformation(
          reasonableExcuse = "fireOrFlood",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for loss of staff" must {
      "show only the event date (when person left the business)" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff",
          isLateAppeal = false, None, None)(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfStaff",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
          SessionKeys.whoPlannedToSubmitVATReturn -> "client")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", isLateAppeal = false,
          Some("1234567890"), None)(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentReferenceNo shouldBe Some("1234567890")
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfStaff",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for technical issues" must {
      "have the startDateOfEvent and endDate of event" in {
        val fakeRequestForTechnicalIssuesJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
          SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues",
          isLateAppeal = false, None, None)(fakeRequestForTechnicalIssuesJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssues",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          endDateOfEvent = "2022-01-02",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "specify its for an agent when ARN is in session" in {
        val fakeRequestForTechnicalIssuesJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
          SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
          SessionKeys.whoPlannedToSubmitVATReturn -> "client")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues",
          isLateAppeal = false, agentReferenceNo = Some("1234567890"), None)(fakeRequestForTechnicalIssuesJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentReferenceNo shouldBe Some("1234567890")
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssues",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          endDateOfEvent = "2022-01-02",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for health" must {
      "show the startDate and endDate of the hospital stay when eventOngoing = false and hospitalStayInvolved = true" in {
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "yes",
          SessionKeys.whenHealthIssueStarted -> "2022-01-01",
          SessionKeys.whenHealthIssueEnded -> "2022-01-31"
        ))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          agentReferenceNo = Some("1234567890"), None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentReferenceNo shouldBe Some("1234567890")
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = true,
          startDateOfEvent = Some("2022-01-01"),
          endDateOfEvent = Some("2022-01-31"),
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "show only the start date when eventOngoing = true and hospitalStayInvolved = true" in {
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = None)(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> "2022-01-01"
        ))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          None, None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = true,
          startDateOfEvent = Some("2022-01-01"),
          endDateOfEvent = None,
          eventOngoing = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "show the dateOfEvent only when no hospital stay was involved (hospitalStayInvolved = false)" in {
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = None)(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2022-01-01"
        ))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          None, None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = false,
          startDateOfEvent = Some("2022-01-01"),
          endDateOfEvent = None,
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }
    }

    "for other" must {
      "parse the session keys to a model" in {
        val fakeRequestForOtherJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "yes")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = false,
          None, Some(Seq(uploadJourneyModel, uploadJourneyModel2)))(fakeRequestForOtherJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = Some(Evidence(2)),
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
      }

      "parse the session keys to a model - no evidence" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "no")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = false,
          None, uploadedFiles = None)(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = None
        )
      }

      "parse the session keys to a model - for late appeal" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.lateAppealReason -> "This is a reason for appealing late.",
          SessionKeys.isUploadEvidence -> "yes")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = true,
          None, Some(Seq(uploadJourneyModel, uploadJourneyModel2)))(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = true,
          lateAppealReason = Some("This is a reason for appealing late."),
          supportingEvidence = Some(Evidence(2)),
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
      }
    }

    "for obligation" must {
      "parse the session keys to a model" in {
        val fakeRequestForObligationJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.otherRelevantInformation -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "yes")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("obligation", isLateAppeal = false,
          None, Some(Seq(uploadJourneyModel, uploadJourneyModel2)))(fakeRequestForObligationJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe ObligationAppealInformation(
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          statement = Some("This is a reason."),
          supportingEvidence = Some(Evidence(2)),
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
      }

      "parse the session keys to a model - no evidence" in {
        val fakeRequestForObligationJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.otherRelevantInformation -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "no")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("obligation", isLateAppeal = false,
          None, None)(fakeRequestForObligationJourney)
        result.appealSubmittedBy shouldBe "client"
        result.appealInformation shouldBe ObligationAppealInformation(
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          statement = Some("This is a reason."),
          supportingEvidence = None,
          uploadedFiles = None
        )
      }
    }
  }


  "writes" should {
    "for bereavement" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = false,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = BereavementAppealInformation(
            reasonableExcuse = "bereavement",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true)
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> false,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "bereavement",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )
        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }
    "for crime" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = CrimeAppealInformation(
            reasonableExcuse = "crime",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            reportedIssueToPolice = true,
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true)
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "crime",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "reportedIssueToPolice" -> true,
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for fire or flood" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = FireOrFloodAppealInformation(
            reasonableExcuse = "fireOrFlood",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true)
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "fireOrFlood",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for loss of staff" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = LossOfStaffAppealInformation(
            reasonableExcuse = "lossOfStaff",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true)
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "lossOfStaff",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for technical issues" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = TechnicalIssuesAppealInformation(
            reasonableExcuse = "technicalIssues",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            endDateOfEvent = "2021-04-24T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true)
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "technicalIssues",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for health" must {
      "write the appeal to JSON" when {
        "there has been a hospital stay - and is no longer ongoing (both start and end date) - write the appeal model to JSON" in {
          val modelToConvertToJson = AppealSubmission(
            sourceSystem = "MDTP",
            taxRegime = "VAT",
            customerReferenceNo = "VRN1234567890",
            dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
            isLPP = true,
            appealSubmittedBy = "client",
            agentReferenceNo = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
              eventOngoing = false,
              hospitalStayInvolved = true,
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              isClientResponsibleForSubmission = Some(true),
              isClientResponsibleForLateSubmission = Some(true)
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "sourceSystem" -> "MDTP",
            "taxRegime" -> "VAT",
            "customerReferenceNo" -> "VRN1234567890",
            "dateOfAppeal" -> "2020-01-01T00:00:00",
            "isLPP" -> true,
            "appealSubmittedBy" -> "client",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
              "eventOngoing" -> false,
              "hospitalStayInvolved" -> true,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "isClientResponsibleForSubmission" -> true,
              "isClientResponsibleForLateSubmission" -> true
            )
          )
          val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
          result shouldBe jsonRepresentingModel
        }

        "there has been a hospital stay AND it is ongoing (no end date) - write the appeal model to JSON" in {
          val modelToConvertToJson = AppealSubmission(
            sourceSystem = "MDTP",
            taxRegime = "VAT",
            customerReferenceNo = "VRN1234567890",
            dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
            isLPP = true,
            appealSubmittedBy = "client",
            agentReferenceNo = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              endDateOfEvent = None,
              eventOngoing = true,
              hospitalStayInvolved = true,
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              isClientResponsibleForSubmission = Some(true),
              isClientResponsibleForLateSubmission = Some(true)
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "sourceSystem" -> "MDTP",
            "taxRegime" -> "VAT",
            "customerReferenceNo" -> "VRN1234567890",
            "dateOfAppeal" -> "2020-01-01T00:00:00",
            "isLPP" -> true,
            "appealSubmittedBy" -> "client",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "eventOngoing" -> true,
              "hospitalStayInvolved" -> true,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "isClientResponsibleForSubmission" -> true,
              "isClientResponsibleForLateSubmission" -> true
            )
          )
          val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
          result shouldBe jsonRepresentingModel
        }

        "there has been NO hospital stay (dateOfEvent present, eventOngoing = false, hospitalStayInvolved = false) " +
          "write the appeal model to JSON" in {
          val modelToConvertToJson = AppealSubmission(
            sourceSystem = "MDTP",
            taxRegime = "VAT",
            customerReferenceNo = "VRN1234567890",
            dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
            isLPP = true,
            appealSubmittedBy = "client",
            agentReferenceNo = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              endDateOfEvent = None,
              eventOngoing = false,
              hospitalStayInvolved = false,
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              isClientResponsibleForSubmission = Some(true),
              isClientResponsibleForLateSubmission = Some(true)
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "sourceSystem" -> "MDTP",
            "taxRegime" -> "VAT",
            "customerReferenceNo" -> "VRN1234567890",
            "dateOfAppeal" -> "2020-01-01T00:00:00",
            "isLPP" -> true,
            "appealSubmittedBy" -> "client",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "eventOngoing" -> false,
              "hospitalStayInvolved" -> false,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "isClientResponsibleForSubmission" -> true,
              "isClientResponsibleForLateSubmission" -> true
            )
          )
          val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
          result shouldBe jsonRepresentingModel
        }
      }
    }

    "for other" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            )),
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true),
            uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            ),
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true,
            "uploadedFiles" -> Seq(
              Json.obj(
                "reference" -> "xyz",
                "fileStatus" -> "READY",
                "downloadUrl" -> "xyz.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename.txt",
                  "fileMimeType" ->"txt",
                  "uploadTimestamp" -> "2020-01-01T01:01:00",
                  "checksum" -> "abcde",
                  "size" -> 1
                ),
                "lastUpdated" -> "2021-02-02T02:02:00"
              ),
              Json.obj(
                "reference" -> "abc",
                "fileStatus" -> "READY",
                "downloadUrl" -> "abc.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename2.pdf",
                  "fileMimeType" -> "pdf",
                  "uploadTimestamp" -> "2020-03-03T03:03:00",
                  "checksum" -> "zyxwv",
                  "size" -> 1
                ),
                "lastUpdated" -> "2021-04-04T04:04:00"
              )
            )
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }

      "write the model to JSON - no evidence" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = None,
            lateAppeal = false,
            lateAppealReason = None,
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true),
            uploadedFiles = None
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }

      "write the model to JSON - for late appeal" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            )),
            lateAppeal = true,
            lateAppealReason = Some("Late reason"),
            isClientResponsibleForSubmission = Some(true),
            isClientResponsibleForLateSubmission = Some(true),
            uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            ),
            "lateAppeal" -> true,
            "lateAppealReason" -> "Late reason",
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true,
            "uploadedFiles" -> Seq(
              Json.obj(
                "reference" -> "xyz",
                "fileStatus" -> "READY",
                "downloadUrl" -> "xyz.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename.txt",
                  "fileMimeType" ->"txt",
                  "uploadTimestamp" -> "2020-01-01T01:01:00",
                  "checksum" -> "abcde",
                  "size" -> 1
                  ),
                "lastUpdated" -> "2021-02-02T02:02:00"
              ),
              Json.obj(
                "reference" -> "abc",
                "fileStatus" -> "READY",
                "downloadUrl" -> "abc.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename2.pdf",
                  "fileMimeType" -> "pdf",
                  "uploadTimestamp" -> "2020-03-03T03:03:00",
                  "checksum" -> "zyxwv",
                  "size" -> 1
                  ),
                "lastUpdated" -> "2021-04-04T04:04:00"
              )
            )
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for obligation" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = ObligationAppealInformation(
            reasonableExcuse = "obligation",
            honestyDeclaration = true,
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            )),
            uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
          )
        )

        val jsonRepresentModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "obligation",
            "honestyDeclaration" -> true,
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            ),
            "uploadedFiles" -> Seq(
              Json.obj(
                "reference" -> "xyz",
                "fileStatus" -> "READY",
                "downloadUrl" -> "xyz.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename.txt",
                  "fileMimeType" ->"txt",
                  "uploadTimestamp" -> "2020-01-01T01:01:00",
                  "checksum" -> "abcde",
                  "size" -> 1
                ),
                "lastUpdated" -> "2021-02-02T02:02:00"
              ),
              Json.obj(
                "reference" -> "abc",
                "fileStatus" -> "READY",
                "downloadUrl" -> "abc.com",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "filename2.pdf",
                  "fileMimeType" -> "pdf",
                  "uploadTimestamp" -> "2020-03-03T03:03:00",
                  "checksum" -> "zyxwv",
                  "size" -> 1
                ),
                "lastUpdated" -> "2021-04-04T04:04:00"
              )
            )
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentModel
      }

      "write the model to JSON - no evidence" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          sourceSystem = "MDTP",
          taxRegime = "VAT",
          customerReferenceNo = "VRN1234567890",
          dateOfAppeal = LocalDateTime.of(2020,1,1,0,0,0),
          isLPP = true,
          appealSubmittedBy = "client",
          agentReferenceNo = None,
          appealInformation = ObligationAppealInformation(
            reasonableExcuse = "obligation",
            honestyDeclaration = true,
            statement = Some("This was the reason"),
            supportingEvidence = None,
            uploadedFiles = None
          )
        )

        val jsonRepresentModel: JsValue = Json.obj(
          "sourceSystem" -> "MDTP",
          "taxRegime" -> "VAT",
          "customerReferenceNo" -> "VRN1234567890",
          "dateOfAppeal" -> "2020-01-01T00:00:00",
          "isLPP" -> true,
          "appealSubmittedBy" -> "client",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "obligation",
            "honestyDeclaration" -> true,
            "statement" -> "This was the reason"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentModel
      }
    }
  }

  "BereavementAppealInformation" should {
    "bereavementAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = BereavementAppealInformation(
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(BereavementAppealInformation.bereavementAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "bereavement",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

  "CrimeAppealInformation" should {
    "crimeAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssueToPolice = true,
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(CrimeAppealInformation.crimeAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "crime",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "reportedIssueToPolice" -> true,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

  "FireOrFloodAppealInformation" should {
    "fireOrFloodAppealWrites" must {
      "write the appeal model to Json" in {
        val model = FireOrFloodAppealInformation(
          reasonableExcuse = "fireOrFlood",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "fireOrFlood",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

  "LossOfStaffAppealInformation" should {
    "lossOfStaffAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfStaff",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "lossOfStaff",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

  "TechnicalIssuesAppealInformation" should {
    "technicalIssuesAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssues",
          honestyDeclaration = true,
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          endDateOfEvent = "2021-04-24T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          isClientResponsibleForSubmission = Some(false),
          isClientResponsibleForLateSubmission = Some(true)
        )
        val result = Json.toJson(model)(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
        result shouldBe Json.obj(
          "reasonableExcuse" -> "technicalIssues",
          "honestyDeclaration" -> true,
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "isClientResponsibleForSubmission" -> false,
          "isClientResponsibleForLateSubmission" -> true
        )
      }
    }
  }

  "HealthAppealInformation" should {
    "healthAppealWrites" must {
      "write the appeal to JSON" when {
        "there has been a hospital stay - and is no longer ongoing (both start and end date) - write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            reasonableExcuse = "health",
            honestyDeclaration = true,
            startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
            eventOngoing = false,
            hospitalStayInvolved = true,
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            isClientResponsibleForSubmission = Some(false),
            isClientResponsibleForLateSubmission = Some(true)
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
            "eventOngoing" -> false,
            "hospitalStayInvolved" -> true,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "isClientResponsibleForSubmission" -> false,
            "isClientResponsibleForLateSubmission" -> true
          )
        }

        "there has been a hospital stay AND it is ongoing (no end date) - write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            reasonableExcuse = "health",
            honestyDeclaration = true,
            startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            endDateOfEvent = None,
            eventOngoing = true,
            hospitalStayInvolved = true,
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            isClientResponsibleForSubmission = Some(false),
            isClientResponsibleForLateSubmission = Some(true)
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "eventOngoing" -> true,
            "hospitalStayInvolved" -> true,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "isClientResponsibleForSubmission" -> false,
            "isClientResponsibleForLateSubmission" -> true
          )
        }

        "there has been NO hospital stay (dateOfEvent present, eventOngoing = false, hospitalStayInvolved = false) " +
          "write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            reasonableExcuse = "health",
            honestyDeclaration = true,
            startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            endDateOfEvent = None,
            eventOngoing = false,
            hospitalStayInvolved = false,
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            isClientResponsibleForSubmission = Some(false),
            isClientResponsibleForLateSubmission = Some(true)
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "eventOngoing" -> false,
            "hospitalStayInvolved" -> false,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "isClientResponsibleForSubmission" -> false,
            "isClientResponsibleForLateSubmission" -> true
          )
        }
      }
    }
  }

  "OtherAppealInformation" should {
    "otherAppealInformationWrites" should {
      "write to JSON - no late appeal" in {
        val modelToConvertToJson = OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01T13:00:00.000Z",
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(Evidence(1)),
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
            | "startDateOfEvent": "2022-01-01T13:00:00.000Z",
            | "statement": "I was late. Sorry.",
            | "supportingEvidence": {
            |   "noOfUploadedFiles": 1
            | },
            | "lateAppeal": false,
            | "isClientResponsibleForSubmission": false,
            | "isClientResponsibleForLateSubmission": true,
            | "uploadedFiles": [
            |   {
            |     "reference": "xyz",
            |     "fileStatus": "READY",
            |     "downloadUrl": "xyz.com",
            |     "uploadDetails": {
            |         "fileName": "filename.txt",
            |         "fileMimeType": "txt",
            |         "uploadTimestamp": "2020-01-01T01:01:00",
            |         "checksum": "abcde",
            |         "size": 1
            |       },
            |       "lastUpdated": "2021-02-02T02:02:00"
            |     },
            |     {
            |       "reference": "abc",
            |       "fileStatus": "READY",
            |       "downloadUrl": "abc.com",
            |       "uploadDetails": {
            |         "fileName": "filename2.pdf",
            |         "fileMimeType": "pdf",
            |         "uploadTimestamp": "2020-03-03T03:03:00",
            |         "checksum": "zyxwv",
            |         "size": 1
            |       },
            |       "lastUpdated": "2021-04-04T04:04:00"
            |     }
            |   ]
            |}
            |""".stripMargin)
        val result = Json.toJson(modelToConvertToJson)
        result shouldBe expectedResult
      }

      "write to JSON - late appeal" in {
        val modelToConvertToJson = OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = "2022-01-01T13:00:00.000Z",
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(Evidence(1)),
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
            | "startDateOfEvent": "2022-01-01T13:00:00.000Z",
            | "statement": "I was late. Sorry.",
            | "supportingEvidence": {
            |   "noOfUploadedFiles": 1
            | },
            | "lateAppeal": true,
            | "lateAppealReason": "This is a reason",
            | "isClientResponsibleForSubmission": false,
            | "isClientResponsibleForLateSubmission": true,
            | "uploadedFiles": [
            |   {
            |     "reference": "xyz",
            |     "fileStatus": "READY",
            |     "downloadUrl": "xyz.com",
            |     "uploadDetails": {
            |         "fileName": "filename.txt",
            |         "fileMimeType": "txt",
            |         "uploadTimestamp": "2020-01-01T01:01:00",
            |         "checksum": "abcde",
            |         "size": 1
            |       },
            |       "lastUpdated": "2021-02-02T02:02:00"
            |     },
            |     {
            |       "reference": "abc",
            |       "fileStatus": "READY",
            |       "downloadUrl": "abc.com",
            |       "uploadDetails": {
            |         "fileName": "filename2.pdf",
            |         "fileMimeType": "pdf",
            |         "uploadTimestamp": "2020-03-03T03:03:00",
            |         "checksum": "zyxwv",
            |         "size": 1
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
          startDateOfEvent = "2022-01-01T13:00:00.000Z",
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
            | "startDateOfEvent": "2022-01-01T13:00:00.000Z",
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

  "ObligationAppealInformation" should {
    "obligationAppealInformationWrites" should {
      "write to JSON" in {
        val modelToConvertToJson = ObligationAppealInformation(
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(
            Evidence(
              noOfUploadedFiles = 1
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
            |   "noOfUploadedFiles": 1
            | },
            | "uploadedFiles": [
            |   {
            |     "reference": "xyz",
            |     "fileStatus": "READY",
            |     "downloadUrl": "xyz.com",
            |     "uploadDetails": {
            |         "fileName": "filename.txt",
            |         "fileMimeType": "txt",
            |         "uploadTimestamp": "2020-01-01T01:01:00",
            |         "checksum": "abcde",
            |         "size": 1
            |       },
            |       "lastUpdated": "2021-02-02T02:02:00"
            |     },
            |     {
            |       "reference": "abc",
            |       "fileStatus": "READY",
            |       "downloadUrl": "abc.com",
            |       "uploadDetails": {
            |         "fileName": "filename2.pdf",
            |         "fileMimeType": "pdf",
            |         "uploadTimestamp": "2020-03-03T03:03:00",
            |         "checksum": "zyxwv",
            |         "size": 1
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
}