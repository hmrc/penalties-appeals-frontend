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

package services.monitoring

import base.SpecBase
import config.AppConfig
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import play.api.mvc.AnyContentAsEmpty

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends SpecBase with BeforeAndAfterEach with Matchers {


  val mockConfiguration: AppConfig = mock(classOf[AppConfig])
  val mockAuditConnector: AuditConnector = mock(classOf[AuditConnector])
  val testAppName = "penalties-frontend"
  val testUrl = "testUrl"

  val testAuditService = new AuditService(mockConfiguration, mockAuditConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "testUrl")

  val testJsonAuditModel: JsonAuditModel = new JsonAuditModel{
    override val auditType = "testJsonAuditType"
    override val transactionName = "testJsonTransactionName"
    override val detail: JsObject = Json.obj("foo" -> "bar")
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector, mockConfiguration)
    when(mockConfiguration.appName) thenReturn testAppName
  }

  "audit" when {
    "given a json auditable data type" should {
      "extract the data and pass it into the AuditConnector" in {
        val expectedData = testAuditService.toExtendedDataEvent(testJsonAuditModel, "testUrl")

        testAuditService.audit(testJsonAuditModel)

        verify(mockAuditConnector)
          .sendExtendedEvent(ArgumentMatchers.refEq(expectedData, "eventId", "generatedAt"))(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )
      }
    }
  }

  "getAllDuplicateUploadsForAppealSubmission" should {
    val sampleDate: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
    "return all the duplicates in a JSON array" in {
      val uploadAsReady: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("/url"),
        uploadDetails = Some(
          UploadDetails(
            fileName = "file1.txt",
            fileMimeType = "text/plain",
            uploadTimestamp = sampleDate,
            checksum = "123456789",
            size = 100
          )
        ),
        failureDetails = None,
        lastUpdated = LocalDateTime.now()
      )
      val uploadAsDuplicate: UploadJourney = uploadAsReady.copy(reference = "ref2", fileStatus = UploadStatusEnum.DUPLICATE)
      val uploadsWithDuplicates = Seq(uploadAsReady, uploadAsDuplicate)
      val result = testAuditService.getAllDuplicateUploadsForAppealSubmission(uploadsWithDuplicates)
      result shouldBe Json.arr(
        Json.obj(
          "upscanReference" -> "ref1",
          "uploadTimestamp" -> sampleDate,
          "name" -> "file1.txt",
          "mimeType" -> "text/plain",
          "size" -> 100,
          "checksum" -> "123456789"
        ),
        Json.obj(
          "upscanReference" -> "ref2",
          "uploadTimestamp" -> sampleDate,
          "name" -> "file1.txt",
          "mimeType" -> "text/plain",
          "size" -> 100,
          "checksum" -> "123456789"
        )
      )
    }
  }
}
