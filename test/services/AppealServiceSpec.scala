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

package services

import base.SpecBase
import connectors.PenaltiesConnector
import models.{ReasonableExcuse, UserRequest}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

class AppealServiceSpec extends SpecBase {
  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val fakeRequestForCrimeJourney = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
    SessionKeys.reasonableExcuse -> "crime",
    SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
    SessionKeys.hasConfirmedDeclaration -> "true",
    SessionKeys.dateOfCrime -> "2022-01-01")
  )

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00",
      | "dueDate": "2020-02-07T13:00:00",
      | "dateCommunicationSent": "2020-02-08T13:00:00"
      |}
      |""".stripMargin)

  class Setup {
    reset(mockPenaltiesConnector)
    val service: AppealService = new AppealService(mockPenaltiesConnector)
  }

  "validatePenaltyIdForEnrolmentKey" should {
    "return None when the connector returns None" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(Json.parse("{}"))))

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return Some when the connector returns Json that is parseable to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataAsJson)))

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new UserRequest[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }
  }

  "getReasonableExcuseListAndParse" should {
    s"call the connector and parse the result to $Some $Seq $ReasonableExcuse" in new Setup {
      val jsonRepresentingSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crimeReason"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      when(mockPenaltiesConnector.getListOfReasonableExcuses()(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(
          Some(jsonRepresentingSeqOfReasonableExcuses)
        ))

      val result = await(service.getReasonableExcuseListAndParse())
      result.isDefined shouldBe true
      result.get shouldBe Seq(
        ReasonableExcuse(
          `type` = "bereavement",
          descriptionKey = "reasonableExcuses.bereavementReason",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "crime",
          descriptionKey = "reasonableExcuses.crimeReason",
          isOtherOption = false
        ),
        ReasonableExcuse(
          `type` = "fireOrFlood",
          descriptionKey = "reasonableExcuses.fireOrFloodReason",
          isOtherOption = false
        )
      )
    }

    s"call the connector and return $None" when {
      "the connector call succeeds but invalid json is returned and therefore can not be parsed" in new Setup {
        val jsonRepresentingInvalidSeqOfReasonableExcuses: JsValue = Json.parse(
          """
            |{
            |  "excusesssss": [
            |    {
            |      "type": "bereavement",
            |      "descriptionKey": "reasonableExcuses.bereavementReason"
            |    },
            |    {
            |      "type": "crime",
            |      "descriptionKey": "reasonableExcuses.crimeReason"
            |    },
            |    {
            |      "type": "fireOrFlood",
            |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
            |    }
            |  ]
            |}
            |""".stripMargin
        )
        when(mockPenaltiesConnector.getListOfReasonableExcuses()(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(
            Some(jsonRepresentingInvalidSeqOfReasonableExcuses)
          ))

        val result = await(service.getReasonableExcuseListAndParse())
        result.isDefined shouldBe false
      }

      "the connector call fails" in new Setup {
        when(mockPenaltiesConnector.getListOfReasonableExcuses()(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = await(service.getReasonableExcuseListAndParse())
        result.isDefined shouldBe false
      }
    }
  }

  "submitAppeal" should {
    "for crime" must {
      "parse the session keys into a model and return true when the connector call is successful" in new Setup {
        when(mockPenaltiesConnector.submitAppeal(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))
        val result = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
        result shouldBe true
      }

      "return false" when {
        "the connector returns a non-200 response" in new Setup {
          when(mockPenaltiesConnector.submitAppeal(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(HttpResponse(BAD_GATEWAY, "")))
          val result = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
          result shouldBe false
        }

        "the connector throws an exception" in new Setup {
          when(mockPenaltiesConnector.submitAppeal(Matchers.any())(Matchers.any(), Matchers.any()))
            .thenReturn(Future.failed(new Exception("I failed.")))
          val result = await(service.submitAppeal("crime")(fakeRequestForCrimeJourney, implicitly, implicitly))
          result shouldBe false
        }
      }
    }
  }
}
