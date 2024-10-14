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

package base

import java.time.{LocalDate, LocalDateTime}

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredActionImpl, DataRetrievalActionImpl}
import crypto.CryptoProvider
import helpers.{DateTimeHelper, IsLateAppealHelper}
import models.session.UserAnswers
import models.session.UserAnswers.SensitiveJsObject
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
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.Html
import repositories.UploadJourneyRepository
import services.monitoring.AuditService
import services.{AuthService, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import utils.SessionKeys
import views.html.errors.Unauthorised

import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterAll with UserAnswersBase {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val config: Configuration = appConfig.config

  lazy val injector: Injector = app.injector

  lazy val cryptoProvider: CryptoProvider = injector.instanceOf[CryptoProvider]

  implicit def jsonToSensitiveJson(json: JsObject): SensitiveJsObject = SensitiveJsObject(json)

  val mockDateTimeHelper: DateTimeHelper = mock(classOf[DateTimeHelper])

  val isLateAppealHelper: IsLateAppealHelper = new IsLateAppealHelper(mockDateTimeHelper, appConfig)

  val mainNavigator: Navigation = new Navigation(mockDateTimeHelper, appConfig, isLateAppealHelper)

  val mockSessionService: SessionService = mock(classOf[SessionService])

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("POST", "/").withSession(SessionKeys.journeyId -> "1234")

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  implicit lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  val unauthorised: Unauthorised = injector.instanceOf[Unauthorised]

  val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  val mockAuditService: AuditService = mock(classOf[AuditService])

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  val mockUploadJourneyRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])

  lazy val dataRequiredAction: DataRequiredActionImpl = injector.instanceOf[DataRequiredActionImpl]

  lazy val dataRetrievalAction: DataRetrievalActionImpl = new DataRetrievalActionImpl(mockSessionService, errorHandler)

  val mockAuthService: AuthService = new AuthService(mockAuthConnector)

  val vrn: String = "123456789"

  val arn: Option[String] = Some("AGENT1")

  def fakeRequestConverter(answers: JsObject = correctUserAnswers, fakeRequest: FakeRequest[AnyContent] = fakeRequest): UserRequest[AnyContent] = {
    UserRequest(vrn, answers = userAnswers(answers))(fakeRequest)
  }

  def agentFakeRequestConverter(answers: JsObject = correctUserAnswers): UserRequest[AnyContent] = {
    UserRequest(vrn = vrn, arn = arn, answers = userAnswers(answers))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))
  }

  def userAnswers(answers: JsObject): UserAnswers = UserAnswers("1234", answers)

  val userRequestWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(correctUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestLPPWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(correctLPPUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestAdditionalWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(correctAdditionalLPPUserAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val userRequestAfterVatIsPaidWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(appealAfterVatIsPaidAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val fakeRequestWithCorrectKeysAndReasonableExcuseSet: String => UserRequest[AnyContent] = (reasonableExcuse: String) =>
    UserRequest(vrn, answers = UserAnswers("1234", correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> reasonableExcuse)))(fakeRequest)

  val userRequestAfterVatIsFullyPaidWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(appealAfterVatIsFullyPaidAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  val fakeRequestWithCorrectKeysAndHonestyDeclarationSet: UserRequest[AnyContent] = fakeRequestConverter(Json.obj(
      SessionKeys.penaltyNumber -> "123456789",
      SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
      SessionKeys.startDateOfPeriod -> "2020-01-01",
      SessionKeys.endDateOfPeriod -> "2020-01-01",
      SessionKeys.dueDateOfPeriod -> "2020-02-07",
      SessionKeys.dateCommunicationSent -> "2020-02-08",
      SessionKeys.journeyId -> "1234",
      SessionKeys.hasConfirmedDeclaration -> "true"
    ))

  val userRequestAfterPaymentPlanSetupWithCorrectKeys: UserRequest[AnyContent] =
    UserRequest(vrn, answers = userAnswers(appealAfterPaymentPlanSetupAnswers))(fakeRequest.withSession(SessionKeys.journeyId -> "1234"))

  implicit lazy val authPredicate: AuthPredicate = new AuthPredicate(
    messagesApi,
    mcc,
    mockAuthService,
    errorHandler,
    unauthorised
  )

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())


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
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.doYouWantToAppealBothPenalties -> "yes"
    )))(fakeRequest)

  val vatAgentLPP2UserRequest: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234",
    Json.obj(
      SessionKeys.appealType -> PenaltyTypeEnum.Additional,
      SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
      SessionKeys.doYouWantToAppealBothPenalties -> "yes"
    )))(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123456789"))

  val agentUserAgentSubmitButClientWasLateSessionKeys: UserRequest[AnyContent] =
    UserRequest("123456789", arn = Some("AGENT1"), answers = UserAnswers("1234", Json.obj(
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

  val invalidChars: String = "コし"

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

  val callbackModel: UploadJourney = UploadJourney(
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

  implicit val crypto: Encrypter with Decrypter = cryptoProvider.getCrypto
}
