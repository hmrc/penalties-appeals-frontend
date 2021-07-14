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

package navigation

import java.time.LocalDateTime

import base.SpecBase
import models.pages._
import models.{CheckMode, NormalMode}
import org.mockito.Mockito._
import utils.SessionKeys

class NavigationSpec extends SpecBase {

  class Setup {
    reset(mockDateTimeHelper)
    when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 2, 1, 0, 0, 0))
  }

  "nextPage" should {
    "in CheckMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result = mainNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidCrimeHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))

        //TODO: may change based on UX
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
      }

      s"called with $WasHospitalStayRequiredPage and answer for 'WhenDidHealthIssueHappenPage' is set" in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.whenHealthIssueHappened -> "2020-01-01")
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }


      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidBecomeUnablePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whenDidBecomeUnable -> "2020-01-01"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad.url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result = mainNavigator.nextPage(WhyWasReturnSubmittedLatePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whyReturnSubmittedLate -> "this is a good reason"
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad.url
      }

      s"called with $EvidencePage" in new Setup {
        val result = mainNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage" in new Setup {
        val result = mainNavigator.nextPage(DidHospitalStayEndPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidHospitalStayBeginPage, CheckMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(HasCrimeBeenReportedPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "crime")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late AND late appeal reason hasn't been entered" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "lossOfStaff")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "fireOrFlood")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "health")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(EvidencePage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "other"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020,4,1,0,0,0))
        val result = mainNavigator.nextPage(DidHospitalStayEndPage, CheckMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
        .withSession(
          SessionKeys.appealType -> "health"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }
    }

    "in NormalMode" when {
      s"called with $HasCrimeBeenReportedPage" in new Setup {
        val result = mainNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidCrimeHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidCrimeHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(NormalMode).url
      }

      s"called with $WhenDidTechnologyIssuesEndPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidFireOrFloodHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("fireOrFlood"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WasHospitalStayRequiredPage" in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
      }

      s"called with $WhenDidHealthIssueHappenPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $DidHospitalStayEndPage" in new Setup {
        val result = mainNavigator.nextPage(DidHospitalStayEndPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidHospitalStayBeginPage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidHospitalStayBeginPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("health"))
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(NormalMode).url
      }

      s"called with $WhenDidBecomeUnablePage" in new Setup {
        val result = mainNavigator.nextPage(WhenDidBecomeUnablePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      }

      s"called with $WhyWasReturnSubmittedLatePage" in new Setup {
        val result = mainNavigator.nextPage(WhyWasReturnSubmittedLatePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
      }

      s"called with $EvidencePage" in new Setup {
        val result = mainNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("other"))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $HasCrimeBeenReportedPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("crime"))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidPersonLeaveTheBusinessPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidPersonLeaveTheBusinessPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "lossOfStaff")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidTechnologyIssuesEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesEndPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "technicalIssues")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidFireOrFloodHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidFireOrFloodHappenPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "fireOrFlood")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhenDidHealthIssueHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(WhenDidHealthIssueHappenPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "health")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $EvidencePage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(EvidencePage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.appealType -> "other"
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $DidHospitalStayEndPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020,4,1,0,0,0))
        val result = mainNavigator.nextPage(DidHospitalStayEndPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
        .withSession(
          SessionKeys.appealType -> "health"
        )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

      s"called with $WhyWasTheReturnSubmittedLateAgentPage when the appeal > 30 days late" in new Setup {
        val result = mainNavigator.nextPage(WhyWasTheReturnSubmittedLateAgentPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.causeOfLateSubmissionAgent -> "causeOfLateSubmissionAgent"
          )))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      }

      s"called with $WhoPlannedToSubmitVATReturnAgentPage when the appeal > 30 days late" in new Setup {
        val result = mainNavigator.nextPage(WhoPlannedToSubmitVATReturnAgentPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent"
          )))
        result.url shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(NormalMode).url
      }

      s"called with $ReasonableExcuseSelectionPage when the appeal > 30 days late" in new Setup {
        val result = mainNavigator.nextPage(ReasonableExcuseSelectionPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )))
        result.url shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      }
    }
  }

  "routingForHospitalStay" should {
    "redirect to the appropriate page in normal mode" when {
      "user answers no " in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("no"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(NormalMode).url
        reset(mockDateTimeHelper)
      }

      // TODO - update when hospital stay was required journey is added, currently reloads the page
      "user answers yes" in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, NormalMode, Some("yes"))(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("health")
        )
        result.url shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(NormalMode).url
        reset(mockDateTimeHelper)
      }
    }

    "redirect to the CYA page in check mode" when {
      "user answers no and the question on the next page has already been answered" in new Setup {
        val result = mainNavigator.nextPage(WasHospitalStayRequiredPage, CheckMode, Some("no"))(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.whenHealthIssueHappened -> "2020-01-01")
          )))
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
        reset(mockDateTimeHelper)
      }

    }
  }

  "routeToMakingALateAppealOrCYAPage" should {
    "route to CYA page" when {
      "the appeal is late but the reason has already been given and we are in CheckMode" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))

        val result = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.lateAppealReason -> "This is a reason."),
            (SessionKeys.appealType -> "crime")
          )), CheckMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      "the appeal is not late" in new Setup {
        val result = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "crime")
          )), NormalMode)

        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "route to late appeal reason page" when {
      "the appeal is late and there is no existing reason as to why" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))

        val result = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "crime")
          )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "the appeal is late but the reason has already been given and we are in NormalMode" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))

        val result = mainNavigator.routeToMakingALateAppealOrCYAPage(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.lateAppealReason -> "This is a reason."),
            (SessionKeys.appealType -> "crime")
          )), NormalMode)

        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }
  }
}
