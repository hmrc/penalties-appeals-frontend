/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.predicates.{AuthPredicate, DataRequiredActionImpl, DataRetrievalActionImpl}
import helpers.DateTimeHelper
import models.session.UserAnswers
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{PenaltyTypeEnum, UserRequest}
import navigation.Navigation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.mock
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, Cookie, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import services.{AuthService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import utils.SessionKeys
import views.html.errors.Unauthorised

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterAll {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val config: Configuration = appConfig.config

  lazy val injector: Injector = app.injector

  val mockDateTimeHelper: DateTimeHelper = mock(classOf[DateTimeHelper])

  val mainNavigator: Navigation = new Navigation(mockDateTimeHelper, appConfig)

  val mockSessionService: SessionService = mock(classOf[SessionService])

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("POST", "/").withSession(SessionKeys.journeyId -> "1234")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  val cyMessages: Messages = messagesApi.preferred(fakeRequest.withTransientLang("cy"))

  val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val sessionService: SessionService = injector.instanceOf[SessionService]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuditService: AuditService = mock(classOf[AuditService])

  val mockUploadJourneyRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])

  lazy val dataRequiredAction: DataRequiredActionImpl = injector.instanceOf[DataRequiredActionImpl]

  lazy val dataRetrievalAction: DataRetrievalActionImpl = new DataRetrievalActionImpl(mockSessionService)

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val vrn: String = "123456789"

  val arn: Option[String] = Some("AGENT1")

  def fakeRequestConverter(answers: JsObject = correctUserAnswers, fakeRequest: FakeRequest[AnyContent] = fakeRequest): UserRequest[AnyContent] = {
    UserRequest(vrn, answers = userAnswers(answers))(fakeRequest)
  }

  def agentFakeRequestConverter(answers: JsObject = correctUserAnswers): UserRequest[AnyContent] = {
    UserRequest(vrn = vrn, arn = arn, answers = userAnswers(answers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))
  }

  val correctUserAnswers: JsObject = Json.obj(
    SessionKeys.penaltyNumber -> "123",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
    SessionKeys.journeyId -> "1234"
  )

  def userAnswers(answers: JsObject): UserAnswers = UserAnswers("1234", answers)

  val correctLPPUserAnswers: JsObject = Json.obj(
    SessionKeys.penaltyNumber -> "123",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
    SessionKeys.journeyId -> "1234"
  )

  val correctAdditionalLPPUserAnswers: JsObject = Json.obj(
    SessionKeys.penaltyNumber -> "123",
    SessionKeys.appealType -> PenaltyTypeEnum.Additional,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
    SessionKeys.journeyId -> "1234"
  )

  val fakeRequestWithCorrectKeysAndJS: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.journeyId -> "1234").withCookies(Cookie("jsenabled", "true"))

  val userRequestWithCorrectKeys: UserRequest[AnyContent] = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestLPPWithCorrectKeys: UserRequest[AnyContent] = UserRequest(vrn, answers = userAnswers(correctLPPUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestAdditionalWithCorrectKeys: UserRequest[AnyContent] = UserRequest(vrn, answers = userAnswers(correctAdditionalLPPUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestWithCorrectKeysAndJS: UserRequest[AnyContent] = UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequestWithCorrectKeysAndJS)

  val fakeRequestWithCorrectKeysAndReasonableExcuseSet: String => UserRequest[AnyContent] = (reasonableExcuse: String) =>
    UserRequest(vrn, answers = UserAnswers("1234", correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> reasonableExcuse)))(fakeRequest)

  val fakeRequestWithCorrectKeysAndHonestyDeclarationSet: UserRequest[AnyContent] = fakeRequestConverter(Json.obj(
      SessionKeys.penaltyNumber -> "123",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> "2020-01-01",
      SessionKeys.endDateOfPeriod -> "2020-01-01",
      SessionKeys.dueDateOfPeriod -> "2020-02-07",
      SessionKeys.dateCommunicationSent -> "2020-02-08",
      SessionKeys.journeyId -> "1234",
      SessionKeys.hasConfirmedDeclaration -> "true"
    ))

  lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  val agentAnswers: JsObject = Json.obj(
    SessionKeys.agentSessionVrn -> "VRN1234",
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
  )

  val agentRequest: FakeRequest[AnyContent] = fakeRequest.withSession(SessionKeys.agentSessionVrn -> "VRN1234")

  val vatTraderLSPUserRequest: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234",
    Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    )))(fakeRequest)

  val vatTraderLPPUserRequest: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234",
    Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    )))(fakeRequest)

  val vatTraderLPP2UserRequest: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234",
    Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Additional,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01")
    )))(fakeRequest)

  val agentUserAgentSubmitButClientWasLateSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
    SessionKeys.whatCausedYouToMissTheDeadline -> "client"
  ) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserAgentClientPlannedToSubmitSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"),
    answers = UserAnswers("1234", Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.whoPlannedToSubmitVATReturn -> "client"
    ) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserAgentMissedSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
    SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
    SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
  ) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserLPP: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment
  ) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserLSP: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission
  ) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserLPPAdditional: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Additional) ++ agentAnswers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val vatTraderUserLSP: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234", correctUserAnswers ++ Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission)))(fakeRequest)

  val vatTraderUserLPP: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234", correctUserAnswers ++ Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment)))(fakeRequest)

  val vatTraderUserAdditional: UserRequest[AnyContent] = UserRequest("123456789", answers = UserAnswers("1234", correctUserAnswers ++ Json.obj(
    SessionKeys.appealType -> PenaltyTypeEnum.Additional)))(fakeRequest)

  val moreThanFiveThousandChars: String = 'a'.toString * 5010

  def uploadListRow(fileNumber: Int, fileName: String, fileReference: String): Html = {
    Html(
      s"""
         |<div class="govuk-summary-list__row" id="document-row-${fileNumber + 1}">
         |   <dt class="govuk-summary-list__key">
         |    File ${fileNumber + 1}
         |   </dt>
         |   <dd class="govuk-summary-list__value">
         |    $fileName
         |   </dd>
         |   <dd class="govuk-summary-list__actions">
         |     <a href="/penalties-appeals/remove-file/$fileReference?isJsEnabled=false" class="govuk-link" id="remove-document-${fileNumber + 1}">
         |    Remove <span class="govuk-visually-hidden"> file ${fileNumber + 1}</span></a>
         |   </dd>
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
