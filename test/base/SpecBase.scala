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
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{PenaltyTypeEnum, UserRequest}
import navigation.Navigation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContent, Cookie, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import repositories.UploadJourneyRepository
import services.AuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.AuthConnector
import utils.SessionKeys
import views.html.errors.Unauthorised

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val injector: Injector = app.injector

  val mockDateTimeHelper: DateTimeHelper = mock(classOf[DateTimeHelper])

  val mainNavigator: Navigation = new Navigation(mockDateTimeHelper, appConfig)

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuditService: AuditService = mock(classOf[AuditService])

  val mockUploadJourneyRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])

  lazy val dataRequiredAction: DataRequiredActionImpl = injector.instanceOf[DataRequiredActionImpl]

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val vrn: String = "123456789"

  val arn: Option[String] = Some("AGENT1")

  def fakeRequestConverter(fakeRequest: FakeRequest[AnyContent] = fakeRequestWithCorrectKeys): UserRequest[AnyContent] = {
    UserRequest(vrn)(fakeRequest)
  }

  def agentFakeRequestConverter(fakeRequest: FakeRequest[AnyContent] = fakeRequestWithCorrectKeys): UserRequest[AnyContent] = {
    UserRequest(vrn = vrn, arn = arn)(fakeRequest)
  }

  val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = fakeRequest
    .withSession((SessionKeys.penaltyId, "123"), (SessionKeys.appealType, "Late_Submission"),
      (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500"), (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500"),
      (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00.500"), (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00.500"),
      (SessionKeys.journeyId, "1234"))

  val fakeRequestWithCorrectKeysAndJS: FakeRequest[AnyContent] = fakeRequestWithCorrectKeys.withCookies(Cookie("jsenabled", "true"))

  val userRequestWithCorrectKeys: UserRequest[AnyContent] = UserRequest(vrn)(fakeRequestWithCorrectKeys)

  val userRequestWithCorrectKeysAndJS: UserRequest[AnyContent] = UserRequest(vrn)(fakeRequestWithCorrectKeysAndJS)

  val fakeRequestWithCorrectKeysAndReasonableExcuseSet: String => UserRequest[AnyContent] = (reasonableExcuse: String) => UserRequest(vrn)(fakeRequest
    .withSession((SessionKeys.penaltyId, "123"), (SessionKeys.appealType, "Late_Submission"), (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500"),
      (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500"), (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00.500"),
      (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00.500"), (SessionKeys.reasonableExcuse, reasonableExcuse), (SessionKeys.journeyId, "1234")))

  val fakeRequestWithCorrectKeysAndHonestyDeclarationSet: FakeRequest[AnyContent] = fakeRequest
    .withSession((SessionKeys.penaltyId, "123"), (SessionKeys.appealType, "Late_Submission"), (SessionKeys.startDateOfPeriod,
      "2020-01-01T12:00:00.500"), (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500"), (SessionKeys.dueDateOfPeriod,
      "2020-02-07T12:00:00.500"), (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00.500"), (SessionKeys.journeyId, "1234"),
      (SessionKeys.hasConfirmedDeclaration, "true"))

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  val agentRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")

  val vatTraderUser: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequest)

  val agentUserSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(agentRequest.withSession(
    SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
    SessionKeys.causeOfLateSubmissionAgent -> "client")
  )

  val agentUserLPP: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(agentRequest.withSession(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString
  ))

  val agentUserLSP: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(agentRequest.withSession(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString
  ))

  val agentUserLPPAdditional: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"))(agentRequest.withSession(
    SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString
  ))

  val vatTraderUserLSP: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequest
    .withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString))

  val vatTraderUserLPP: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequest
    .withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))

  val vatTraderUserAdditional: UserRequest[AnyContent] = UserRequest("123456789")(fakeRequest
    .withSession(SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString))

  val moreThanFiveThousandChars: String = 'a'.toString * 5010

  def uploadListRow(fileNumber: Int, fileName: String, fileReference: String): Html = {
    Html(
      s"""
        | <div class="govuk-summary-list__row" id="document-row-${fileNumber + 1}">
        |   <dt class="govuk-summary-list__key">
        |    Document ${fileNumber + 1}
        |   </dt>
        |   <dd class="govuk-summary-list__value">
        |    $fileName
        |   </dd>
        |   <dd class="govuk-summary-list__actions">
        |   <form action="/penalties-appeals/remove-file-upload" method="POST" novalidate>
        |     <input type="hidden" name="fileReference" value="$fileReference">
        |     <button type="submit" class="govuk-link remove-link govuk-body" id="remove-button-${fileNumber + 1}">
        |       Remove
        |       <span class="govuk-visually-hidden">Document ${fileNumber + 1}</span>
        |     </button>
        |   </form>
        |</dd>
        |</div>
        |""".stripMargin
    )
  }

  def insetText(message: String): Html = {
    Html(
      s"""
         |<div class="govuk-inset-text">
         |  $message
         |</div>
         |""".stripMargin
    )
  }

  val callBackModel: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    ))
  )
}
