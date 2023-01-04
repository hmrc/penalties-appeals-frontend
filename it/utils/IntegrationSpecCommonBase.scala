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

package utils

import com.codahale.metrics.SharedMetricRegistries
import helpers.WiremockHelper
import models.session.UserAnswers
import org.mongodb.scala.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import repositories.UserAnswersRepository
import stubs.{AuditStub, AuthStub}
import uk.gov.hmrc.http.SessionKeys.authToken

trait IntegrationSpecCommonBase extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with
  BeforeAndAfterAll with BeforeAndAfterEach with TestSuite with WiremockHelper {

  lazy val injector: Injector = app.injector

  val fakeRequest: FakeRequest[AnyContent] = FakeRequest("POST", "/").withSession(
    authToken -> "1234",
    SessionKeys.journeyId -> "1234"
  )

  def userAnswers(answers: JsObject): UserAnswers = UserAnswers("1234", answers)

  override def afterEach(): Unit = {
    resetAll()
    super.afterEach()
    SharedMetricRegistries.clear()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    AuthStub.authorised()
    AuditStub.audit()
    AuditStub.auditMerge()
    SharedMetricRegistries.clear()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    start()
    SharedMetricRegistries.clear()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    resetAll()
    stop()
    SharedMetricRegistries.clear()
  }

  val configForApp: Map[String, Any] = Map(
    "microservice.services.auth.port" -> stubPort,
    "microservice.services.penalties.port" -> stubPort,
    "microservice.services.upscan-initiate.port" -> stubPort,
    "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "auditing.enabled" -> true,
    "auditing.consumer.baseUri.port" -> stubPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(configForApp)
    .build()

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  def buildClientForRequestToApp(baseUrl: String = "/penalties-appeals", uri: String): WSRequest = {
    ws.url(s"http://localhost:$port$baseUrl$uri").withFollowRedirects(false)
  }

  implicit val config: Configuration = injector.instanceOf[Configuration]

  lazy val userAnswersRepository: UserAnswersRepository = injector.instanceOf[UserAnswersRepository]

  class UserAnswersSetup(sessionDataToStore: UserAnswers = UserAnswers("1234", Json.obj())) {
    await(userAnswersRepository.collection.deleteMany(Document()).toFuture())
    await(userAnswersRepository.upsertUserAnswer(sessionDataToStore))
  }
}