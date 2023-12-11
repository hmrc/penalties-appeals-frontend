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

import base.SpecBase
import connectors.TimeToPayConnector
import connectors.httpParsers.{TimeToPayHttpParser, UnexpectedFailure}
import models.ess.TimeToPayResponseModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class TimeToPayServiceSpec extends SpecBase with LogCapturing {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockTTPConnector: TimeToPayConnector = mock(classOf[TimeToPayConnector])

  val service = new TimeToPayService(mockTTPConnector)

  val timeToPayResponseModel: TimeToPayResponseModel = TimeToPayResponseModel("592d4a09cdc8e04b00021459","http://localhost:1111/test")

  val timeToPayResponseJson: JsObject = Json.obj(
    "journeyId" -> "592d4a09cdc8e04b00021459",
    "nextUrl" -> "http://localhost:1111/test"
  )

  val ttpResponse: HttpResponse = HttpResponse.apply(Status.CREATED, timeToPayResponseJson.toString())

  "retrieveRedirectUrl" should {
    "return a url when the call is successful" in {
      when(mockTTPConnector.setupJourney(any())(any(), any()))
        .thenReturn(Future.successful(TimeToPayHttpParser.TimeToPayResponseReads.read("", "", ttpResponse)))
      val result = service.retrieveRedirectUrl(hc, ec, mockAppConfig)
      await(result) shouldBe Right(timeToPayResponseModel.nextUrl)
    }

    "return an error when the call is unsuccessful" in {
      when(mockTTPConnector.setupJourney(any())(any(), any()))
        .thenReturn(Future.successful(Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "something went wrong"))))
      val result = service.retrieveRedirectUrl(hc, ec, mockAppConfig)
      await(result) shouldBe Left(UnexpectedFailure(Status.INTERNAL_SERVER_ERROR, "something went wrong"))
    }
  }
}
