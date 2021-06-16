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
      |   "lateAppeal": false
      |}
      |""".stripMargin
  )

  val lossOfStaffAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "lossOfStaff",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false
      |}
      |""".stripMargin
  )

  val technicalIssuesAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "technicalIssues",
      |   "startDateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "endDateOfEvent": "2021-04-24T18:25:43.511Z",
      |   "lateAppeal": false
      |}
      |""".stripMargin
  )

  val fireOrFloodAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "type": "fireOrFlood",
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "lateAppeal": false
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
          lateAppealReason = None
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
          lateAppealReason = None
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
          lateAppealReason = None
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
          lateAppealReason = None
        )
        val result = AppealSubmission.parseAppealInformationToJson(model)
        result shouldBe technicalIssuesAppealInformationJson
      }
    }
  }

  "constructModelBasedOnReasonableExcuse" should {
    "show submitted by agent - when the user is an agent" in {
      val fakeAgentRequestForCrimeJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
        SessionKeys.reasonableExcuse -> "crime",
        SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.dateOfCrime -> "2022-01-01")
      )

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", false)(fakeAgentRequestForCrimeJourney)
      result.reasonableExcuse shouldBe "crime"
      result.penaltyId shouldBe "123"
      result.submittedBy shouldBe "agent"
      result.honestyDeclaration shouldBe true
      result.appealInformation shouldBe CrimeAppealInformation(
        `type` = "crime", dateOfEvent = "2022-01-01", reportedIssue = true, statement = None, lateAppeal = false, lateAppealReason = None
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

      val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", true)(fakeRequestForCrimeJourney)
      result.reasonableExcuse shouldBe "crime"
      result.penaltyId shouldBe "123"
      result.submittedBy shouldBe "client"
      result.honestyDeclaration shouldBe true
      result.appealInformation shouldBe CrimeAppealInformation(
        `type` = "crime", dateOfEvent = "2022-01-01", reportedIssue = true, statement = None, lateAppeal = true, lateAppealReason = Some("Some Reason")
      )
    }

    "for crime" must {
      "change reported issue to false - when the answer for has crime been reported to police is NOT yes" in {
        val fakeRequestForCrimeJourney = UserRequest("123456789")(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "crime",
          SessionKeys.hasCrimeBeenReportedToPolice -> "unknown",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.dateOfCrime -> "2022-01-01")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("crime", false)(fakeRequestForCrimeJourney)
        result.reasonableExcuse shouldBe "crime"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe CrimeAppealInformation(
          `type` = "crime", dateOfEvent = "2022-01-01", reportedIssue = false, statement = None, lateAppeal = false, lateAppealReason = None
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
        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("fireOrFlood", false)(fakeRequestForFireOrFloodJourney)
        result.reasonableExcuse shouldBe "fireOrFlood"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe FireOrFloodAppealInformation(
          `type` = "fireOrFlood", dateOfEvent = "2022-01-01", statement = None, lateAppeal = false, lateAppealReason = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", false)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "lossOfStaff"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          `type` = "lossOfStaff", dateOfEvent = "2022-01-01", statement = None, lateAppeal = false, lateAppealReason = None
        )
      }

      "set the appeal submittedBy to agent when ARN exists in session" in {
        val fakeRequestForLossOfStaffJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("lossOfStaff", false)(fakeRequestForLossOfStaffJourney)
        result.reasonableExcuse shouldBe "lossOfStaff"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "agent"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe LossOfStaffAppealInformation(
          `type` = "lossOfStaff", dateOfEvent = "2022-01-01", statement = None, lateAppeal = false, lateAppealReason = None
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

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues", false)(fakeRequestForTechnicalIssuesJourney)
        result.reasonableExcuse shouldBe "technicalIssues"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "client"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          `type` = "technicalIssues", startDateOfEvent = "2022-01-01", endDateOfEvent = "2022-01-02", statement = None, lateAppeal = false, lateAppealReason = None
        )
      }

      "specify its for an agent when ARN is in session" in {
        val fakeRequestForTechnicalIssuesJourney = UserRequest("123456789", arn = Some("AGENT1"))(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
          SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02")
        )

        val result = AppealSubmission.constructModelBasedOnReasonableExcuse("technicalIssues", false)(fakeRequestForTechnicalIssuesJourney)
        result.reasonableExcuse shouldBe "technicalIssues"
        result.penaltyId shouldBe "123"
        result.submittedBy shouldBe "agent"
        result.honestyDeclaration shouldBe true
        result.appealInformation shouldBe TechnicalIssuesAppealInformation(
          `type` = "technicalIssues", startDateOfEvent = "2022-01-01", endDateOfEvent = "2022-01-02", statement = None, lateAppeal = false, lateAppealReason = None
        )
      }
    }
  }


  "writes" should {
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
            lateAppealReason = None
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
            "lateAppeal" -> false
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
            lateAppealReason = None
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
            "lateAppeal" -> false
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
            lateAppealReason = None
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
            "lateAppeal" -> false
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
            lateAppealReason = None
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
            "lateAppeal" -> false
          )
        )

        val result = Json.toJson(modelToConvertToJson)(AppealSubmission.writes)
        result shouldBe jsonRepresentingModel
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
          lateAppealReason = Some("Reason")
        )
        val result = Json.toJson(model)(CrimeAppealInformation.crimeAppealWrites)
        result shouldBe Json.obj(
          "type" -> "crime",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "reportedIssue" -> true,
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason"
        )
      }
    }

    "fireOrFloodAppealWrites" must {
      "write the appeal model to Json" in {
        val model = FireOrFloodAppealInformation(
          `type` = "fireOrFlood",
          dateOfEvent = "2021-04-23T18:25:43.511Z",
          statement = None,
          lateAppeal = true,
          lateAppealReason = Some("Reason")
        )
        val result = Json.toJson(model)(FireOrFloodAppealInformation.fireOrFloodAppealWrites)
        result shouldBe Json.obj(
          "type" -> "fireOrFlood",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason"
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
          lateAppealReason = Some("Reason")
        )
        val result = Json.toJson(model)(LossOfStaffAppealInformation.lossOfStaffAppealWrites)
        result shouldBe Json.obj(
          "type" -> "lossOfStaff",
          "dateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason"
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
          lateAppealReason = Some("Reason")
        )
        val result = Json.toJson(model)(TechnicalIssuesAppealInformation.technicalIssuesAppealWrites)
        result shouldBe Json.obj(
          "type" -> "technicalIssues",
          "startDateOfEvent" -> "2021-04-23T18:25:43.511Z",
          "endDateOfEvent" -> "2021-04-24T18:25:43.511Z",
          "lateAppeal" -> true,
          "lateAppealReason" -> "Reason"
        )
      }
    }
  }
}