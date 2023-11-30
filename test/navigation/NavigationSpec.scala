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

package navigation

import base.SpecBase
import config.featureSwitches.{ShowFindOutHowToAppealLSPJourney, ShowFullAppealAgainstTheObligation}
import models.pages._
import models.{CheckMode, NormalMode, PenaltyTypeEnum, UserRequest}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.SessionKeys

import java.time.LocalDate

class NavigationSpec extends SpecBase {
  val mockConfiguration: Configuration = mock(classOf[Configuration])
  object TestNavigator extends Navigation(mockDateTimeHelper, appConfig, isLateAppealHelper)(mockConfiguration)

  class Setup {
    reset(mockDateTimeHelper)
    reset(mockConfiguration)
    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 2, 1))
    when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation.name))(ArgumentMatchers.any()))
      .thenReturn(true)
  }

  def checkModePreviousPageTest(pagesAndUrls: Seq[(Page, String)]): Unit = {
    pagesAndUrls.foreach { pageAndExpectedUrl =>
      s"called with ${pageAndExpectedUrl._1} - route to the correct page" in {
        val result: Call = TestNavigator.previousPage(pageAndExpectedUrl._1, CheckMode, false)(fakeRequestConverter(correctUserAnswers))
        result.url shouldBe pageAndExpectedUrl._2
      }
    }
  }

  def normalModePreviousPageTest(pagesAndUrls: Seq[(Page, String)]): Unit = {
    pagesAndUrls.foreach { pageAndExpectedUrl =>
      s"called with ${pageAndExpectedUrl._1} - route to the correct page" in {
        val result: Call = TestNavigator.previousPage(pageAndExpectedUrl._1, NormalMode, false)(fakeRequestConverter(correctUserAnswers))
        result.url shouldBe pageAndExpectedUrl._2
      }
    }
  }

  "previousPage" should {
    "in CheckMode" when {
      checkModePreviousPageTest(Seq(
        (DidHospitalStayEndPage, controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url),
        (WhenDidHospitalStayBeginPage, controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
        (WhenDidHealthIssueHappenPage, controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url),
        (UploadFirstDocumentPage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url),
        (UploadAnotherDocumentPage, controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url),
        (EvidencePage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url),
        (FileListPage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url),
        (UploadEvidenceQuestionPage, controllers.routes.CheckYourAnswersController.onPageLoad().url),
        (AppealSinglePenaltyPage, controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url),
        (AppealCoverBothPenaltiesPage, controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(CheckMode).url)
      ))

      "the user is on the original page they were routed to - route back to CYA" in {
        val result: Call = TestNavigator.previousPage(WhenDidHospitalStayBeginPage, CheckMode, false)(fakeRequestConverter(correctUserAnswers, fakeRequest.withSession(SessionKeys.originatingChangePage -> WhenDidHospitalStayBeginPage.toString)))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "in NormalMode" when {
      normalModePreviousPageTest(
        Seq(
          (CancelVATRegistrationPage, "http://localhost:9180/penalties"),
          (YouCannotAppealPage, controllers.routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url),
          (YouCanAppealThisPenaltyPage, controllers.routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url),
          (OtherRelevantInformationPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (FileListPage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url),
          (UploadFirstDocumentPage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url),
          (UploadAnotherDocumentPage, controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url),
          (EvidencePage, controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url),
          (WhenDidThePersonDiePage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (HasCrimeBeenReportedPage, controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url),
          (WhenDidCrimeHappenPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WhenDidFireOrFloodHappenPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WasHospitalStayRequiredPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WhenDidHealthIssueHappenPage, controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode).url),
          (WhenDidHospitalStayBeginPage, controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode).url),
          (DidHospitalStayEndPage, controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url),
          (WhenDidPersonLeaveTheBusinessPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WhenDidTechnologyIssuesBeginPage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WhenDidTechnologyIssuesEndPage, controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(NormalMode).url),
          (WhenDidBecomeUnablePage, controllers.routes.HonestyDeclarationController.onPageLoad().url),
          (WhyWasReturnSubmittedLatePage, controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(NormalMode).url),
          (WhoPlannedToSubmitVATReturnAgentPage, controllers.routes.AppealStartController.onPageLoad().url),
          (WhatCausedYouToMissTheDeadlinePage, controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode).url),
          (PenaltySelectionPage, controllers.routes.AppealStartController.onPageLoad().url),
          (AppealSinglePenaltyPage, controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode).url),
          (AppealCoverBothPenaltiesPage, controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode).url),
          (AppealByLetterKickOutPage, controllers.routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url),
          (OtherWaysToAppealPage, controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad().url),
          (HasBusinessAskedHMRCToCancelRegistrationPage, "http://localhost:9180/penalties"),
          (HasHMRCConfirmedRegistrationCancellationPage, controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad().url)
        )
      )

      s"the user is on the $AppealStartPage" must {
        "route the user back to the 'You can appeal this penalty' page when appealing against obligation" in {
          val result: Call = TestNavigator.previousPage(AppealStartPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)))
          result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
        }

        "route back to the penalties and appeals page when its not an obligation appeal" in {
          val result: Call = TestNavigator.previousPage(AppealStartPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers))
          result.url shouldBe "http://localhost:9180/penalties"
        }
      }

      s"the user is on the $HonestyDeclarationPage" must {
        "route the user back to the 'Appeal a VAT penalty' page when appealing against obligation" in {
          val result: Call = TestNavigator.previousPage(HonestyDeclarationPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)))
          result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
        }

        "route back to the reasonable excuse selection page when not appealing against obligation" in {
          val result: Call = TestNavigator.previousPage(HonestyDeclarationPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "health")))
          result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        }
      }

      s"the user is on the $UploadEvidenceQuestionPage" must {
        "route the user back to the 'Other relevant information' page when appealing against obligation" in {
          val result: Call = TestNavigator.previousPage(UploadEvidenceQuestionPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)))
          result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
        }

        "route back to the why return submitted late page when not appealing against obligation" in {
          val result: Call = TestNavigator.previousPage(UploadEvidenceQuestionPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "health")))
          result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
        }
      }

      s"the user is on the $MakingALateAppealPage" must {
        def makingALateAppealReverseNormalRouteTest(reason: String, expectedUrl: String, userRequest: UserRequest[_]): Unit = {
          s"route the user back to the correct page when appealing with $reason reason" in {
            val result: Call = TestNavigator.previousPage(MakingALateAppealPage, NormalMode, true)(userRequest)
            result.url shouldBe expectedUrl
          }
        }

        makingALateAppealReverseNormalRouteTest("bereavement",
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "bereavement")))

        makingALateAppealReverseNormalRouteTest("crime",
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "crime")))

        makingALateAppealReverseNormalRouteTest("fireOrFlood",
          controllers.routes.FireOrFloodReasonController.onPageLoad(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "fireOrFlood")))

        makingALateAppealReverseNormalRouteTest("health - hospital stay ended",
          controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "health",
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.hasHealthEventEnded -> "yes",
            SessionKeys.whenHealthIssueEnded -> "2021-02-01",
            SessionKeys.whenHealthIssueStarted -> "2021-02-01")))

        makingALateAppealReverseNormalRouteTest("health - hospital stay ongoing",
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "health",
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.hasHealthEventEnded -> "no",
            SessionKeys.whenHealthIssueStarted -> "2021-02-01")))

        makingALateAppealReverseNormalRouteTest("health - no hospital stay",
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "health")))

        makingALateAppealReverseNormalRouteTest("lossOfStaff",
          controllers.routes.LossOfStaffReasonController.onPageLoad(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "lossOfStaff")))

        makingALateAppealReverseNormalRouteTest("technicalIssues",
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "technicalIssues")))

        makingALateAppealReverseNormalRouteTest("other",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "other",
            SessionKeys.isUploadEvidence -> "yes")))

        makingALateAppealReverseNormalRouteTest("other - no upload",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "other")))

        makingALateAppealReverseNormalRouteTest("obligation",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url,
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.isObligationAppeal -> true)))

      }

      s"the user is on the $ReasonableExcuseSelectionPage" when {
        "the user is an agent and appealing an LSP" must {
          "route back to the 'what caused you to miss the deadline' page if the agent planned to submit" in {
            val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(agentFakeRequestConverter(correctUserAnswers ++ Json.obj(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent"
            )))
            result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode).url
          }

          "route back to the 'who planned to submit return' page if the client planned to submit" in {
            val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(agentFakeRequestConverter(correctUserAnswers ++ Json.obj(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
              SessionKeys.whoPlannedToSubmitVATReturn -> "client"
            )))
            result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode).url
          }
        }

        "the user has the option to appeal both penalties" must {
          "route back to appeal both penalties page" when {
            "they have selected to appeal both penalties" in {
              val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
                SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
                SessionKeys.doYouWantToAppealBothPenalties -> "yes"
              )))
              result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
            }
          }

          "route back to appeal single penalty page" when {
            "they have selected NOT to appeal both penalties" in {
              val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
                SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
                SessionKeys.doYouWantToAppealBothPenalties -> "no"
              )))
              result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
            }
          }
        }

        "the user is not agent or not appealing an LSP as agent" must {
          "route back to the landing page" in {
            val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(userRequestWithCorrectKeys)
            result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
          }
        }
      }

      s"the user is on the $CheckYourAnswersPage" when {
        "the user is appealing late" must {
          "route back to the 'making a late appeal' page if the agent planned to submit" in new Setup {
            when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
              2020, 4, 1))
            val result: Call = TestNavigator.previousPage(CheckYourAnswersPage, NormalMode, false)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
            )
            result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          }

          "route back to the previous page before making a late appeal if the client planned to submit" in new Setup {
            val result: Call = TestNavigator.previousPage(CheckYourAnswersPage, NormalMode, false)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
            result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
          }
        }
      }

      s"the user is on the $AppealByLetterKickOutPage" when {
        s"the $ShowFindOutHowToAppealLSPJourney feature switch is enabled" must {
          "route to has HMRC confirmed registration cancelled registration page" in {
            when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFindOutHowToAppealLSPJourney.name))(ArgumentMatchers.any())).thenReturn(true)
            val result: Call = TestNavigator.previousPage(AppealByLetterKickOutPage, NormalMode, false)(userRequestWithCorrectKeys)
            result.url shouldBe controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad().url
          }
        }

        s"the $ShowFindOutHowToAppealLSPJourney feature switch is disabled" must {
          "route to has Cancel VAT registration page" in {
            when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFindOutHowToAppealLSPJourney.name))(ArgumentMatchers.any())).thenReturn(false)
            val result: Call = TestNavigator.previousPage(AppealByLetterKickOutPage, NormalMode, false)(userRequestWithCorrectKeys)
            result.url shouldBe controllers.routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
          }
        }
      }

      "route the user back to the CYA page if they originated on this page" in {
        val result: Call = TestNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode, false)(fakeRequestConverter(correctUserAnswers, fakeRequest.withSession(SessionKeys.originatingChangePage -> ReasonableExcuseSelectionPage.toString)))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

  }

  "nextPage" should {
    "in CheckMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result: Call = TestNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidTechnologyIssuesEndPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
      }

      s"called with $WasHospitalStayRequiredPage and answer for 'WhenDidHealthIssueHappenPage' is set" in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whenHealthIssueHappened -> LocalDate.parse("2020-01-01")
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidBecomeUnablePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whenDidBecomeUnable -> LocalDate.parse("2020-01-01")
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhyWasReturnSubmittedLatePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whyReturnSubmittedLate -> "this is a good reason"
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $EvidencePage" in new Setup {
        val result: Call = TestNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage - navigate to when did hospital stay end page when user answers yes" in new Setup {
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, CheckMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(CheckMode).url
      }

      s"called with $DidHospitalStayEndPage - navigate to CYA page when user answers no" in new Setup {
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, CheckMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage - navigate to making a late page when user answers no " +
        s"(appeal > 30 days late)" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, CheckMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayEndPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayEndPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayBeginPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage - user selected 'client' and now changes to agent" in new Setup {
        val result: Call = TestNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, CheckMode, Some("agent"))(
          fakeRequestConverter(correctUserAnswers ++ Json.obj(SessionKeys.whoPlannedToSubmitVATReturn -> "client")))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
          SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "crime"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "lossOfStaff"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "fireOrFlood"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "health"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "other"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayEndPage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "health"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, CheckMode, Some("client"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $ReasonableExcuseSelectionPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(ReasonableExcuseSelectionPage, CheckMode, None)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidThePersonDiePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidThePersonDiePage, CheckMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.reasonableExcuse -> "bereavement"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }
      s"called with $OtherRelevantInformationPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(None, CheckMode)
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
      }
      s"called with $UploadFirstDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = TestNavigator.nextPage(UploadFirstDocumentPage, CheckMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
      }

      s"called with $FileListPage redirects to $UploadAnotherDocumentPage when the user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, CheckMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(CheckMode).url
      }

      s"called with $FileListPage redirects to CYA page when the user selects no" in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, CheckMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded " in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, CheckMode)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage" in new Setup {
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url
      }

      s"called with $AppealSinglePenaltyPage" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealSinglePenaltyPage, CheckMode, None)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $AppealCoverBothPenaltiesPage" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealCoverBothPenaltiesPage, CheckMode, None)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $AppealCoverBothPenaltiesPage - one appeal is > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2022, 1, 1))
        val result: Call = TestNavigator.nextPage(AppealCoverBothPenaltiesPage, CheckMode, None)(userRequestWithCorrectKeys.copy(
          answers = userAnswers(correctUserAnswers ++ Json.obj(
            SessionKeys.doYouWantToAppealBothPenalties -> "yes",
            SessionKeys.firstPenaltyCommunicationDate -> "2021-12-01",
            SessionKeys.secondPenaltyCommunicationDate -> "2022-01-01"
          ))
        ))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(PenaltySelectionPage, CheckMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(CheckMode).url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects no" in new Setup {
        val result: Call = TestNavigator.nextPage(PenaltySelectionPage, CheckMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(CheckMode).url
      }
    }

    "in NormalMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result: Call = TestNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidCrimeHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
      }

      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayEndPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidHospitalStayBeginPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhenDidBecomeUnablePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhyWasReturnSubmittedLatePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      }

      s"called with $UploadEvidenceQuestionPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage" in new Setup {
        val result: Call = TestNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidTechnologyIssuesEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the answer is yes" in new Setup {
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayEnd(NormalMode).url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the answer is no (> 30 days late appeal)" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the answer is no (route to CYA)" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 3, 1))
        val result: Call = TestNavigator.nextPage(DidHospitalStayEndPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, NormalMode, None)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whatCausedYouToMissTheDeadline -> "client"
        )))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage - selects client" in new Setup {
        val result: Call = TestNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, NormalMode, Some("client"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "client"
        )))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage" in new Setup {
        val result: Call = TestNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, NormalMode, Some("agent"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whoPlannedToSubmitVATReturn -> "agent"
        )))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode).url
      }

      s"called with $WhenDidThePersonDiePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = TestNavigator.nextPage(WhenDidThePersonDiePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("bereavement"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $HonestyDeclarationPage" in new Setup {
        val result: Call = TestNavigator.nextPage(HonestyDeclarationPage, NormalMode, Some("crime"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.hasConfirmedDeclaration -> true
        )))
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      }

      s"called with $HonestyDeclarationPage isObligation is true" in new Setup {

        val result: Call = TestNavigator.nextPage(HonestyDeclarationPage, NormalMode)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.hasConfirmedDeclaration -> true
        )))
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
      }

      s"called with $EvidencePage isObligation is true" in new Setup {
        val result: Call = TestNavigator.nextPage(EvidencePage, NormalMode)(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.isObligationAppeal -> true
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadFirstDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = TestNavigator.nextPage(UploadFirstDocumentPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
      }

      s"called with $FileListPage redirects to $UploadAnotherDocumentPage when the user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      }

      s"called with $FileListPage redirect making a late appeal page - when the user has 5 documents uploaded and appeal > 30 days late and user answers no" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(FileListPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded and user answers no" in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $FileListPage redirect making a late appeal page - when the user has 5 documents uploaded and appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(FileListPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded " in new Setup {
        val result: Call = TestNavigator.nextPage(FileListPage, NormalMode)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage" in new Setup {
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("yes"), Some(true))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
      }

      s"called with $UploadEvidenceQuestionPage - user answer no (non late appeal)" in new Setup {
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage - user answer no (late appeal)" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $YouCanAppealThisPenaltyPage - user answer yes (route to appeal start page - obligation)" in new Setup {
        val result: Call = TestNavigator.nextPage(YouCanAppealThisPenaltyPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("obligation")
        )
        result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
      }

      s"called with $YouCanAppealThisPenaltyPage - user answer no (route to penalties and appeals page)" in new Setup {
        val result: Call = TestNavigator.nextPage(YouCanAppealThisPenaltyPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("obligation")
        )
        result.url shouldBe "http://localhost:9180/penalties"
      }

      s"called with $AppealSinglePenaltyPage" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealSinglePenaltyPage, NormalMode, None)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $AppealCoverBothPenaltiesPage" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealCoverBothPenaltiesPage, NormalMode, None)(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(PenaltySelectionPage, NormalMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects no" in new Setup {
        val result: Call = TestNavigator.nextPage(PenaltySelectionPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
      }

      s"called with $AppealAfterPaymentPlanSetUpPage - redirects to time to pay service when user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealAfterPaymentPlanSetUpPage, NormalMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe appConfig.timeToPayUrl
      }

      s"called with $AppealAfterPaymentPlanSetUpPage - redirects to other ways to appeal page when user selects no" in new Setup {
        val result: Call = TestNavigator.nextPage(AppealAfterPaymentPlanSetUpPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.findOutHowToAppeal.routes.OtherWaysToAppealController.onPageLoad().url
      }
      s"called with $CanYouPayPage - redirects to appeal after vat is paid page when user selects yes" in new Setup {
        val result: Call = TestNavigator.nextPage(CanYouPayPage, NormalMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad().url
      }

      s"called with $CanYouPayPage - redirects to appeal after payment plan is setup page when user selects no" in new Setup {
        val result: Call = TestNavigator.nextPage(CanYouPayPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad().url
      }
      s"called with $CanYouPayPage - redirects to if you've paid your vatpage when user selects paid" in new Setup {
        val result: Call = TestNavigator.nextPage(CanYouPayPage, NormalMode, Some("paid"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.findOutHowToAppeal.routes.IfYouvePaidYourVATController.onPageLoad().url
      }

      s"called with $HasBusinessAskedHMRCToCancelRegistrationPage - redirects to has HMRC confirmed registration cancelled when user answers yes" in new Setup {
        val result: Call = TestNavigator.nextPage(HasBusinessAskedHMRCToCancelRegistrationPage, NormalMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad().url
      }

      s"called with $HasBusinessAskedHMRCToCancelRegistrationPage - redirects to actions to take page when user answers no" in new Setup {
        val result: Call = TestNavigator.nextPage(HasBusinessAskedHMRCToCancelRegistrationPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe "/" //TODO: change when PRM-3086 is implemented
      }

      s"called with $HasHMRCConfirmedRegistrationCancellationPage - redirects to has Appeal by letter when user answers yes" in new Setup {
        val result: Call = TestNavigator.nextPage(HasHMRCConfirmedRegistrationCancellationPage, NormalMode, Some("yes"))(userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      }

      s"called with $HasHMRCConfirmedRegistrationCancellationPage - redirects to actions to take page when user answers no" in new Setup {
        val result: Call = TestNavigator.nextPage(HasHMRCConfirmedRegistrationCancellationPage, NormalMode, Some("no"))(userRequestWithCorrectKeys)
        result.url shouldBe "/" //TODO: change when PRM-3086 is implemented
      }
    }
  }

  "routingForHospitalStay" should {
    "redirect to the appropriate page in normal mode" when {
      "user answers no " in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
        reset(mockDateTimeHelper)
      }

      "user answers yes" in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("yes"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url
        reset(mockDateTimeHelper)
      }
    }

    "redirect to the CYA page in check mode" when {
      "user answers no and the question on the next page has already been answered" in new Setup {
        val result: Call = TestNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.whenHealthIssueHappened -> "2020-01-01"
        )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

    }
  }

  "routeToMakingALateAppealOrCYAPage" should {
    "route to CYA page" when {
      "the appeal is late but the reason has already been given and we are in CheckMode" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.lateAppealReason -> "This is a reason.",
          SessionKeys.appealType -> "crime"
        )), CheckMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      "the appeal is not late" in new Setup {
        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "crime"
        )), NormalMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "route to late appeal reason page" when {
      "the appeal is late and there is no existing reason as to why" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "crime"
        )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "appealing multiple penalties and the LPP1 communication date is more than 30 days ago" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "crime",
          SessionKeys.firstPenaltyCommunicationDate -> "2020-02-01",
          SessionKeys.secondPenaltyCommunicationDate -> "2020-03-31",
          SessionKeys.dateCommunicationSent -> "",
          SessionKeys.doYouWantToAppealBothPenalties -> "yes"
        )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "appealing multiple penalties and both the LPP1 / LPP2 communication date are more than 30 days ago" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.appealType -> "crime",
          SessionKeys.firstPenaltyCommunicationDate -> "2020-02-01",
          SessionKeys.secondPenaltyCommunicationDate -> "2020-02-28",
          SessionKeys.dateCommunicationSent -> "",
          SessionKeys.doYouWantToAppealBothPenalties -> "yes"
        )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "the appeal is late but the reason has already been given and we are in NormalMode" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = TestNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(correctUserAnswers ++ Json.obj(
          SessionKeys.lateAppealReason -> "This is a reason.",
          SessionKeys.appealType -> "crime"
        )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }
  }

  "getNextURLBasedOnReasonableExcuse" should {
    "in NormalMode" must {
      s"called with $WhenDidThePersonDiePage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("bereavement"), NormalMode)
        result.url shouldBe controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(NormalMode).url
      }
      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("crime"), NormalMode)
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      }
      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("fireOrFlood"), NormalMode)
        result.url shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(NormalMode).url
      }
      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("technicalIssues"), NormalMode)
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(NormalMode).url
      }
      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("health"), NormalMode)
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode).url
      }
      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(Some("other"), NormalMode)
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(NormalMode).url
      }
      s"called with $OtherRelevantInformationPage" in new Setup {
        val result: Call = TestNavigator.getNextURLBasedOnReasonableExcuse(None, NormalMode)
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
      }
      "called with Unknown Page" in new Setup {
        val result: MatchError = intercept[MatchError](TestNavigator.getNextURLBasedOnReasonableExcuse(Some("unknown"), NormalMode))
        result.getMessage.contains("[Navigation][getNextURLBasedOnReasonableExcuse] - Unknown reasonable excuse Some(unknown)") shouldBe true
      }
    }
  }

  "routingForCancelVATRegistrationPage" when {

    "redirect to YouCanAppeal page" when {
      "yes option selected and full journey is enabled" in new Setup {
        val result: Call = TestNavigator.routingForCancelVATRegistrationPage(Some("yes"), userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
      }
    }

    "redirect to AppealByLetter page" when {
      "yes option selected and full journey is disabled" in new Setup {
        when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFullAppealAgainstTheObligation.name))(ArgumentMatchers.any()))
          .thenReturn(false)
        val result: Call = TestNavigator.routingForCancelVATRegistrationPage(Some("yes"), userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      }
    }

    "redirect to YouCannotAppeal page" when {
      "no option selected" in new Setup {
        val result: Call = TestNavigator.routingForCancelVATRegistrationPage(Some("no"), userRequestWithCorrectKeys)
        result.url shouldBe controllers.routes.YouCannotAppealController.onPageLoad.url
      }
    }

  }

  "routeForYouCanAppealPenalty" should {
    "redirect to the appeal start page when the user selects yes" in new Setup {
      val result: Call = TestNavigator.routeForYouCanAppealPenalty(Some("yes"))
      result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
    }

    "redirect to the penalties and appeals page when the user selects no" in new Setup {
      val result: Call = TestNavigator.routeForYouCanAppealPenalty(Some("no"))
      result.url shouldBe appConfig.penaltiesFrontendUrl
    }

    "reload the page when the matching fails" in new Setup {
      val result: Call = TestNavigator.routeForYouCanAppealPenalty(Some("blah"))
      result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
    }
  }

  "routeForUploadEvidenceQuestion" should {
    "redirect to Upload Evidence page in normal mode" when {
      "user answers yes for Upload Evidence Question " in new Setup {
        val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("yes"), Some(true))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode, true).url
      }

      "redirect to CYA/making a late appeal page in Normal Mode" when {
        "user answers no for Upload Evidence Question" in new Setup {
          val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }

        "user answers no for Upload Evidence Question - late appeal" in new Setup {
          when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
            2020, 4, 1))
          val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }
      }

      "redirect to the Upload Evidence page in Check Mode" when {
        "user answers yes for Upload Evidence Question" in new Setup {
          val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, Some("yes"), Some(true))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode, true).url
        }
      }

      "redirect to the CYA page in Check Mode" when {
        "user answers no for Upload Evidence Question" in new Setup {
          val result: Call = TestNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }
      }
    }
  }

  "routingForPenaltySelectionPage" should {
    "redirect to the single appeal page" when {
      "user selects no" in new Setup {
        val result: Call = TestNavigator.routingForPenaltySelectionPage(Some("no"), NormalMode)
        result shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode)
      }
    }

    "redirect to the multiple appeal page" when {
      "the user selects yes" in {
        val result: Call = TestNavigator.routingForPenaltySelectionPage(Some("yes"), NormalMode)
        result shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode)
      }
    }
    "throw match error when given an unknown page" when {
      "the user selects unknown" in {
        val result: MatchError = intercept[MatchError](TestNavigator.reverseCheckingRoutes(HonestyDeclarationPage, fakeRequestConverter(correctUserAnswers)))
        result.getMessage.contains("[Navigation][reverseCheckingRoutes] - Unknown page HonestyDeclarationPage") shouldBe true
      }
    }
  }

  "routingForSetUpPaymentPlanPage" should {
    "redirect to the appropriate page in normal mode" when {
      "user answers no " in new Setup {
        val result: Call = TestNavigator.routingForSetUpPaymentPlanPage(Some("no"))
        result.url shouldBe controllers.findOutHowToAppeal.routes.OtherWaysToAppealController.onPageLoad().url
      }

      "user answers yes" in new Setup {
        val result: Call = TestNavigator.routingForSetUpPaymentPlanPage(Some("yes"))
        result.url shouldBe appConfig.timeToPayUrl
      }
    }
  }

  "routingForCanYouPayPage" should {
    "redirect to the appropriate page in normal mode" when {
      "user answers yes " in new Setup {
        val result: Call = TestNavigator.routingForCanYouPayPage(Some("yes"))
        result.url shouldBe controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onPageLoad().url
      }
      "user answers no " in new Setup {
        val result: Call = TestNavigator.routingForCanYouPayPage(Some("no"))
        result.url shouldBe controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onPageLoad().url
      }
      "user answers paid " in new Setup {
        val result: Call = TestNavigator.routingForCanYouPayPage(Some("paid"))
        result.url shouldBe controllers.findOutHowToAppeal.routes.IfYouvePaidYourVATController.onPageLoad().url
      }
    }
  }

  "routingForHasBusinessAskedHMRCToCancelRegistrationPage" should {
    "redirect to the 'Has HMRC confirmed that the registration is cancelled?' page" when {
      "the user answers yes" in new Setup {
        val result: Call = TestNavigator.routingForHasBusinessAskedHMRCToCancelRegistrationPage(Some("yes"))
        result.url shouldBe controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad().url
      }
    }

    "redirect to the 'Actions to take before you can appeal online' page" when {
      "the user answers no" in new Setup {
        val result: Call = TestNavigator.routingForHasBusinessAskedHMRCToCancelRegistrationPage(Some("no"))
        result.url shouldBe "/" //TODO: change when PRM-3086 is implemented
      }
    }

    "redirect back to the 'Has the business asked HMRC to cancel the VAT registration?' page" when {
      "there is no answer or the answer is not valid" in new Setup {
        val result: Call = TestNavigator.routingForHasBusinessAskedHMRCToCancelRegistrationPage(None)
        result.url shouldBe controllers.findOutHowToAppeal.routes.HasBusinessAskedHMRCToCancelRegistrationController.onPageLoad().url
      }
    }
  }

  "routingForHasHMRCConfirmedRegistrationCancellationPage" should {
    "redirect to the 'Appeal by letter' page" when {
      "the user answers yes" in new Setup {
        val result: Call = TestNavigator.routingForHasHMRCConfirmedRegistrationCancellationPage(Some("yes"))
        result.url shouldBe controllers.routes.YouCannotAppealController.onPageLoadAppealByLetter().url
      }
    }

    "redirect to the 'Actions to take before you can appeal online' page" when {
      "the user answers no" in new Setup {
        val result: Call = TestNavigator.routingForHasHMRCConfirmedRegistrationCancellationPage(Some("no"))
        result.url shouldBe "/" //TODO: change when PRM-3086 is implemented
      }
    }

    "redirect back to the 'Has HMRC confirmed that the registration is cancelled?' page" when {
      "there is no answer or the answer is not valid" in new Setup {
        val result: Call = TestNavigator.routingForHasHMRCConfirmedRegistrationCancellationPage(None)
        result.url shouldBe controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad().url
      }
    }
  }

  "reverseRouteForAppealByLetterPage" should {
    "redirect to the 'Has HMRC confirmed that the registration is cancelled?' page" when {
      s"the $ShowFindOutHowToAppealLSPJourney is enabled" in new Setup {
        when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFindOutHowToAppealLSPJourney.name))(ArgumentMatchers.any())).thenReturn(true)
        val result: Call = TestNavigator.reverseRouteForAppealByLetterPage()
        result.url shouldBe controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onPageLoad().url
      }
    }

    "redirect to the 'Has HMRC been asked to cancel the VAT registration for this business?' page" when {
      s"the $ShowFindOutHowToAppealLSPJourney is disabled" in new Setup {
        when(mockConfiguration.get[Boolean](ArgumentMatchers.eq(ShowFindOutHowToAppealLSPJourney.name))(ArgumentMatchers.any())).thenReturn(false)
        val result: Call = TestNavigator.reverseRouteForAppealByLetterPage()
        result.url shouldBe controllers.routes.CancelVATRegistrationController.onPageLoadForCancelVATRegistration().url
      }
    }
  }
}