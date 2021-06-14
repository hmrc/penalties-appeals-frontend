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
  val crimeAppealJson: JsValue = Json.parse(
    """
      |{
      |    "submittedBy": "client",
      |    "penaltyId": "1234567890",
      |    "reasonableExcuse": "ENUM_PEGA_LIST",
      |    "honestyDeclaration": true,
      |    "appealInformation": {
      |						"type": "crime",
      |            "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |            "reportedIssue": true
      |		}
      |}
      |""".stripMargin)

  val crimeAppealJsonWithKeyMissing: JsValue = Json.parse(
    """
      |{
      |    "submittedBy": "client",
      |    "penaltyId": "1234567890",
      |    "reasonableExcuse": "ENUM_PEGA_LIST",
      |    "appealInformation": {
      |						"type": "crime",
      |            "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |            "reportedIssue": true
      |		}
      |}
      |""".stripMargin)

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

  val invalidCrimeAppealInformationJson: JsValue = Json.parse(
    """
      |{
      |   "dateOfEvent": "2021-04-23T18:25:43.511Z",
      |   "reportedIssue": true
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
  }
}