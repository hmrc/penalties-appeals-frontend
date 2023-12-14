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

package services

import java.time.{LocalDate, LocalDateTime}

import com.github.tomakehurst.wiremock.client.WireMock._
import models.session.UserAnswers
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{PenaltyTypeEnum, UserRequest}
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.PayNowStub.{successfulPayNowCall, unsuccessfulPayNowCall}
import stubs.PenaltiesStub._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.Logger.logger
import utils.{IntegrationSpecCommonBase, SessionKeys}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._


class PayNowServiceISpec extends IntegrationSpecCommonBase {
  val service = injector.instanceOf[PayNowService]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val vrn = "9999"
  val chargeReference= "1234567890"
  val vatAmount = 6666
  val dueDate =   LocalDate.of(2020, 1, 31)


  "retrieveRedirectUrl" should {
    "return Right when call is successful" in {
      val responseBody: String = Json.obj(
        "journeyId" -> "1234",
        "nextUrl" -> "http://url/next-url"
      ).toString()
      successfulPayNowCall(
        responseBody
      )
      val result = service.retrieveRedirectUrl(vrn, chargeReference, vatAmount, dueDate)( hc, ec)
      await(result).isRight shouldBe true
    }

    "return Left when call is unsuccessful" in {
      unsuccessfulPayNowCall
      val result = service.retrieveRedirectUrl(vrn, chargeReference, vatAmount, dueDate)(hc, ec)
      await(result).isLeft shouldBe true
    }
  }
}