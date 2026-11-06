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

import java.time.LocalDate

import play.api.libs.json.Json

import play.api.test.Helpers._
import stubs.PayNowStub.{successfulPayNowCall, unsuccessfulPayNowCall}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationSpecCommonBase

import scala.concurrent.ExecutionContext

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