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

package navigation

import base.SpecBase
import models.pages._
import models.{CheckMode, NormalMode, PenaltyTypeEnum, UserRequest}
import org.mockito.Mockito._
import play.api.mvc.Call
import utils.SessionKeys

import java.time.LocalDate

class NavigationSpec extends SpecBase {
  class Setup {
    reset(mockDateTimeHelper)
    when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
      2020, 2, 1))

  }

  def checkModePreviousPageTest(pagesAndUrls: Seq[(Page, String)]): Unit = {
    pagesAndUrls.foreach { pageAndExpectedUrl =>
      s"called with ${pageAndExpectedUrl._1} - route to the correct page" in {
        val result: Call = mainNavigator.previousPage(pageAndExpectedUrl._1, CheckMode)(fakeRequestConverter(fakeRequest))
        result.url shouldBe pageAndExpectedUrl._2
      }
    }
  }

  def normalModePreviousPageTest(pagesAndUrls: Seq[(Page, String)]): Unit = {
    pagesAndUrls.foreach { pageAndExpectedUrl =>
      s"called with ${pageAndExpectedUrl._1} - route to the correct page" in {
        val result: Call = mainNavigator.previousPage(pageAndExpectedUrl._1, NormalMode)(fakeRequestConverter(fakeRequest))
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
        val result: Call = mainNavigator.previousPage(WhenDidHospitalStayBeginPage, CheckMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.originatingChangePage -> WhenDidHospitalStayBeginPage.toString)))
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
          (AppealCoverBothPenaltiesPage, controllers.routes.PenaltySelectionController.onPageLoadForPenaltySelection(NormalMode).url)
        )
      )

      s"the user is on the $AppealStartPage" must {
        "route the user back to the 'You can appeal this penalty' page when appealing against obligation" in {
          val result: Call = mainNavigator.previousPage(AppealStartPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.isObligationAppeal -> "true")))
          result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
        }

        "route back to the penalties and appeals page when its not an obligation appeal" in {
          val result: Call = mainNavigator.previousPage(AppealStartPage, NormalMode)(fakeRequestConverter(fakeRequest))
          result.url shouldBe "http://localhost:9180/penalties"
        }
      }

      s"the user is on the $HonestyDeclarationPage" must {
        "route the user back to the 'Appeal a VAT penalty' page when appealing against obligation" in {
          val result: Call = mainNavigator.previousPage(HonestyDeclarationPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.isObligationAppeal -> "true")))
          result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
        }

        "route back to the reasonable excuse selection page when not appealing against obligation" in {
          val result: Call = mainNavigator.previousPage(HonestyDeclarationPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "health")))
          result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        }
      }

      s"the user is on the $UploadEvidenceQuestionPage" must {
        "route the user back to the 'Other relevant information' page when appealing against obligation" in {
          val result: Call = mainNavigator.previousPage(UploadEvidenceQuestionPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.isObligationAppeal -> "true")))
          result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
        }

        "route back to the why return submitted late page when not appealing against obligation" in {
          val result: Call = mainNavigator.previousPage(UploadEvidenceQuestionPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "health")))
          result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
        }
      }

      s"the user is on the $MakingALateAppealPage" must {
        def makingALateAppealReverseNormalRouteTest(reason: String, expectedUrl: String, userRequest: UserRequest[_]): Unit = {
          s"route the user back to the correct page when appealing with $reason reason" in {
            val result: Call = mainNavigator.previousPage(MakingALateAppealPage, NormalMode)(userRequest)
            result.url shouldBe expectedUrl
          }
        }
        makingALateAppealReverseNormalRouteTest("bereavement",
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "bereavement")))

        makingALateAppealReverseNormalRouteTest("crime",
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "crime")))

        makingALateAppealReverseNormalRouteTest("fireOrFlood",
          controllers.routes.FireOrFloodReasonController.onPageLoad(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "fireOrFlood")))

        makingALateAppealReverseNormalRouteTest("health",
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "health",
            SessionKeys.wasHospitalStayRequired -> "yes")))

        makingALateAppealReverseNormalRouteTest("health - no hospital stay",
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "health")))

        makingALateAppealReverseNormalRouteTest("lossOfStaff",
          controllers.routes.LossOfStaffReasonController.onPageLoad(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "lossOfStaff")))

        makingALateAppealReverseNormalRouteTest("technicalIssues",
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "technicalIssues")))

        makingALateAppealReverseNormalRouteTest("other",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "other",
            SessionKeys.isUploadEvidence -> "yes")))

        makingALateAppealReverseNormalRouteTest("other - no upload",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.reasonableExcuse -> "other")))

        makingALateAppealReverseNormalRouteTest("obligation",
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url,
          fakeRequestConverter(fakeRequest.withSession(SessionKeys.isObligationAppeal -> "true")))

      }

      s"the user is on the $ReasonableExcuseSelectionPage" when {
        "the user is an agent and appealing an LSP" must {
          "route back to the 'what caused you to miss the deadline' page if the agent planned to submit" in {
            val result: Call = mainNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode)(agentFakeRequestConverter(fakeRequest.withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent"
            )))
            result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode).url
          }

          "route back to the 'who planned to submit return' page if the client planned to submit" in {
            val result: Call = mainNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode)(agentFakeRequestConverter(fakeRequest.withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.whoPlannedToSubmitVATReturn -> "client"
            )))
            result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode).url
          }
        }

        "the user is not agent or not appealing an LSP as agent" must {
          "route back to the landing page" in {
            val result: Call = mainNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode)(fakeRequestConverter(fakeRequest))
            result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
          }
        }
      }

      s"the user is on the $CheckYourAnswersPage" when {
        "the user is appealing late" must {
          "route back to the 'making a late appeal' page if the agent planned to submit" in new Setup {
            when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
              2020, 4, 1))
            val result: Call = mainNavigator.previousPage(CheckYourAnswersPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
            )
            result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          }

          "route back to the previous page before making a late appeal if the client planned to submit" in new Setup {
            val result: Call = mainNavigator.previousPage(CheckYourAnswersPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
            result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
          }
        }
      }

      "route the user back to the CYA page if they originated on this page" in {
        val result: Call = mainNavigator.previousPage(ReasonableExcuseSelectionPage, NormalMode)(fakeRequestConverter(fakeRequest.withSession(SessionKeys.originatingChangePage -> ReasonableExcuseSelectionPage.toString)))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

  }

  "nextPage" should {
    "in CheckMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result: Call = mainNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
      }

      s"called with $WasHospitalStayRequiredPage and answer for 'WhenDidHealthIssueHappenPage' is set" in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whenHealthIssueHappened -> "2020-01-01"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidBecomeUnablePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whenDidBecomeUnable -> "2020-01-01"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhyWasReturnSubmittedLatePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whyReturnSubmittedLate -> "this is a good reason"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $EvidencePage" in new Setup {
        val result: Call = mainNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage" in new Setup {
        val result: Call = mainNavigator.nextPage(DidHospitalStayEndPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidHospitalStayBeginPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage - user selected 'client' and now changes to agent" in new Setup {
        val result: Call = mainNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, CheckMode, Some("agent"))(
          fakeRequestConverter(fakeRequestWithCorrectKeys
            .withSession(
              SessionKeys.whoPlannedToSubmitVATReturn -> "client"
            )))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "crime"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "lossOfStaff"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "fireOrFlood"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "health"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "other"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(DidHospitalStayEndPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "health"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, CheckMode, Some("client"))(
          fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $ReasonableExcuseSelectionPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(ReasonableExcuseSelectionPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidThePersonDiePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidThePersonDiePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "bereavement"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }
      s"called with $OtherRelevantInformationPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(None, CheckMode)
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
      }
      s"called with $UploadFirstDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadFirstDocumentPage, CheckMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
      }

      s"called with $UploadAnotherDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadAnotherDocumentPage, CheckMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(CheckMode).url
      }

      s"called with $FileListPage redirects to $UploadAnotherDocumentPage when the user selects yes" in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, CheckMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(CheckMode).url
      }

      s"called with $FileListPage redirects to CYA page when the user selects no" in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, CheckMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded " in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, CheckMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
        ))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url
      }

      s"called with $AppealSinglePenaltyPage" in new Setup {
        val result: Call = mainNavigator.nextPage(AppealSinglePenaltyPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $AppealCoverBothPenaltiesPage" in new Setup {
        val result: Call = mainNavigator.nextPage(AppealCoverBothPenaltiesPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects yes" in new Setup {
        val result: Call = mainNavigator.nextPage(PenaltySelectionPage, CheckMode, Some("yes"))(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(CheckMode).url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects no" in new Setup {
        val result: Call = mainNavigator.nextPage(PenaltySelectionPage, CheckMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(CheckMode).url
      }
    }

    "in NormalMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result: Call = mainNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidCrimeHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
      }

      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage" in new Setup {
        val result: Call = mainNavigator.nextPage(DidHospitalStayEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidHospitalStayBeginPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhenDidBecomeUnablePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhyWasReturnSubmittedLatePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(NormalMode).url
      }

      s"called with $UploadEvidenceQuestionPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage" in new Setup {
        val result: Call = mainNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "lossOfStaff"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidTechnologyIssuesEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "technicalIssues"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "fireOrFlood"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "health"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "other"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(DidHospitalStayEndPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "health"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhatCausedYouToMissTheDeadlinePage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhatCausedYouToMissTheDeadlinePage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whatCausedYouToMissTheDeadline -> "client"
          )))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage - selects client" in new Setup {
        val result: Call = mainNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, NormalMode, Some("client"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage" in new Setup {
        val result: Call = mainNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, NormalMode, Some("agent"))(
          fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent"
          )))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(NormalMode).url
      }

      s"called with $WhenDidThePersonDiePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
          2020, 4, 1))
        val result: Call = mainNavigator.nextPage(WhenDidThePersonDiePage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "bereavement"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $HonestyDeclarationPage" in new Setup {
        val result: Call = mainNavigator.nextPage(HonestyDeclarationPage, NormalMode, Some("crime"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.hasConfirmedDeclaration -> "true"
          )))
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      }

      s"called with $HonestyDeclarationPage isObligation is true" in new Setup {

        val result: Call = mainNavigator.nextPage(HonestyDeclarationPage, NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.hasConfirmedDeclaration -> "true"
          )))
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
      }

      s"called with $EvidencePage isObligation is true" in new Setup {
        val result: Call = mainNavigator.nextPage(EvidencePage, NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.isObligationAppeal -> "true"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadFirstDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadFirstDocumentPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
      }

      s"called with $UploadAnotherDocumentPage redirects to File Upload List Page" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadAnotherDocumentPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
      }

      s"called with $FileListPage redirects to $UploadAnotherDocumentPage when the user selects yes" in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      }

      s"called with $FileListPage redirect making a late appeal page - when the user has 5 documents uploaded and appeal > 30 days late and user answers no" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(FileListPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded and user answers no" in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, NormalMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys
        ))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $FileListPage redirect making a late appeal page - when the user has 5 documents uploaded and appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(FileListPage, NormalMode)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $FileListPage redirects CYA page - when the user has 5 documents uploaded " in new Setup {
        val result: Call = mainNavigator.nextPage(FileListPage, NormalMode)(fakeRequestConverter(fakeRequestWithCorrectKeys
        ))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
      }

      s"called with $UploadEvidenceQuestionPage - user answer no (non late appeal)" in new Setup {
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $UploadEvidenceQuestionPage - user answer no (late appeal)" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      s"called with $YouCanAppealThisPenaltyPage - user answer yes (route to appeal start page - obligation)" in new Setup {
        val result: Call = mainNavigator.nextPage(YouCanAppealThisPenaltyPage, NormalMode, Some("yes"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("obligation")
        )
        result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
      }

      s"called with $YouCanAppealThisPenaltyPage - user answer no (route to penalties and appeals page)" in new Setup {
        val result: Call = mainNavigator.nextPage(YouCanAppealThisPenaltyPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("obligation")
        )
        result.url shouldBe "http://localhost:9180/penalties"
      }

      s"called with $AppealSinglePenaltyPage" in new Setup {
        val result: Call = mainNavigator.nextPage(AppealSinglePenaltyPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $AppealCoverBothPenaltiesPage" in new Setup {
        val result: Call = mainNavigator.nextPage(AppealCoverBothPenaltiesPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects yes" in new Setup {
        val result: Call = mainNavigator.nextPage(PenaltySelectionPage, NormalMode, Some("yes"))(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode).url
      }

      s"called with $PenaltySelectionPage - redirects to single appeal page when user selects no" in new Setup {
        val result: Call = mainNavigator.nextPage(PenaltySelectionPage, NormalMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys))
        result.url shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode).url
      }
    }
  }

  "routingForHospitalStay" should {
    "redirect to the appropriate page in normal mode" when {
      "user answers no " in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
        reset(mockDateTimeHelper)
      }

      "user answers yes" in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("yes"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url
        reset(mockDateTimeHelper)
      }
    }

    "redirect to the CYA page in check mode" when {
      "user answers no and the question on the next page has already been answered" in new Setup {
        val result: Call = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
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

        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.lateAppealReason -> "This is a reason.",
            SessionKeys.appealType -> "crime"
          )), CheckMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      "the appeal is not late" in new Setup {
        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "crime"
          )), NormalMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "route to late appeal reason page" when {
      "the appeal is late and there is no existing reason as to why" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "crime"
          )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "appealing multiple penalties and the LPP1 communication date is more than 30 days ago" in new Setup {
        when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(2020, 4, 1))

        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
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

        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
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

        val result: Call = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
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
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("bereavement"), NormalMode)
        result.url shouldBe controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(NormalMode).url
      }
      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("crime"), NormalMode)
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(NormalMode).url
      }
      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("fireOrFlood"), NormalMode)
        result.url shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(NormalMode).url
      }
      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("technicalIssues"), NormalMode)
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(NormalMode).url
      }
      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("health"), NormalMode)
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(NormalMode).url
      }
      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(Some("other"), NormalMode)
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(NormalMode).url
      }
      s"called with $OtherRelevantInformationPage" in new Setup {
        val result: Call = mainNavigator.getNextURLBasedOnReasonableExcuse(None, NormalMode)
        result.url shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(NormalMode).url
      }
    }
  }

  "routingForCancelVATRegistrationPage" when {

    "redirect to AppealStart page" when {
      "yes option selected" in new Setup {
        val result: Call = mainNavigator.routingForCancelVATRegistrationPage(Some("yes"))
        result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
      }
    }

    "redirect to YouCannotAppeal page" when {
      "no option selected" in new Setup {
        val result: Call = mainNavigator.routingForCancelVATRegistrationPage(Some("no"))
        result.url shouldBe controllers.routes.YouCannotAppealController.onPageLoad.url
      }
    }

  }

  "routeForYouCanAppealPenalty" should {
    "redirect to the appeal start page when the user selects yes" in new Setup {
      val result: Call = mainNavigator.routeForYouCanAppealPenalty(Some("yes"))
      result.url shouldBe controllers.routes.AppealStartController.onPageLoad().url
    }

    "redirect to the penalties and appeals page when the user selects no" in new Setup {
      val result: Call = mainNavigator.routeForYouCanAppealPenalty(Some("no"))
      result.url shouldBe appConfig.penaltiesFrontendUrl
    }

    "reload the page when the matching fails" in new Setup {
      val result: Call = mainNavigator.routeForYouCanAppealPenalty(Some("blah"))
      result.url shouldBe controllers.routes.YouCanAppealPenaltyController.onPageLoad().url
    }
  }

  "routeForUploadEvidenceQuestion" should {
    "redirect to Upload Evidence page in normal mode" when {
      "user answers yes for Upload Evidence Question " in new Setup {
        val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("yes"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
        )
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
      }

      "redirect to CYA/making a late appeal page in Normal Mode" when {
        "user answers no for Upload Evidence Question" in new Setup {
          val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }

        "user answers no for Upload Evidence Question - late appeal" in new Setup {
          when(mockDateTimeHelper.dateNow).thenReturn(LocalDate.of(
            2020, 4, 1))
          val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, NormalMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }
      }

      "redirect to the Upload Evidence page in Check Mode" when {
        "user answers yes for Upload Evidence Question" in new Setup {
          val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, Some("yes"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }
      }

      "redirect to the CYA page in Check Mode" when {
        "user answers no for Upload Evidence Question" in new Setup {
          val result: Call = mainNavigator.nextPage(UploadEvidenceQuestionPage, CheckMode, Some("no"))(
            fakeRequestWithCorrectKeysAndReasonableExcuseSet("other")
          )
          result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        }
      }
    }
  }

  "routingForPenaltySelectionPage" should {
    "redirect to the single appeal page" when {
      "user selects no" in new Setup{
        val result: Call = mainNavigator.routingForPenaltySelectionPage(Some("no"), NormalMode)
        result shouldBe controllers.routes.PenaltySelectionController.onPageLoadForSinglePenaltySelection(NormalMode)
      }
    }

    "redirect to the multiple appeal page" when {
      "the user selects yes" in {
        val result: Call = mainNavigator.routingForPenaltySelectionPage(Some("yes"), NormalMode)
        result shouldBe controllers.routes.PenaltySelectionController.onPageLoadForAppealCoverBothPenalties(NormalMode)
      }
    }
  }
}
