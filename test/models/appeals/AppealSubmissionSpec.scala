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

package models.appeals

import base.SpecBase
import models.UserRequest
import models.appeals.submission._
import models.session.UserAnswers
import models.upload.UploadJourney
import play.api.libs.json.{JsValue, Json}
import utils.SessionKeys

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}

class AppealSubmissionSpec extends SpecBase {

  val crimeAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "crime",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "reportedIssueToPolice": "yes",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val lossOfStaffAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "lossOfEssentialStaff",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val technicalIssuesAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "technicalIssue",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "endDateOfEvent": "2021-04-24T00:00:01",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val fireOrFloodAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "fireandflood",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T00:00:00",
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
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "endDateOfEvent": "2021-04-24T00:00:01",
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
      |   "startDateOfEvent": "2021-04-23T00:00:00",
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
      |   "startDateOfEvent": "2021-04-23T00:00:00",
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
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "statement": "This is a statement.",
      |   "supportingEvidence": {
      |     "noOfUploadedFiles": 2
      |   },
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true,
      |   "uploadedFiles":[
      |     {
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
      |    ]
      |}
      |""".stripMargin
  )

  val otherAppealInformationJsonNoEvidence: JsValue = Json.parse(
    """
      |{
      |   "reasonableExcuse": "other",
      |   "honestyDeclaration": true,
      |   "startDateOfEvent": "2021-04-23T00:00:00",
      |   "statement": "This is a statement.",
      |   "lateAppeal": false,
      |   "isClientResponsibleForSubmission": false,
      |   "isClientResponsibleForLateSubmission": true
      |}
      |""".stripMargin
  )

  val uploadJourneyModel: UploadJourney = callbackModel.copy(reference = "ref1", lastUpdated = LocalDateTime.of(2021, 2, 2, 2, 2))
  val uploadJourneyModel2: UploadJourney = callbackModel.copy(reference = "ref2", lastUpdated = LocalDateTime.of(2021, 4, 4, 4, 4))

  "parseAppealInformationToJson" should {
    "for crime" must {
      "parse the appeal information model into a JsObject" in {
        val model = CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
          reportedIssueToPolice = "yes",
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
          reasonableExcuse = "fireandflood",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          reasonableExcuse = "lossOfEssentialStaff",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          reasonableExcuse = "technicalIssue",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
          endDateOfEvent = LocalDate.parse("2021-04-24").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS),
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
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
          endDateOfEvent = Some(LocalDate.parse("2021-04-24").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS)),
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
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
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
          startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
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
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
          statement = Some("This is a statement."),
          supportingEvidence = Some(Evidence(
            noOfUploadedFiles = 2
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
          startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
  }

  "constructModelBasedOnReasonableExcuse" should {
    "show submitted by agent - when the user is an agent" in {
      val userAnswers = UserAnswers("1234", Json.obj(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
        SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
        SessionKeys.whatCausedYouToMissTheDeadline -> "client"
      ) ++ correctUserAnswers)
      val fakeAgentRequestForCrimeJourney = UserRequest(vrn = "123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = false,
        agentReferenceNo = Some("AGENT1"), None)(fakeAgentRequestForCrimeJourney)
      result.appealSubmittedBy shouldBe "agent"
      result.agentDetails shouldBe Some(AgentDetails("AGENT1", isExcuseRelatedToAgent = false))
      result.appealInformation shouldBe CrimeAppealInformation(reasonableExcuse = "crime", honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(), reportedIssueToPolice = "yes", statement = None, lateAppeal = false,
        lateAppealReason = None, isClientResponsibleForSubmission = Some(false), isClientResponsibleForLateSubmission = Some(true)
      )
    }

    "show submitted by customer - when the user is a customer" in {
      val userAnswers = UserAnswers("1234", Json.obj(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01"),
        SessionKeys.lateAppealReason -> "Some Reason"
      ) ++ correctUserAnswers)
      val fakeRequestForCrimeJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = true,
        None, None)(fakeRequestForCrimeJourney)
      result.appealSubmittedBy shouldBe "customer"
      result.appealInformation shouldBe CrimeAppealInformation(
        reasonableExcuse = "crime",
        honestyDeclaration = true,
        startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
        reportedIssueToPolice = "yes",
        statement = None,
        lateAppeal = true,
        lateAppealReason = Some("Some Reason"),
        isClientResponsibleForSubmission = None,
        isClientResponsibleForLateSubmission = None
      )
    }

    "for bereavement" must {
      "show only the event date (when the bereavement occurred)" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement",
          isLateAppeal = false, None, None)(fakeRequestForBereavementJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe BereavementAppealInformation(
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01"),
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        ) ++ correctUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctUserAnswers))
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.appealInformation shouldBe BereavementAppealInformation(
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01"),
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for crime" must {
      "change reported issue to unknown - when the answer for has crime been reported to police is unknown" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "unknown",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForCrimeJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = false,
          None, None)(fakeRequestForCrimeJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          reportedIssueToPolice = "unknown",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "change reported issue to no - when the answer for has crime been reported to police is no" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "no",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForCrimeJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", isLateAppeal = false,
          None, None)(fakeRequestForCrimeJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe CrimeAppealInformation(
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          reportedIssueToPolice = "no",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "no",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for fire or flood" must {
      "construct a FireOrFlood model when passed the fire or flood reasonable excuse" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "fireOrFlood",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForFireOrFloodJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("fireOrFlood",
          isLateAppeal = false, None, None)(fakeRequestForFireOrFloodJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe FireOrFloodAppealInformation(
          reasonableExcuse = "fireandflood",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "fireOrFlood",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("fireOrFlood",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for loss of staff" must {
      "show only the event date (when person left the business)" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff",
          isLateAppeal = false, None, None)(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfEssentialStaff",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01"),
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        ) ++ correctUserAnswers)
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", isLateAppeal = false,
          agentReferenceNo = Some("AGENT1"), None)(fakeRequestForLossOfStaffJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentDetails shouldBe Some(AgentDetails("AGENT1", false))
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          reasonableExcuse = "lossOfEssentialStaff",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01"),
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for technical issues" must {
      "have the startDateOfEvent and endDate of event" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "technicalIssues",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
        ) ++ correctUserAnswers)
        val fakeRequestForTechnicalIssuesJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues",
          isLateAppeal = false, None, None)(fakeRequestForTechnicalIssuesJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssue",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          endDateOfEvent = LocalDate.parse("2022-01-02").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "specify its for an agent when ARN is in session" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "technicalIssues",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02"),
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        ) ++ correctUserAnswers)
        val fakeRequestForTechnicalIssuesJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForTechnicalIssuesJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentDetails shouldBe Some(AgentDetails("AGENT1", false))
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          reasonableExcuse = "technicalIssue",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          endDateOfEvent = LocalDate.parse("2022-01-02").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS),
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "technicalIssues",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for health" must {
      "show the startDate and endDate of the hospital stay when eventOngoing = false and hospitalStayInvolved = true" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "yes",
          SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenHealthIssueEnded -> LocalDate.parse("2022-01-31"),
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        ) ++ correctUserAnswers)
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          agentReferenceNo = Some("AGENT1"), None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "agent"
        result.agentDetails shouldBe Some(AgentDetails("AGENT1", false))
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = true,
          startDateOfEvent = Some(LocalDate.parse("2022-01-01").atStartOfDay()),
          endDateOfEvent = Some(LocalDate.parse("2022-01-31").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS)),
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = Some(true),
          isClientResponsibleForLateSubmission = None
        )
      }

      "show only the start date when eventOngoing = true and hospitalStayInvolved = true" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "yes",
          SessionKeys.hasHealthEventEnded -> "no",
          SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = None, answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          None, None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = true,
          startDateOfEvent = Some(LocalDate.parse("2022-01-01").atStartOfDay()),
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
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2022-01-01")
        ) ++ correctUserAnswers)
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = None, answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", isLateAppeal = false,
          None, None)(fakeRequestForHealthJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe HealthAppealInformation(
          reasonableExcuse = "health",
          honestyDeclaration = true,
          hospitalStayInvolved = false,
          startDateOfEvent = Some(LocalDate.parse("2022-01-01").atStartOfDay()),
          endDateOfEvent = None,
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2022-01-01")
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
      }
    }

    "for other" must {
      "parse the session keys to a model" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "yes"
        ) ++ correctUserAnswers)
        val fakeRequestForOtherJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = false,
          None, Some(Seq(uploadJourneyModel, uploadJourneyModel2)))(fakeRequestForOtherJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = Some(Evidence(noOfUploadedFiles = 2)),
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
      }

      "parse the session keys to a model - no evidence" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "no"
        ) ++ correctUserAnswers)
        val fakeRequestForObligationJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = false,
          None, uploadedFiles = None)(fakeRequestForObligationJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = None
        )
      }

      "parse the session keys to a model - remove supportingEvidence object when no files have been uploaded" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "yes"
        ) ++ correctUserAnswers)
        val fakeRequestForOtherJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = false,
          None, Some(Seq()))(fakeRequestForOtherJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = None,
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = Some(Seq())
        )
      }

      "parse the session keys to a model - for late appeal" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.lateAppealReason -> "This is a reason for appealing late.",
          SessionKeys.isUploadEvidence -> "yes"
        ) ++ correctUserAnswers)
        val fakeRequestForObligationJourney = UserRequest("123456789", answers = userAnswers)(fakeRequestConverter(correctUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", isLateAppeal = true,
          None, Some(Seq(uploadJourneyModel, uploadJourneyModel2)))(fakeRequestForObligationJourney)
        result.appealSubmittedBy shouldBe "customer"
        result.appealInformation shouldBe OtherAppealInformation(
          reasonableExcuse = "other",
          honestyDeclaration = true,
          startDateOfEvent = LocalDate.parse("2022-01-01").atStartOfDay(),
          statement = Some("This is a reason."),
          lateAppeal = true,
          lateAppealReason = Some("This is a reason for appealing late."),
          supportingEvidence = Some(Evidence(noOfUploadedFiles = 2)),
          isClientResponsibleForSubmission = None,
          isClientResponsibleForLateSubmission = None,
          uploadedFiles = Some(Seq(uploadJourneyModel, uploadJourneyModel2))
        )
      }

      "set isClientResponsibleForSubmission and isClientResponsibleForLateSubmission value to true when an agent appeals an LPP" in {
        val userAnswers = UserAnswers("1234", Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01"),
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.isUploadEvidence -> "no"
        ) ++ correctLPPUserAnswers)
        val fakeRequestForBereavementJourney = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers)(fakeRequestConverter(correctLPPUserAnswers))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other",
          isLateAppeal = false, agentReferenceNo = Some("AGENT1"), None)(fakeRequestForBereavementJourney)
        result.appealInformation.isClientResponsibleForLateSubmission shouldBe Some(true)
        result.appealInformation.isClientResponsibleForSubmission shouldBe Some(true)
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = false,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = BereavementAppealInformation(
            reasonableExcuse = "bereavement",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "bereavement",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = CrimeAppealInformation(
            reasonableExcuse = "crime",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
            reportedIssueToPolice = "yes",
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "crime",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
            "reportedIssueToPolice" -> "yes",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = FireOrFloodAppealInformation(
            reasonableExcuse = "fireandflood",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "fireandflood",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = LossOfStaffAppealInformation(
            reasonableExcuse = "lossOfEssentialStaff",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "lossOfEssentialStaff",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = TechnicalIssuesAppealInformation(
            reasonableExcuse = "technicalIssue",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
            endDateOfEvent = LocalDate.parse("2021-04-24").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS),
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "technicalIssue",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
            "endDateOfEvent" -> "2021-04-24T00:00:01",
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
            dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            isLPP = true,
            appealSubmittedBy = "customer",
            agentDetails = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
              endDateOfEvent = Some(LocalDate.parse("2021-04-24").atTime(LocalTime.of(0, 0, 1)).truncatedTo(ChronoUnit.SECONDS)),
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
            "appealSubmittedBy" -> "customer",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T00:00:00",
              "endDateOfEvent" -> "2021-04-24T00:00:01",
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
            dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            isLPP = true,
            appealSubmittedBy = "customer",
            agentDetails = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
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
            "appealSubmittedBy" -> "customer",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T00:00:00",
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
            dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
            isLPP = true,
            appealSubmittedBy = "customer",
            agentDetails = None,
            appealInformation = HealthAppealInformation(
              reasonableExcuse = "health",
              honestyDeclaration = true,
              startDateOfEvent = Some(LocalDate.parse("2021-04-23").atStartOfDay()),
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
            "appealSubmittedBy" -> "customer",
            "appealInformation" -> Json.obj(
              "reasonableExcuse" -> "health",
              "honestyDeclaration" -> true,
              "startDateOfEvent" -> "2021-04-23T00:00:00",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 2
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 2
            ),
            "lateAppeal" -> false,
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true,
            "uploadedFiles" -> Seq(
              Json.obj(
                "reference" -> "ref1",
                "fileStatus" -> "READY",
                "downloadUrl" -> "download.file/url",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "file1.txt",
                  "fileMimeType" -> "text/plain",
                  "uploadTimestamp" -> "2018-01-01T01:01:00",
                  "checksum" -> "check1234",
                  "size" -> 2
                ),
                "lastUpdated" -> "2021-02-02T02:02:00"
              ),
              Json.obj(
                "reference" -> "ref2",
                "fileStatus" -> "READY",
                "downloadUrl" -> "download.file/url",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "file1.txt",
                  "fileMimeType" -> "text/plain",
                  "uploadTimestamp" -> "2018-01-01T01:01:00",
                  "checksum" -> "check1234",
                  "size" -> 2
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
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
          dateOfAppeal = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
          isLPP = true,
          appealSubmittedBy = "customer",
          agentDetails = None,
          appealInformation = OtherAppealInformation(
            reasonableExcuse = "other",
            honestyDeclaration = true,
            startDateOfEvent = LocalDate.parse("2021-04-23").atStartOfDay(),
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 2
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
          "appealSubmittedBy" -> "customer",
          "appealInformation" -> Json.obj(
            "reasonableExcuse" -> "other",
            "honestyDeclaration" -> true,
            "startDateOfEvent" -> "2021-04-23T00:00:00",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 2
            ),
            "lateAppeal" -> true,
            "lateAppealReason" -> "Late reason",
            "isClientResponsibleForSubmission" -> true,
            "isClientResponsibleForLateSubmission" -> true,
            "uploadedFiles" -> Seq(
              Json.obj(
                "reference" -> "ref1",
                "fileStatus" -> "READY",
                "downloadUrl" -> "download.file/url",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "file1.txt",
                  "fileMimeType" -> "text/plain",
                  "uploadTimestamp" -> "2018-01-01T01:01:00",
                  "checksum" -> "check1234",
                  "size" -> 2
                ),
                "lastUpdated" -> "2021-02-02T02:02:00"
              ),
              Json.obj(
                "reference" -> "ref2",
                "fileStatus" -> "READY",
                "downloadUrl" -> "download.file/url",
                "uploadDetails" -> Json.obj(
                  "fileName" -> "file1.txt",
                  "fileMimeType" -> "text/plain",
                  "uploadTimestamp" -> "2018-01-01T01:01:00",
                  "checksum" -> "check1234",
                  "size" -> 2
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
  }
}