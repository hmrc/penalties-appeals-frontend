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

package base

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredActionImpl}
import helpers.DateTimeHelper
import models.UserRequest
import navigation.Navigation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.AuthService
import uk.gov.hmrc.auth.core.AuthConnector
import utils.SessionKeys
import views.html.errors.Unauthorised

import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val injector = app.injector

  val mockDateTimeHelper: DateTimeHelper = mock(classOf[DateTimeHelper])

  val mainNavigator: Navigation = new Navigation(mockDateTimeHelper, appConfig)

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  lazy val dataRequiredAction = injector.instanceOf[DataRequiredActionImpl]

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val vrn: String = "123456789"

  def fakeRequestConverter(fakeRequest: FakeRequest[AnyContent] = fakeRequestWithCorrectKeys): UserRequest[AnyContent] = {
    UserRequest(vrn)(fakeRequest)
  }

  val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = fakeRequest
    .withSession((SessionKeys.penaltyId, "123"), (SessionKeys.appealType, "Late_Submission"), (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500"),
      (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500"), (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00.500"), (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00.500"))

  val userRequestWithCorrectKeys: UserRequest[AnyContent] = UserRequest(vrn)(fakeRequestWithCorrectKeys)

  val fakeRequestWithCorrectKeysAndReasonableExcuseSet = (reasonableExcuse: String) => UserRequest(vrn)(fakeRequest
    .withSession((SessionKeys.penaltyId, "123"), (SessionKeys.appealType, "Late_Submission"), (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500"),
      (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500"), (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00.500"), (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00.500"),
      (SessionKeys.reasonableExcuse, reasonableExcuse)))

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())
}
