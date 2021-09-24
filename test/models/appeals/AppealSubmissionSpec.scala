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

package models.appeals

import base.SpecBase
import models.UserRequest
import play.api.libs.json.{JsValue, Json}
import utils.SessionKeys

class AppealSubmissionSpec extends SpecBase {

  val crimeAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "crime",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "reportedIssue": true,
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val lossOfStaffAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "lossOfStaff",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val technicalIssuesAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "technicalIssues",
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "endDateOfEvent": "2021-04-24T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val fireOrFloodAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "fireOrFlood",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val healthAppealInformationHospitalStayNotOngoingJson: JsValue = Json.parse(
    """
      |{
      |   "type": "health",
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "endDateOfEvent": "2021-04-24T18:25:43.511Z",
      |   "eventOngoing": false,
      |   "hospitalStayInvolved": true,
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val healthAppealInformationHospitalStayOngoingJson: JsValue = Json.parse(
    """
      |{
      |   "type": "health",
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "eventOngoing": true,
      |   "hospitalStayInvolved": true,
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val healthAppealInformationNoHospitalStayJson: JsValue = Json.parse(
    """
      |{
      |   "type": "health",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "hospitalStayInvolved": false,
      |   "eventOngoing": false,
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val otherAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "other",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "statement": "This is a statement.",
      |   "supportingEvidence": {
      |     "noOfUploadedFiles": 1
      |   },
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val otherAppealInformationJsonNoEvidence: JsValue = Json.parse(
    """
      |{
      |   "type": "other",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "statement": "This is a statement.",
      |   "lateAppeal": false,
      |   "whoPlannedToSubmit": "agent",
      |   "causeOfLateSubmissionAgent": "client"
      |}
      |""".stripMargin
  )

  val obligationAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "obligation",
      |   "statement": "This is a statement.",
      |   "supportingEvidence":{
      |     "noOfUploadedFiles": 1
      |   }
      |}
      |""".stripMargin
  )

  val obligationAppealInformationJsonNoEvidence: JsValue = Json.parse(
    """
      |{
      |   "type": "obligation",
      |   "statement": "This is a statement."
      |}
      |""".stripMargin
  )

  "parseAppealInformationToJson" should {
    "for crime" must {
      "parse the appeal information model into a JsObject" in {
        val model = CrimeAppealInformation(
          `type` = "crime",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssue = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe crimeAppealInformationJson
      }
    }

    "for fire or flood" must {
      "parse the appeal information model into a JsObject" in {
        val model = FireOrFloodAppealInformation(
          `type` = "fireOrFlood",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe fireOrFloodAppealInformationJson
      }
    }

    "for loss of staff" must {
      "parse the appeal information model into a JsObject" in {
        val model = LossOfStaffAppealInformation(
          `type` = "lossOfStaff",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe lossOfStaffAppealInformationJson
      }
    }

    "for technical issues" must {
      "parse the appeal information model into a JsObject" in {
        val model = TechnicalIssuesAppealInformation(
          `type` = "technicalIssues",
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          endDateOfEvent = "2021-04-24T18:25:43.511Z",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe technicalIssuesAppealInformationJson
      }
    }

    "for health" must {
      "parse the appeal information model into a JsObject (when a startDateOfEvent and endDateOfEvent is present NOT dateOfEvent)" in {
        val model = HealthAppealInformation(
          `type` = "health",
          startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
          dateOfEvent = None,
          eventOngoing = false,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationHospitalStayNotOngoingJson
      }

      "parse the appeal information model into a JsObject (when a startDateOfEvent is present NOT dateOfEvent AND endDateOfEvent i.e. " +
        "event ongoing hospital stay)" in {
        val model = HealthAppealInformation(
          `type` = "health",
          startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          endDateOfEvent = None,
          dateOfEvent = None,
          eventOngoing = true,
          hospitalStayInvolved = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationHospitalStayOngoingJson
      }

      "parse the appeal information model into a JsObject (when a dateOfEvent is present NOT startDateOfEvent AND endDateOfEvent i.e. " +
        "no hospital stay)" in {
        val model = HealthAppealInformation(
          `type` = "health",
          startDateOfEvent = None,
          endDateOfEvent = None,
          dateOfEvent = Some("2021-04-23T18:25:43.511Z"),
          eventOngoing = false,
          hospitalStayInvolved = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe healthAppealInformationNoHospitalStayJson
      }
    }

    "for other" must {
      "parse the appeal information model into a JsObject" in {
        val model = OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = Some("This is a statement."),
          supportingEvidence = Some(Evidence(
            noOfUploadedFiles = 1
          )),
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe otherAppealInformationJson
      }

      "parse the appeal information model into a JsObject - when no evidence provided" in {
        val model = OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = Some("This is a statement."),
          supportingEvidence = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe otherAppealInformationJsonNoEvidence
      }
    }

    "for obligation" must {
      "parse the appeal information model into a JsObject" in {
        val model = ObligationAppealInformation(
          `type` = "obligation",
          statement = Some("This is a statement."),
          supportingEvidence = Some(Evidence(
            noOfUploadedFiles = 1))
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe obligationAppealInformationJson
      }

      "parse the appeal information model into a JsObject - when no evidence is provided" in{
        val model = ObligationAppealInformation(
          `type` = "obligation",
          statement = Some("This is a statement."),
          supportingEvidence = None
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
        SessionKeys.causeOfLateSubmissionAgent -> "client")
      )

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", false, 0)(fakeAgentRequestForCrimeJourney)
      result.reasonableExcuse shouldBe "crime"
      result.penaltyId shouldBe "123"
      result.submittedBy shouldBe "agent"
      result.honestyDeclaration shouldBe true
      result.appealInformation shouldBe CrimeAppealInformation(
        `type` = "crime", dateOfEvent = "2022-01-01", reportedIssue = true, statement = None, lateAppeal = false, lateAppealReason = None,
        whoPlannedToSubmit = Some("agent"),
        causeOfLateSubmissionAgent = Some("client")
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

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", true, 0)(fakeRequestForCrimeJourney)
      result.reasonableExcuse shouldBe "crime"
      result.penaltyId shouldBe "123"
      result.submittedBy shouldBe "client"
      result.honestyDeclaration shouldBe true
      result.appealInformation shouldBe CrimeAppealInformation(
        `type` = "crime",
        dateOfEvent = "2022-01-01",
        reportedIssue = true,
        statement = None,
        lateAppeal = true,
        lateAppealReason = Some("Some Reason"),
        whoPlannedToSubmit = None,
        causeOfLateSubmissionAgent = None
      )
    }

    "for bereavement" must {
      "show only the event date (when the bereavement occurred)" in {
        val fakeRequestForBereavementJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01")
        )
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement", false, 0)(fakeRequestForBereavementJourney)
        result.reasonableExcuse shouldBe "bereavement"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe BereavementAppealInformation(
          `type` = "bereavement",
          dateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val fakeRequestForBereavementJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01",
          SessionKeys.whoPlannedToSubmitVATReturn -> "client")
        )
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("bereavement", false, 0)(fakeRequestForBereavementJourney)
        result.reasonableExcuse shouldBe "bereavement"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe BereavementAppealInformation(
          `type` = "bereavement",
          dateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("client"),
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", false, 0)(fakeRequestForCrimeJourney)
        result.reasonableExcuse shouldBe "crime"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe CrimeAppealInformation(
          `type` = "crime",
          dateOfEvent = "2022-01-01",
          reportedIssue = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
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
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("fireOrFlood", false, 0)(fakeRequestForFireOrFloodJourney)
        result.reasonableExcuse shouldBe "fireOrFlood"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe FireOrFloodAppealInformation(
          `type` = "fireOrFlood",
          dateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", false, 0)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "lossOfStaff"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          `type` = "lossOfStaff",
          dateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
          SessionKeys.whoPlannedToSubmitVATReturn -> "client")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", false, 0)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "lossOfStaff"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "agent"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          `type` = "lossOfStaff",
          dateOfEvent = "2022-01-01",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("client"),
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues", false, 0)(fakeRequestForTechnicalIssuesJourney)
        result.reasonableExcuse shouldBe "technicalIssues"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          `type` = "technicalIssues",
          startDateOfEvent = "2022-01-01",
          endDateOfEvent = "2022-01-02",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues", false, 0)(fakeRequestForTechnicalIssuesJourney)
        result.reasonableExcuse shouldBe "technicalIssues"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "agent"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          `type` = "technicalIssues",
          startDateOfEvent = "2022-01-01",
          endDateOfEvent = "2022-01-02",
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = Some("client"),
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", false, 0)(fakeRequestForHealthJourney)
        result.reasonableExcuse shouldBe "health"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "agent"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe HealthAppealInformation(
          `type` = "health",
          hospitalStayInvolved = true,
          dateOfEvent = None,
          startDateOfEvent = Some("2022-01-01"),
          endDateOfEvent = Some("2022-01-31"),
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", false, 0)(fakeRequestForHealthJourney)
        result.reasonableExcuse shouldBe "health"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe HealthAppealInformation(
          `type` = "health",
          hospitalStayInvolved = true,
          dateOfEvent = None,
          startDateOfEvent = Some("2022-01-01"),
          endDateOfEvent = None,
          eventOngoing = true,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }

      "show the dateOfEvent only when no hospital stay was involved (hospitalStayInvolved = false)" in {
        val fakeRequestForHealthJourney = UserRequest("123456789", arn = None)(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2022-01-01"
        ))

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("health", false, 0)(fakeRequestForHealthJourney)
        result.reasonableExcuse shouldBe "health"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe HealthAppealInformation(
          `type` = "health",
          hospitalStayInvolved = false,
          dateOfEvent = Some("2022-01-01"),
          startDateOfEvent = None,
          endDateOfEvent = None,
          eventOngoing = false,
          statement = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }
    }

    "for other" must {
      "parse the session keys to a model" in {
        val fakeRequestForOtherJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.evidenceFileName -> "file1.txt",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", false, 1)(fakeRequestForOtherJourney)
        result.reasonableExcuse shouldBe "other"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = Some(Evidence(1)),
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }

      "parse the session keys to a model - no evidence" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", false, 0)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "other"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = false,
          lateAppealReason = None,
          supportingEvidence = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }

      "parse the session keys to a model - for late appeal" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.evidenceFileName -> "file1.txt",
          SessionKeys.lateAppealReason -> "This is a reason for appealing late.")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("other", true, 1)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "other"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01",
          statement = Some("This is a reason."),
          lateAppeal = true,
          lateAppealReason = Some("This is a reason for appealing late."),
          supportingEvidence = Some(Evidence(1)),
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
      }
    }

    "for obligation" must {
      "parse the session keys to a model" in {
        val fakeRequestForObligationJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.evidenceFileName -> "file1.txt",
          SessionKeys.otherRelevantInformation -> "This is a reason.")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("obligation", false, 1)(fakeRequestForObligationJourney)
        result.reasonableExcuse shouldBe "obligation"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe ObligationAppealInformation(
          `type` = "obligation",
          statement = Some("This is a reason."),
          supportingEvidence = Some(Evidence(1))
        )
      }

      "parse the session keys to a model - no evidence" in {
        val fakeRequestForObligationJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.otherRelevantInformation -> "This is a reason.")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("obligation", false, 0)(fakeRequestForObligationJourney)
        result.reasonableExcuse shouldBe "obligation"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe ObligationAppealInformation(
          `type` = "obligation",
          statement = Some("This is a reason."),
          supportingEvidence = None
        )
      }
    }
  }


  "writes" should {
    "for bereavement" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "bereavement",
          honestyDeclaration = true,
          appealInformation = BereavementAppealInformation(
            `type` = "bereavement",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "bereavement",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "bereavement",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )
        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }
    "for crime" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "crime",
          honestyDeclaration = true,
          appealInformation = CrimeAppealInformation(
            `type` = "crime",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            reportedIssue = true,
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "crime",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "crime",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "reportedIssue" -> true,
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for fire or flood" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "fireOrFlood",
          honestyDeclaration = true,
          appealInformation = FireOrFloodAppealInformation(
            `type` = "fireOrFlood",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "fireOrFlood",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "fireOrFlood",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for loss of staff" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "lossOfStaff",
          honestyDeclaration = true,
          appealInformation = LossOfStaffAppealInformation(
            `type` = "lossOfStaff",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )
        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "lossOfStaff",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "lossOfStaff",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for technical issues" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "technicalIssues",
          honestyDeclaration = true,
          appealInformation = TechnicalIssuesAppealInformation(
            `type` = "technicalIssues",
            startDateOfEvent = "2021-04-23T18:25:43.511Z",
            endDateOfEvent = "2021-04-24T18:25:43.511Z",
            statement = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "technicalIssues",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "technicalIssues",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
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
            submittedBy = "client",
            penaltyId = "1234",
            reasonableExcuse = "health",
            honestyDeclaration = true,
            appealInformation = HealthAppealInformation(
              `type` = "health",
              startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
              eventOngoing = false,
              hospitalStayInvolved = true,
              dateOfEvent = None,
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              whoPlannedToSubmit = Some("agent"),
              causeOfLateSubmissionAgent = Some("client")
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "submittedBy" -> "client",
            "penaltyId" -> "1234",
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "appealInformation" -> Json.obj(
              "type" -> "health",
              "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
              "eventOngoing" -> false,
              "hospitalStayInvolved" -> true,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "whoPlannedToSubmit" -> "agent",
              "causeOfLateSubmissionAgent" -> "client"
            )
          )
          val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
          result shouldBe jsonRepresentingModel
        }

        "there has been a hospital stay AND it is ongoing (no end date) - write the appeal model to JSON" in {
          val modelToConvertToJson = AppealSubmission(
            submittedBy = "client",
            penaltyId = "1234",
            reasonableExcuse = "health",
            honestyDeclaration = true,
            appealInformation = HealthAppealInformation(
              `type` = "health",
              startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              endDateOfEvent = None,
              eventOngoing = true,
              hospitalStayInvolved = true,
              dateOfEvent = None,
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              whoPlannedToSubmit = Some("agent"),
              causeOfLateSubmissionAgent = Some("client")
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "submittedBy" -> "client",
            "penaltyId" -> "1234",
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "appealInformation" -> Json.obj(
              "type" -> "health",
              "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "eventOngoing" -> true,
              "hospitalStayInvolved" -> true,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "whoPlannedToSubmit" -> "agent",
              "causeOfLateSubmissionAgent" -> "client"
            )
          )
          val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
          result shouldBe jsonRepresentingModel
        }

        "there has been NO hospital stay (dateOfEvent present, eventOngoing = false, hospitalStayInvolved = false) " +
          "write the appeal model to JSON" in {
          val modelToConvertToJson = AppealSubmission(
            submittedBy = "client",
            penaltyId = "1234",
            reasonableExcuse = "health",
            honestyDeclaration = true,
            appealInformation = HealthAppealInformation(
              `type` = "health",
              startDateOfEvent = None,
              endDateOfEvent = None,
              eventOngoing = false,
              hospitalStayInvolved = false,
              dateOfEvent = Some("2021-04-23T18:25:43.511Z"),
              statement = None,
              lateAppeal = true,
              lateAppealReason = Some("Reason"),
              whoPlannedToSubmit = Some("agent"),
              causeOfLateSubmissionAgent = Some("client")
            )
          )
          val jsonRepresentingModel: JsValue = Json.obj(
            "submittedBy" -> "client",
            "penaltyId" -> "1234",
            "reasonableExcuse" -> "health",
            "honestyDeclaration" -> true,
            "appealInformation" -> Json.obj(
              "type" -> "health",
              "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
              "eventOngoing" -> false,
              "hospitalStayInvolved" -> false,
              "lateAppeal" -> true,
              "lateAppealReason" -> "Reason",
              "whoPlannedToSubmit" -> "agent",
              "causeOfLateSubmissionAgent" -> "client"
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
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "other",
          honestyDeclaration = true,
          appealInformation = OtherAppealInformation(
            `type` = "other",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            )),
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "other",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "other",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            ),
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }

      "write the model to JSON - no evidence" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "other",
          honestyDeclaration = true,
          appealInformation = OtherAppealInformation(
            `type` = "other",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = None,
            lateAppeal = false,
            lateAppealReason = None,
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "other",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "other",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "lateAppeal" -> false,
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }

      "write the model to JSON - for late appeal" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "other",
          honestyDeclaration = true,
          appealInformation = OtherAppealInformation(
            `type` = "other",
            dateOfEvent = "2021-04-23T18:25:43.511Z",
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            )),
            lateAppeal = true,
            lateAppealReason = Some("Late reason"),
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
        )

        val jsonRepresentingModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "other",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "other",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            ),
            "lateAppeal" -> true,
            "lateAppealReason" -> "Late reason",
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
      }
    }

    "for obligation" must {
      "write the model to JSON" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          appealInformation = ObligationAppealInformation(
            `type` = "obligation",
            statement = Some("This was the reason"),
            supportingEvidence = Some(Evidence(
              noOfUploadedFiles = 1
            ))
          )
        )

        val jsonRepresentModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "obligation",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "obligation",
            "statement" -> "This was the reason",
            "supportingEvidence" -> Json.obj(
              "noOfUploadedFiles" -> 1
            )
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentModel
      }

      "write the model to JSON - no evidence" in {
        val modelToConvertToJson: AppealSubmission = AppealSubmission(
          submittedBy = "client",
          penaltyId = "1234",
          reasonableExcuse = "obligation",
          honestyDeclaration = true,
          appealInformation = ObligationAppealInformation(
            `type` = "obligation",
            statement = Some("This was the reason"),
            supportingEvidence = None
          )
        )

        val jsonRepresentModel: JsValue = Json.obj(
          "submittedBy" -> "client",
          "penaltyId" -> "1234",
          "reasonableExcuse" -> "obligation",
          "honestyDeclaration" -> true,
          "appealInformation" -> Json.obj(
            "type" -> "obligation",
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
          `type` = "bereavement",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = Json.toJson(model)(BereavementAppealInformation.bereavementAppealWrites)
        result shouldBe Json.obj(
          "type" -> "bereavement",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "whoPlannedToSubmit" -> "agent",
          "causeOfLateSubmissionAgent" -> "client"
        )
      }
    }
  }

  "CrimeAppealInformation" should {
    "crimeAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = CrimeAppealInformation(
          `type` = "crime",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          reportedIssue = true,
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = Json.toJson(model)(CrimeAppealInformation.crimeAppealWrites)
        result shouldBe Json.obj(
          "type" -> "crime",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "reportedIssue" -> true,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "whoPlannedToSubmit" -> "agent",
          "causeOfLateSubmissionAgent" -> "client"
        )
      }
    }
  }

  "FireOrFloodAppealInformation" should {
    "fireOrFloodAppealWrites" must {
      "write the appeal model to Json" in {
        val model = FireOrFloodAppealInformation(
          `type` = "fireOrFlood",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = Json.toJson(model)(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
        result shouldBe Json.obj(
          "type" -> "fireOrFlood",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "whoPlannedToSubmit" -> "agent",
          "causeOfLateSubmissionAgent" -> "client"
        )
      }
    }
  }

  "LossOfStaffAppealInformation" should {
    "lossOfStaffAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = LossOfStaffAppealInformation(
          `type` = "lossOfStaff",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = Json.toJson(model)(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
        result shouldBe Json.obj(
          "type" -> "lossOfStaff",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "whoPlannedToSubmit" -> "agent",
          "causeOfLateSubmissionAgent" -> "client"
        )
      }
    }
  }

  "TechnicalIssuesAppealInformation" should {
    "technicalIssuesAppealWrites" must {
      "write the appeal model to JSON" in {
        val model = TechnicalIssuesAppealInformation(
          `type` = "technicalIssues",
          startDateOfEvent = "2021-04-23T18:25:43.511Z",
          endDateOfEvent = "2021-04-24T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason"),
          whoPlannedToSubmit = Some("agent"),
          causeOfLateSubmissionAgent = Some("client")
        )
        val result = Json.toJson(model)(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
        result shouldBe Json.obj(
          "type" -> "technicalIssues",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason",
          "whoPlannedToSubmit" -> "agent",
          "causeOfLateSubmissionAgent" -> "client"
        )
      }
    }
  }

  "HealthAppealInformation" should {
    "healthAppealWrites" must {
      "write the appeal to JSON" when {
        "there has been a hospital stay - and is no longer ongoing (both start and end date) - write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            `type` = "health",
            startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            endDateOfEvent = Some("2021-04-24T18:25:43.511Z"),
            eventOngoing = false,
            hospitalStayInvolved = true,
            dateOfEvent = None,
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "type" -> "health",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
            "eventOngoing" -> false,
            "hospitalStayInvolved" -> true,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        }

        "there has been a hospital stay AND it is ongoing (no end date) - write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            `type` = "health",
            startDateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            endDateOfEvent = None,
            eventOngoing = true,
            hospitalStayInvolved = true,
            dateOfEvent = None,
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "type" -> "health",
            "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "eventOngoing" -> true,
            "hospitalStayInvolved" -> true,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        }

        "there has been NO hospital stay (dateOfEvent present, eventOngoing = false, hospitalStayInvolved = false) " +
          "write the appeal model to JSON" in {
          val model = HealthAppealInformation(
            `type` = "health",
            startDateOfEvent = None,
            endDateOfEvent = None,
            eventOngoing = false,
            hospitalStayInvolved = false,
            dateOfEvent = Some("2021-04-23T18:25:43.511Z"),
            statement = None,
            lateAppeal = true,
            lateAppealReason = Some("Reason"),
            whoPlannedToSubmit = Some("agent"),
            causeOfLateSubmissionAgent = Some("client")
          )
          val result = Json.toJson(model)(HealthAppealInformation.healthAppealWrites)
          result shouldBe Json.obj(
            "type" -> "health",
            "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
            "eventOngoing" -> false,
            "hospitalStayInvolved" -> false,
            "lateAppeal" -> true,
            "lateAppealReason" -> "Reason",
            "whoPlannedToSubmit" -> "agent",
            "causeOfLateSubmissionAgent" -> "client"
          )
        }
      }
    }
  }

  "OtherAppealInformation" should {
    "otherAppealInformationWrites" should {
      "write to JSON - no late appeal" in {
        val modelToConvertToJson = OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01T13:00:00.000Z",
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(Evidence(1)),
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
        val expectedResult = Json.parse(
          """
            |{
            | "type": "other",
            | "dateOfEvent": "2022-01-01T13:00:00.000Z",
            | "statement": "I was late. Sorry.",
            | "supportingEvidence": {
            |   "noOfUploadedFiles": 1
            | },
            | "lateAppeal": false
            |}
            |""".stripMargin)
        val result = Json.toJson(modelToConvertToJson)
        result shouldBe expectedResult
      }

      "write to JSON - late appeal" in {
        val modelToConvertToJson = OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01T13:00:00.000Z",
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(Evidence(1)),
          lateAppeal = true,
          lateAppealReason = Some("This is a reason"),
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
        val expectedResult = Json.parse(
          """
            |{
            | "type": "other",
            | "dateOfEvent": "2022-01-01T13:00:00.000Z",
            | "statement": "I was late. Sorry.",
            | "supportingEvidence": {
            |   "noOfUploadedFiles": 1
            | },
            | "lateAppeal": true,
            | "lateAppealReason": "This is a reason"
            |}
            |""".stripMargin)
        val result = Json.toJson(modelToConvertToJson)
        result shouldBe expectedResult
      }

      "write to JSON - no evidence" in {
        val modelToConvertToJson = OtherAppealInformation(
          `type` = "other",
          dateOfEvent = "2022-01-01T13:00:00.000Z",
          statement = Some("I was late. Sorry."),
          supportingEvidence = None,
          lateAppeal = false,
          lateAppealReason = None,
          whoPlannedToSubmit = None,
          causeOfLateSubmissionAgent = None
        )
        val expectedResult = Json.parse(
          """
            |{
            | "type": "other",
            | "dateOfEvent": "2022-01-01T13:00:00.000Z",
            | "statement": "I was late. Sorry.",
            | "lateAppeal": false
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
          `type` = "obligation",
          statement = Some("I was late. Sorry."),
          supportingEvidence = Some(
            Evidence(
              noOfUploadedFiles = 1
            )
        ))
        val expectedResult = Json.parse(
          """
            |{
            | "type": "obligation",
            | "statement": "I was late. Sorry.",
            | "supportingEvidence": {
            |   "noOfUploadedFiles": 1
            | }
            |}
            |""".stripMargin)
        val result = Json.toJson(modelToConvertToJson)
        result shouldBe expectedResult
      }

      "write to JSON - no evidence" in {
        val modelToConvertToJson = ObligationAppealInformation(
          `type` = "obligation",
          statement = Some("I was late. Sorry."),
          supportingEvidence = None
        )
        val expectedResult = Json.parse(
          """
            |{
            | "type": "obligation",
            | "statement": "I was late. Sorry."
            |}
            |""".stripMargin)
        val result = Json.toJson(modelToConvertToJson)
        result shouldBe expectedResult
      }
    }
  }
}