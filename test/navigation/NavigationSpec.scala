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

import base.SpecBase
import models.{CheckMode, NormalMode}
import models.pages._
import org.mockito.Mockito._
import utils.SessionKeys

import java.time.LocalDateTime

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
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
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

      //TODO: implement when the 'When did the technology issues end' page is implemented
//      s"called with $WhenDidTechnologyIssuesBeginPage" in new Setup {
//        val result = mainNavigator.nextPage(WhenDidTechnologyIssuesBeginPage, NormalMode, None)(fakeRequestWithCorrectKeysAndReasonableExcuseSet("lossOfStaff"))
//        result.url shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded().url
//      }

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

      s"called with $WhenDidCrimeHappenPage when the appeal > 30 days late" in new Setup {
        when(mockDateTimeHelper.dateTimeNow).thenReturn(LocalDateTime.of(2020, 4, 1, 0, 0, 0))
        val result = mainNavigator.nextPage(HasCrimeBeenReportedPage, NormalMode, None)(fakeRequestConverter(fakeRequestWithCorrectKeys
          .withSession(
            (SessionKeys.appealType -> "lossOfStaff")
          )))
        result.url shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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
