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
import models.User
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AppealServiceSpec extends SpecBase {
  val mockPenaltiesConnector: PenaltiesConnector = mock[PenaltiesConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val appealDataAsJson: JsValue = Json.parse(
    """
      |{
      | "type": "LATE_SUBMISSION",
      | "startDate": "2020-01-01T12:00:00",
      | "endDate": "2020-01-01T13:00:00"
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

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new User[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return None when the connectors returns Json that cannot be parsed to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(Json.parse("{}"))))

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new User[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe false
    }

    "return Some when the connector returns Json that is parseable to a model" in new Setup {
      when(mockPenaltiesConnector.getAppealsDataForPenalty(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(appealDataAsJson)))

      val result = service.validatePenaltyIdForEnrolmentKey("1234")(new User[AnyContent]("123456789")(fakeRequest), implicitly, implicitly)
      await(result).isDefined shouldBe true
    }
  }
}
