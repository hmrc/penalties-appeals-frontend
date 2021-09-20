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

package controllers

import base.SpecBase
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsString
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import java.time.LocalDateTime

import connectors.UpscanConnector

import scala.concurrent.Future

class UpscanControllerSpec extends SpecBase {
  val repository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val connector: UpscanConnector = mock(classOf[UpscanConnector])
  val controller: UpscanController = new UpscanController(repository, connector)(appConfig, stubMessagesControllerComponents())

  "UpscanController" should {
    "getStatusOfFileUpload" must {
      "return OK" when {
        "the user has an upload state in the database" in {
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(UploadStatusEnum.WAITING)))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsString(result) shouldBe "\"WAITING\""
        }

        "the user has a successful file upload in the database" in {
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(UploadStatusEnum.READY)))

          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe OK
          contentAsString(result) shouldBe "\"READY\""
        }
      }

      "return NOT FOUND" when {
        "the database does not contain such values specified" in {
          when(repository.getStatusOfFileUpload(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(None))
          val result = controller.getStatusOfFileUpload("1234", "ref1")(fakeRequest)
          status(result) shouldBe NOT_FOUND
        }
      }
    }

    "initiateCallToUpscan" must {
      "return OK" when {
        "the user has a upload state in the database" in {
          val result = controller.initiateCallToUpscan("1234")(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe "asdf"
        }
      }
    }
  }
}
