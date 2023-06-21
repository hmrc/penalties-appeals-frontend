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

package helpers

import base.SpecBase
import models.appeals.CheckYourAnswersRow
import models.pages._
import models.session.UserAnswers
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import utils.SessionKeys

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionAnswersHelperSpec extends SpecBase {
  val mockRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val mockConfig: Configuration = mock(classOf[Configuration])
  val sessionAnswersHelper = new SessionAnswersHelper(mockRepository, mockAppConfig, mockDateTimeHelper)

  def answers(sessionKeys: JsObject): UserAnswers = UserAnswers("1234", sessionKeys)

  when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 1, 1))
  when(mockAppConfig.daysRequiredForLateAppeal).thenReturn(30)
  "isAllAnswerPresentForReasonableExcuse" should {
    "for crime" must {
      "return true - when all keys present" in {
        val userRequest = UserRequest("123456789", answers = answers(crimeAnswers))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(userRequest)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val sessionKeys = Json.obj(
          SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfCrime -> LocalDate.parse("2022-01-01")
        )
        val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(userRequest)
        result shouldBe false
      }
    }

    "for loss of staff" must {
      "return true - when all keys present" in {
        val userRequest = UserRequest("123456789", answers = answers(lossOfStaffAnswers))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(userRequest)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val sessionKeys = Json.obj(
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> "true"
        )
        val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(userRequest)
        result shouldBe false
      }
    }

    "for fire or flood" must {
      "return true - when all keys present" in {
        val userRequest = UserRequest("123456789", answers = answers(fireOrFloodAnswers))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(userRequest)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val sessionKeys = Json.obj(
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.dateOfFireOrFlood -> LocalDate.parse("2022-01-01")
        )
        val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(userRequest)
        result shouldBe false
      }
    }

    "for technical issues" must {
      "return true - when all keys present" in {
        val userRequest = UserRequest("123456789", answers = answers(techIssuesAnswers))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(userRequest)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val sessionKeys = Json.obj(
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidTechnologyIssuesBegin -> LocalDate.parse("2022-01-01"),
          SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
        )
        val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(userRequest)
        result shouldBe false
      }
    }

    "for health" must {
      "return true" when {
        "the keys are present for no hospital stay journey" in {
          val userRequest = UserRequest("123456789", answers = answers(noHospitalStayAnswers))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe true
        }

        "the keys are present for ongoing hospital stay journey" in {
          val userRequest = UserRequest("123456789", answers = answers(hospitalOngoingAnswers))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe true
        }

        "the keys are present for an ended hospital stay" in {
          val userRequest = UserRequest("123456789", answers = answers(hospitalEndedAnswers))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe true
        }
      }

      "return false" when {
        "there was a hospital stay but there event ongoing question hasn't been answered" in {
          val sessionKeys = Json.obj(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
          )
          val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe false
        }

        "the hospital stay question hasn't been answered" in {
          val sessionKeys = Json.obj(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.hasHealthEventEnded -> "yes",
            SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01")
          )
          val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe false
        }

        "there is an ongoing hospital stay but no startDate has been provided" in {
          val sessionKeys = Json.obj(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.hasHealthEventEnded -> "no",
            SessionKeys.wasHospitalStayRequired -> "yes"
          )
          val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe false
        }

        "there is a hospital stay that has ended but no end date has been provided" in {
          val sessionKeys = Json.obj(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.hasHealthEventEnded -> "yes"
          )
          val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe false
        }

        "not all keys are present" in {
          val sessionKeys = Json.obj(
            SessionKeys.hasConfirmedDeclaration -> true,
            SessionKeys.whenHealthIssueStarted -> LocalDate.parse("2022-01-01"),
            SessionKeys.whenDidTechnologyIssuesEnd -> LocalDate.parse("2022-01-02")
          )
          val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(userRequest)
          result shouldBe false
        }
      }
    }

    "for other" must {
      "return true - when all keys present" in {
        val userRequest = UserRequest("123456789", answers = answers(otherAnswers))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(userRequest)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val sessionKeys = Json.obj(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2022-01-01")
        )
        val userRequest = UserRequest("123456789", answers = answers(sessionKeys))(FakeRequest())
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(userRequest)
        result shouldBe false
      }
    }
  }

  def fakeRequestWithKeys(answers: JsObject, lateReason: Boolean, whoPlanned: Option[String] = None,
                               whatCaused: Option[String] = None): UserRequest[AnyContent] = {
    if (whoPlanned.isDefined) {
      agentFakeRequestConverter(
        answers
          ++ Json.obj(SessionKeys.whoPlannedToSubmitVATReturn -> whoPlanned)
          ++ {if (whatCaused.isDefined) Json.obj(SessionKeys.whatCausedYouToMissTheDeadline -> whatCaused.get) else Json.obj()}
          ++ {if (lateReason) Json.obj(SessionKeys.lateAppealReason -> "Lorem ipsum") else Json.obj()}
      )
    } else {
      fakeRequestConverter(
        answers ++ {if (lateReason) Json.obj(SessionKeys.lateAppealReason -> "Lorem ipsum") else Json.obj()}
      )
    }
  }

  def determineFunction(function: String, reasonableExcuse: Option[String], answers: JsObject,
                        whoPlanned: Option[String] = None, whatCaused: Option[String] = None,
                        lateAppeal: Boolean = false, fileNames: Option[String] = None): Seq[CheckYourAnswersRow] = {
    val fakeRequest = fakeRequestWithKeys(answers, lateAppeal, whoPlanned, whatCaused)
    function match {
      case "generic" =>
        sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            reasonableExcuse.get, fileNames)(fakeRequest, implicitly)
      case "health" =>
        sessionAnswersHelper.getHealthReasonAnswers()(fakeRequest, implicitly)
      case "agent" =>
        sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequest, implicitly)
      case "all" =>
        sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(fileNames)(fakeRequest, implicitly)
      case "obligation" =>
        sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage(fileNames)(fakeRequest, implicitly)
    }
  }

  "getContentForReasonableExcuseCheckYourAnswersPage" should {

    def checkYourAnswers(reasonableExcuse: String, answers: JsObject, whoPlanned: Option[String] = None,
                         whatCaused: Option[String] = None, lateAppeal: Boolean = false,
                         fileNames: Option[String] = None): Seq[CheckYourAnswersRow] =
      determineFunction("generic", Some(reasonableExcuse), answers, whoPlanned, whatCaused, lateAppeal, fileNames)

    "for crime" must {
      "return all the keys from the session ready to be passed to the view" in {
        val result = checkYourAnswers("crime", crimeAnswers)

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "Crime"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the crime happen?"
        result(1).value shouldBe "1\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
          WhenDidCrimeHappenPage.toString
        ).url
        result(2).key shouldBe "Has this crime been reported to the police?"
        result(2).value shouldBe "Yes"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
          HasCrimeBeenReportedPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val result = checkYourAnswers("crime", crimeAnswers, lateAppeal = true)

        result(3).key shouldBe "Reason for appealing after 30 days"
        result(3).value shouldBe "Lorem ipsum"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for fire or flood" must {
      "return all the keys from the session ready to be passed to the view" in {
        val result = checkYourAnswers("fireOrFlood", fireOrFloodAnswers)

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "Fire or flood"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the fire or flood happen?"
        result(1).value shouldBe "1\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
          WhenDidFireOrFloodHappenPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val result = checkYourAnswers("fireOrFlood", fireOrFloodAnswers, lateAppeal = true)

        result(2).key shouldBe "Reason for appealing after 30 days"
        result(2).value shouldBe "Lorem ipsum"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for loss of staff" must {
      "return all the keys from the session ready to be passed to the view" in {
        val result = checkYourAnswers("lossOfStaff", lossOfStaffAnswers)

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "Loss of staff essential to the VAT process"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the person leave the business?"
        result(1).value shouldBe "1\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
          WhenDidPersonLeaveTheBusinessPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val result =checkYourAnswers("lossOfStaff", lossOfStaffAnswers, lateAppeal = true)

        result(2).key shouldBe "Reason for appealing after 30 days"
        result(2).value shouldBe "Lorem ipsum"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for technical issues" must {
      "return all the keys from the session ready to be passed to the view" in {
        val result = checkYourAnswers("technicalIssues", techIssuesAnswers)

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "Technology issues"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the technology issues begin?"
        result(1).value shouldBe "1\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
          WhenDidTechnologyIssuesBeginPage.toString
        ).url
        result(2).key shouldBe "When did the technology issues end?"
        result(2).value shouldBe "2\u00A0January\u00A02022"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
          WhenDidTechnologyIssuesEndPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val result = checkYourAnswers("technicalIssues", techIssuesAnswers, lateAppeal = true)

        result(3).key shouldBe "Reason for appealing after 30 days"
        result(3).value shouldBe "Lorem ipsum"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for LPP on other journey" must {
      "display the correct wording for trader" in {
        val result = checkYourAnswers("other", otherAnswers ++
          Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment, SessionKeys.isUploadEvidence -> "no"), lateAppeal = true)

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "The reason does not fit into any of the other categories"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the issue first stop you paying the VAT bill?"
        result(1).value shouldBe "2\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
          WhenDidBecomeUnablePage.toString
        ).url
        result(2).key shouldBe "Why was the VAT bill paid late?"
        result(2).value shouldBe "This is a reason."
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
          WhyWasReturnSubmittedLatePage.toString
        ).url
        result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(3).value shouldBe "No"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4).key shouldBe "Reason for appealing after 30 days"
        result(4).value shouldBe "Lorem ipsum"
        result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "when an agent is on the page" should {

      "for health" must {
        "for no hospital stay" should {
          "when the agent intended to submit the VAT return but the client could not" in {
            val result = checkYourAnswers("health",  noHospitalStayAnswers, Some("agent"), Some("client"))

            result.head.key shouldBe "Reason for missing the VAT deadline"
            result.head.value shouldBe "Health"
            result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage.toString
            ).url
            result(1).key shouldBe "Did this health issue include a hospital stay?"
            result(1).value shouldBe "No"
            result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage.toString
            ).url
            result(2).key shouldBe "When did the health issue first stop your client getting information to you?"
            result(2).value shouldBe "2\u00A0January\u00A02022"
            result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage.toString
            ).url
          }

          "when the client intended to submit the VAT return" in {
            val result = checkYourAnswers("health", noHospitalStayAnswers, Some("client"), None)

            result(2).key shouldBe "When did the health issue first stop your client submitting the VAT Return?"
            result(2).value shouldBe "2\u00A0January\u00A02022"
            result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage.toString
            ).url
          }
          "when the agent intended to submit but missed the deadline" in {
            val result = checkYourAnswers("health", noHospitalStayAnswers, Some("agent"), Some("agent"))

            result(2).key shouldBe "When did the health issue first stop you submitting the VAT Return?"
            result(2).value shouldBe "2\u00A0January\u00A02022"
            result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage.toString
            ).url
          }

          "return all keys and the 'Reason for appealing after 30 days' text" in {
            val result = checkYourAnswers("health",  noHospitalStayAnswers, Some("agent"), Some("client"), lateAppeal = true)

            result(3).key shouldBe "Reason for appealing after 30 days"
            result(3).value shouldBe "Lorem ipsum"
            result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.MakingALateAppealController.onPageLoad().url,
              MakingALateAppealPage.toString
            ).url
          }

          "when it is an LPP show the correct message" in {
            val result = checkYourAnswers("health",
              noHospitalStayAnswers ++ Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment), Some("agent"), Some("client"))

            result(2).key shouldBe "When did the health issue first stop your client paying the VAT bill?"
            result(2).value shouldBe "2\u00A0January\u00A02022"
            result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage.toString
            ).url
          }
        }
      }

      "for other" must {
        "display the correct wording for agent" when {
          "the client planned to submit" in {
            val result = checkYourAnswers("other", otherAnswers, Some("client"), None)
            result(1).key shouldBe "When did the issue first stop your client submitting the VAT Return?"
            result(1).value shouldBe "2\u00A0January\u00A02022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }

          "the agent planned to submit and client missed deadline" in {
            val result = checkYourAnswers("other", otherAnswers, Some("agent"), Some("client"))
            result(1).key shouldBe "When did the issue first stop your client getting information to you?"
            result(1).value shouldBe "2\u00A0January\u00A02022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }

          "the agent planned to submit and missed deadline" in {
            val result = checkYourAnswers("other",  otherAnswers, Some("agent"), Some("agent"))
            result(1).key shouldBe "When did the issue first stop you submitting the VAT Return?"
            result(1).value shouldBe "2\u00A0January\u00A02022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }
        }

        "for no upload" in {
          val result = checkYourAnswers("other", otherAnswers, Some("agent"), Some("client"))
          result.head.key shouldBe "Reason for missing the VAT deadline"
          result.head.value shouldBe "The reason does not fit into any of the other categories"
          result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1).key shouldBe "When did the issue first stop your client getting information to you?"
          result(1).value shouldBe "2\u00A0January\u00A02022"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2).key shouldBe "Why was the return submitted late?"
          result(2).value shouldBe "This is a reason."
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(3).value shouldBe "Yes"
          result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4).key shouldBe "Evidence to support this appeal"
          result(4).value shouldBe "Not provided"
          result(4).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
        }

        "for no upload - and late appeal" in {
          val result = checkYourAnswers("other",
            otherAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"), Some("agent"), Some("client"), lateAppeal = true)
          result(4).key shouldBe "Reason for appealing after 30 days"
          result(4).value shouldBe "Lorem ipsum"
          result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "for upload" in {
          val result = checkYourAnswers("other", otherAnswers,Some("agent"), Some("client"), fileNames = Some("file.docx"))
          result(4).key shouldBe "Evidence to support this appeal"
          result(4).value shouldBe "file.docx"
          result(4).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
        }

        "for upload - and late appeal" in {
          val result = checkYourAnswers("other", otherAnswers,Some("agent"), Some("client"), fileNames = Some("file.docx"), lateAppeal = true)
          result(5).key shouldBe "Reason for appealing after 30 days"
          result(5).value shouldBe "Lorem ipsum"
          result(5).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }
      }

      "for LPP - appeal both penalties available - agent selects no" in {
        val result = checkYourAnswers("other", otherAnswers ++
          Json.obj(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
            SessionKeys.doYouWantToAppealBothPenalties -> "no"),
          Some("agent"), Some("client"), fileNames = Some("file.docx"))
        result.head.key shouldBe "Do you intend to appeal both penalties for the same reason?"
        result.head.value shouldBe "No"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url,
          PenaltySelectionPage.toString
        ).url
        result(1).key shouldBe "Reason for missing the VAT deadline"
        result(1).value shouldBe "The reason does not fit into any of the other categories"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(2).key shouldBe "When did the issue first stop your client paying the VAT bill?"
        result(2).value shouldBe "2\u00A0January\u00A02022"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
          WhenDidBecomeUnablePage.toString
        ).url
        result(3).key shouldBe "Why was the VAT bill paid late?"
        result(3).value shouldBe "This is a reason."
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
          WhyWasReturnSubmittedLatePage.toString
        ).url
      }

      "for LPP - appeal both penalties available - agent selects yes" in {
        val result = checkYourAnswers("other", otherAnswers ++
          Json.obj(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
            SessionKeys.doYouWantToAppealBothPenalties -> "yes"),
          Some("agent"), Some("client"), fileNames = Some("file.docx"))
        result.head.key shouldBe "Do you intend to appeal both penalties for the same reason?"
        result.head.value shouldBe "Yes"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url,
          PenaltySelectionPage.toString
        ).url
      }

      "display no upload details row if the user has uploaded files but has changed their mind and selects 'no' - 'hide' the files uploaded" in {
        val result = checkYourAnswers("other", otherAnswers ++
          Json.obj(
            SessionKeys.isUploadEvidence -> "no"),
          Some("agent"), Some("client"), fileNames = Some("file.docx"), lateAppeal = true)
        result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(3).value shouldBe "No"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4).key shouldBe "Reason for appealing after 30 days"
        result(4).value shouldBe "Lorem ipsum"
        result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "when a VAT trader is on the page" should {

      "for health" must {
        "for no hospital stay" should {
          "return all the keys from the session ready to be passed to the view" in {
            val result = checkYourAnswers("health", noHospitalStayAnswers)
            result.head.key shouldBe "Reason for missing the VAT deadline"
            result.head.value shouldBe "Health"
            result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.ReasonableExcuseController.onPageLoad().url,
              ReasonableExcuseSelectionPage.toString
            ).url
            result(1).key shouldBe "Did this health issue include a hospital stay?"
            result(1).value shouldBe "No"
            result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
              WasHospitalStayRequiredPage.toString
            ).url
            result(2).key shouldBe "When did the health issue first stop you submitting the VAT Return?"
            result(2).value shouldBe "2\u00A0January\u00A02022"
            result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
              WhenDidHealthIssueHappenPage.toString
            ).url
          }

          "return all keys and the 'Reason for appealing after 30 days' text" in {
            val result = checkYourAnswers("health", noHospitalStayAnswers, lateAppeal = true)
            result(3).key shouldBe "Reason for appealing after 30 days"
            result(3).value shouldBe "Lorem ipsum"
            result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.MakingALateAppealController.onPageLoad().url,
              MakingALateAppealPage.toString
            ).url
          }
        }
      }

      "for other" must {
        "for no upload" in {
          val result = checkYourAnswers("other", otherAnswers)
          result.head.key shouldBe "Reason for missing the VAT deadline"
          result.head.value shouldBe "The reason does not fit into any of the other categories"
          result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1).key shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1).value shouldBe "2\u00A0January\u00A02022"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2).key shouldBe "Why was the return submitted late?"
          result(2).value shouldBe "This is a reason."
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(3).value shouldBe "Yes"
          result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4).key shouldBe "Evidence to support this appeal"
          result(4).value shouldBe "Not provided"
          result(4).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
        }

        "for no upload - and late appeal" in {
          val result = checkYourAnswers("other", otherAnswers, lateAppeal = true)
          result(5).key shouldBe "Reason for appealing after 30 days"
          result(5).value shouldBe "Lorem ipsum"
          result(5).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "for upload" in {
          val result = checkYourAnswers("other", otherAnswers, fileNames = Some("file.docx"))
          result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(3).value shouldBe "Yes"
          result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4).key shouldBe "Evidence to support this appeal"
          result(4).value shouldBe "file.docx"
          result(4).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
        }

        "for upload - and late appeal" in {
          val result = checkYourAnswers("other", otherAnswers, lateAppeal = true, fileNames = Some("file.docx"))
          result(5).key shouldBe "Reason for appealing after 30 days"
          result(5).value shouldBe "Lorem ipsum"
          result(5).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "when the user clicked no to upload don't show the upload evidence row" in {
          val result = checkYourAnswers("other", otherAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"),
            lateAppeal = true, fileNames = Some("file.docx"))
          result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(3).value shouldBe "No"
          result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4).key shouldBe "Reason for appealing after 30 days"
          result(4).value shouldBe "Lorem ipsum"
          result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "when the user has changed their answer and does not want to upload files - but existing files have been uploaded - 'hide' the row" in {
          val result = checkYourAnswers("other", otherAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"),
            lateAppeal = true, fileNames = Some("file.docx"))
          result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(3).value shouldBe "No"
          result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4).key shouldBe "Reason for appealing after 30 days"
          result(4).value shouldBe "Lorem ipsum"
          result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }
      }

      "not show the late appeal reason when the user initially selected to appeal both penalties but now does not want to (LPP2 selected, LPP1 is late)" in {
        val result = checkYourAnswers("other", otherAnswers ++ Json.obj(
            SessionKeys.doYouWantToAppealBothPenalties -> "no",
            SessionKeys.isUploadEvidence -> "no"),
          lateAppeal = true)
        result.head.key shouldBe "Do you intend to appeal both penalties for the same reason?"
        result.head.value shouldBe "No"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url,
          PenaltySelectionPage.toString
        ).url
        result.size shouldBe 5
      }
    }
  }

  "getHealthReasonAnswers" must {
    def checkYourAnswers(answers: JsObject, whoPlanned: Option[String] = None,
                         whatCaused: Option[String] = None, lateAppeal: Boolean = false) =
      determineFunction("health", None, answers, whoPlanned, whatCaused, lateAppeal)

    "when an agent is on the page" should {
      "when there is no hospital stay" should {
        "return rows of answers" in {
          val result = checkYourAnswers(noHospitalStayAnswers, Some("agent"), Some("client"))

          result.head.key shouldBe "Reason for missing the VAT deadline"
          result.head.value shouldBe "Health"
          result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1).key shouldBe "Did this health issue include a hospital stay?"
          result(1).value shouldBe "No"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2).key shouldBe "When did the health issue first stop your client getting information to you?"
          result(2).value shouldBe "2\u00A0January\u00A02022"
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }

        "when the client intended to submit the VAT return" in {
          val result = checkYourAnswers(noHospitalStayAnswers, Some("client"))

          result(2).key shouldBe "When did the health issue first stop your client submitting the VAT Return?"
          result(2).value shouldBe "2\u00A0January\u00A02022"
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }

        "when the agent intended to submit but missed the deadline" in {
          val result = checkYourAnswers(noHospitalStayAnswers, Some("agent"), Some("agent"))

          result(2).key shouldBe "When did the health issue first stop you submitting the VAT Return?"
          result(2).value shouldBe "2\u00A0January\u00A02022"
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }
      }

      "when it is an LPP show the correct message" in {
        val result = checkYourAnswers(noHospitalStayAnswers ++ Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment), Some("client"))

        result(2).key shouldBe "When did the health issue first stop your client paying the VAT bill?"
        result(2).value shouldBe "2\u00A0January\u00A02022"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
          WhenDidHealthIssueHappenPage.toString
        ).url
      }
    }

    "when a VAT trader is on the page" should {
      "when there is no hospital stay" should {
        "return rows of answers" in {
          val result = checkYourAnswers(noHospitalStayAnswers)

          result.head.key shouldBe "Reason for missing the VAT deadline"
          result.head.value shouldBe "Health"
          result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1).key shouldBe "Did this health issue include a hospital stay?"
          result(1).value shouldBe "No"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2).key shouldBe "When did the health issue first stop you submitting the VAT Return?"
          result(2).value shouldBe "2\u00A0January\u00A02022"
          result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }
      }

      "when it is an LPP show the correct message" in {
        val result = checkYourAnswers(noHospitalStayAnswers ++ Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment))

        result(2).key shouldBe "When did the health issue first stop you paying the VAT bill?"
        result(2).value shouldBe "2\u00A0January\u00A02022"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
          WhenDidHealthIssueHappenPage.toString
        ).url
      }
    }

    "when there is hospital stay ended" should {
      "return rows of question-answers" in {
        val result = checkYourAnswers(hospitalEndedAnswers)

        result(1).key shouldBe "Did this health issue include a hospital stay?"
        result(1).value shouldBe "Yes"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
          WasHospitalStayRequiredPage.toString
        ).url
        result(2).key shouldBe "When did the hospital stay begin?"
        result(2).value shouldBe "2\u00A0January\u00A02022"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
          WhenDidHospitalStayBeginPage.toString
        ).url
        result(3).key shouldBe "Has the hospital stay ended?"
        result(3).value shouldBe "Yes"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
          DidHospitalStayEndPage.toString
        ).url
        result(4).key shouldBe "When did it end?"
        result(4).value shouldBe "3\u00A0January\u00A02022"
        result(4).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(CheckMode).url,
          WhenDidHospitalStayEndPage.toString
        ).url
      }
    }

    "when there is hospital stay not ended" should {
      "return rows of question-answers" in {
        val result = checkYourAnswers(hospitalOngoingAnswers)

        result(3).key shouldBe "Has the hospital stay ended?"
        result(3).value shouldBe "No"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
          DidHospitalStayEndPage.toString
        ).url
      }
    }

    "throw a MatchError when the user has invalid health data in the session" in {
      val fakeRequestWithAllHospitalStayKeysPresent = fakeRequestConverter(Json.obj(
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenHealthIssueStarted -> "2022-01-01",
          SessionKeys.hasHealthEventEnded -> "no"
        ))

      val result = intercept[MatchError](sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllHospitalStayKeysPresent, implicitly))
      result.getMessage.contains(
        "[SessionAnswersHelper][getHealthReasonAnswers] - Attempted to load CYA page but no valid health reason data found in session") shouldBe true
    }
  }

  "for bereavement (someone died)" must {
    def checkYourAnswers(answers: JsObject): Seq[CheckYourAnswersRow] =
      determineFunction("generic", Some("bereavement"), answers)

    "return all the keys from the session ready to be passed to the view" in {
      val result = checkYourAnswers(bereavementAnswers)

      result.head.key shouldBe "Reason for missing the VAT deadline"
      result.head.value shouldBe "Bereavement (someone died)"
      result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.ReasonableExcuseController.onPageLoad().url,
        ReasonableExcuseSelectionPage.toString
      ).url
      result(1).key shouldBe "When did the person die?"
      result(1).value shouldBe "1\u00A0January\u00A02021"
      result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
        WhenDidThePersonDiePage.toString
      ).url
    }

    "return all keys and the 'Reason for appealing after 30 days' text" in {
      val result = checkYourAnswers(bereavementAnswers ++
        Json.obj(SessionKeys.whenDidThePersonDie -> LocalDate.parse("2022-01-01"),
        SessionKeys.lateAppealReason -> "Lorem ipsum"))

      result(2).key shouldBe "Reason for appealing after 30 days"
      result(2).value shouldBe "Lorem ipsum"
      result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.MakingALateAppealController.onPageLoad().url,
        MakingALateAppealPage.toString
      ).url
    }
  }

  "getContentForAgentsCheckYourAnswersPage" should {
    def checkYourAnswers(answers: JsObject, whoPlanned: String, whatCaused: Option[String] = None) =
      determineFunction("agent", Some("other"), answers, Some(whoPlanned), whatCaused)

    "when the client planned to submit VAT return (so no cause Of LateSubmission chosen)" should {
      "return rows of answers" in {
        val result = checkYourAnswers(otherAnswers, "client")

        result.head.key shouldBe "Before the deadline, who planned to submit the return?"
        result.head.value shouldBe "My client did"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage.toString
        ).url
      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being agent" should {
      "return rows of answers" in {
        val result = checkYourAnswers(otherAnswers, "agent", Some("agent"))

        result.head.key shouldBe "Before the deadline, who planned to submit the return?"
        result.head.value shouldBe "I did"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage.toString
        ).url
        result(1).key shouldBe "What caused you to miss the deadline?"
        result(1).value shouldBe "Something else happened to delay me"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
          WhatCausedYouToMissTheDeadlinePage.toString
        ).url

      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being client" should {
      "return rows of answers" in {
        val result = checkYourAnswers(otherAnswers, "agent", Some("client"))

        result(1).key shouldBe "What caused you to miss the deadline?"
        result(1).value shouldBe "My client did not get information to me on time"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
          WhatCausedYouToMissTheDeadlinePage.toString
        ).url
      }
    }
  }

  "getAllTheContentForCheckYourAnswersPage" should {
    def checkYourAnswers(answers: JsObject, excuse: String, whoPlanned: Option[String], whatCaused: Option[String],
                         fileNames: Option[String] = None, function: String): Seq[CheckYourAnswersRow] = {
      determineFunction(function, Some(excuse), answers, whoPlanned, whatCaused, fileNames =  fileNames)
    }

    "when agent session is present" should {
      "return getAllTheContentForCheckYourAnswersPage as list of getContentForAgentsCheckYourAnswersPage and  " +
        "getContentForReasonableExcuseCheckYourAnswersPage" in {
        val resultReasonableExcuses = checkYourAnswers(techIssuesAnswers, "technicalIssues", Some("client"), None, function = "generic")
        val resultAgent = checkYourAnswers(techIssuesAnswers, "technicalIssues", Some("client"), None,function = "agent")
        val resultAllContent = checkYourAnswers(techIssuesAnswers, "technicalIssues", Some("client"), None,function = "all")

        resultAgent ++ resultReasonableExcuses shouldBe resultAllContent
      }

      "return getAllTheContentForCheckYourAnswersPage as list of ONLY getContentForReasonableExcuseCheckYourAnswersPage when it is a LPP appeal" in {
        val answer = techIssuesAnswers ++ Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment)

        val resultReasonableExcuses = checkYourAnswers(answer, "technicalIssues", Some("client"), None, function = "generic")
        val resultAllContent = checkYourAnswers(answer, "technicalIssues", Some("client"), None, function = "all")

        resultReasonableExcuses shouldBe resultAllContent
      }

      "return getAllTheContentForCheckYourAnswersPage as list of ONLY getContentForReasonableExcuseCheckYourAnswersPage" +
        " when it is a LPP appeal (Additional)" in {
        val answer = techIssuesAnswers ++ Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Additional)

        val resultReasonableExcuses = checkYourAnswers(answer, "technicalIssues", Some("client"), None, function = "generic")
        val resultAllContent = checkYourAnswers(answer, "technicalIssues", Some("client"), None, function = "all")

        resultReasonableExcuses shouldBe resultAllContent
      }
    }
    "agent session is not present" when {
      "the appeal is against the obligation" must {
        "show the obligation variation of the page" in {
          val result = checkYourAnswers(obligationAnswers, "other", None, None, function = "all", fileNames = Some("file.txt"))

          result.head.key shouldBe "Tell us why you want to appeal the penalty"
          result.head.value shouldBe "This is some relevant information"
          result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
            OtherRelevantInformationPage.toString
          ).url
          result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(1).value shouldBe "Yes"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(2).key shouldBe "Evidence to support this appeal"
          result(2).value shouldBe "file.txt"
          result(2).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
        }

        "show the obligation variation of the page - 'hide' the files uploaded if user selected no to uploading files" in {
          val result = checkYourAnswers(obligationAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"),
            "other", None, None, function = "all", fileNames = Some("file.txt"))

          result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
          result(1).value shouldBe "No"
          result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result.size shouldBe 2
        }
      }

      "return getAllTheContentForCheckYourAnswersPage as list of getContentForReasonableExcuseCheckYourAnswersPage only" in {
        val resultReasonableExcuses = checkYourAnswers(techIssuesAnswers, "technicalIssues", None, None, function = "generic")
        val resultAllContent = checkYourAnswers(techIssuesAnswers, "technicalIssues", None, None, function = "all")

        resultReasonableExcuses shouldBe resultAllContent

      }
    }
  }

  "getContentForObligationAppealCheckYourAnswersPage" should {
    def checkYourAnswers(answers: JsObject, fileName: Option[String]): Seq[CheckYourAnswersRow] =
      determineFunction("obligation", reasonableExcuse = None, answers, fileNames = fileName)
    "when no evidence file uploaded" should {
      "return rows of answers" in {
        val result = checkYourAnswers(obligationAnswers, None)

        result.head.key shouldBe "Tell us why you want to appeal the penalty"
        result.head.value shouldBe "This is some relevant information"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(1).value shouldBe "Yes"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(2).key shouldBe "Evidence to support this appeal"
        result(2).value shouldBe "Not provided"
        result(2).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
      }

      "return rows of answers - without uploaded files row" in {
        val result = checkYourAnswers(obligationAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"), None)

        result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(1).value shouldBe "No"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result.size shouldBe 2
      }
    }

    "when evidence file is uploaded" should {
      "return rows of answers" in {
        val result = checkYourAnswers(obligationAnswers, Some("some-file-name.txt"))

        result(2).key shouldBe "Evidence to support this appeal"
        result(2).value shouldBe "some-file-name.txt"
        result(2).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
      }

      "the user has selected no to uploaded files - 'hide' the row" in {
        val result = checkYourAnswers(obligationAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"), Some("some-file-name.txt"))

        result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(1).value shouldBe "No"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result.size shouldBe 2
      }
    }
  }

  "getPreviousUploadsFileNames" should {
    "return the file names" in {
      val fakeRequestForOtherJourney: UserRequest[AnyContent] = fakeRequestConverter(correctUserAnswers ++ Json.obj(
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> true,
        SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        SessionKeys.whenDidBecomeUnable -> "2022-01-02"
      ), fakeRequest)
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
      when(mockRepository.getUploadsForJourney(Some("1234"))).thenReturn(Future.successful(Option(Seq(callBackModel))))
      await(sessionAnswersHelper.getPreviousUploadsFileNames()(fakeRequestForOtherJourney)) shouldBe "file1.txt"
    }
  }

  "getContentWithExistingUploadFileNames" should {
    def fileNameHelper(answers: JsObject, reasonableExcuse: String): Future[Seq[CheckYourAnswersRow]] = {
      val fakeRequest = fakeRequestWithKeys(answers, lateReason = false, None, None)
      sessionAnswersHelper.getContentWithExistingUploadFileNames(reasonableExcuse)(fakeRequest, implicitly)
    }

    "when reason is 'other' (that requires a file upload call)" should {
      "return the rows for CYA page" in {
        val result = await(fileNameHelper(otherAnswers, "other"))

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "The reason does not fit into any of the other categories"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the issue first stop you submitting the VAT Return?"
        result(1).value shouldBe "2\u00A0January\u00A02022"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
          WhenDidBecomeUnablePage.toString
        ).url
        result(2).key shouldBe "Why was the return submitted late?"
        result(2).value shouldBe "This is a reason."
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
          WhyWasReturnSubmittedLatePage.toString
        ).url
        result(3).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(3).value shouldBe "Yes"
        result(3).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4).key shouldBe "Evidence to support this appeal"
        result(4).value shouldBe "file1.txt"
        result(4).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
      }
    }

    "when there's an Obligation Appeal Journey (that requires a file upload call) " should {
      "return the rows for CYA page " in {
        val result = await(fileNameHelper(obligationAnswers, "other"))

        result.head.key shouldBe "Tell us why you want to appeal the penalty"
        result.head.value shouldBe "This is some relevant information"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
        result(1).value shouldBe "Yes"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(2).key shouldBe "Evidence to support this appeal"
        result(2).value shouldBe "file1.txt"
        result(2).url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, isJsEnabled = false).url
      }
    }

    "when the user has files uploaded - but changed their mind - 'hide' the files uploaded" in {
      val result = await(fileNameHelper(obligationAnswers ++ Json.obj(SessionKeys.isUploadEvidence -> "no"), "other"))

      result.head.key shouldBe "Tell us why you want to appeal the penalty"
      result.head.value shouldBe "This is some relevant information"
      result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
        OtherRelevantInformationPage.toString
      ).url
      result(1).key shouldBe "Do you want to upload evidence to support your appeal?"
      result(1).value shouldBe "No"
      result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
        UploadEvidenceQuestionPage.toString
      ).url
      result.size shouldBe 2
    }

    "when reason is 'bereavement' (that doesn't require a file upload call)" should {
      "return the rows for CYA page " in {
        val result = await(fileNameHelper(bereavementAnswers ++
          Json.obj(SessionKeys.isUploadEvidence -> "yes", SessionKeys.lateAppealReason -> "Lorem ipsum"), "other"))

        result.head.key shouldBe "Reason for missing the VAT deadline"
        result.head.value shouldBe "Bereavement (someone died)"
        result.head.url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1).key shouldBe "When did the person die?"
        result(1).value shouldBe "1\u00A0January\u00A02021"
        result(1).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
          WhenDidThePersonDiePage.toString
        ).url
        result(2).key shouldBe "Reason for appealing after 30 days"
        result(2).value shouldBe "Lorem ipsum"
        result(2).url shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
        result.size shouldBe 3
      }
    }
  }
}
