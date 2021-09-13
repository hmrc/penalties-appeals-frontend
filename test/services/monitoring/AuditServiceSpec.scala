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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import play.api.mvc.AnyContentAsEmpty

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

}
