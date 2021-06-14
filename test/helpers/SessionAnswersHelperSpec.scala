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

package helpers

import base.SpecBase
import models.{CheckMode, NormalMode}
import utils.SessionKeys

class SessionAnswersHelperSpec extends SpecBase {
  "isAllAnswerPresentForReasonableExcuse" should {
    "for crime" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "crime",
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(fakeRequestWithAllCrimeKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(fakeRequestWithAllCrimeKeysPresent)
        result shouldBe false
      }
    }

    "for fire or flood" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent)
        result shouldBe false
      }
    }
  }

  "getContentForReasonableExcuseCheckYourAnswersPage" should {
    "for crime" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "crime",
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("crime")(fakeRequestWithAllCrimeKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Crime"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the crime happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url
        result(2)._1 shouldBe "Has this crime been reported to the police?"
        result(2)._2 shouldBe "Yes"
        result(2)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url
      }

      "return all keys and the 'Why you did not appeal sooner' text" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "crime",
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("crime")(fakeRequestWithAllCrimeKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Crime"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the crime happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url
        result(2)._1 shouldBe "Has this crime been reported to the police?"
        result(2)._2 shouldBe "Yes"
        result(2)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url
        result(3)._1 shouldBe "Why you did not appeal sooner"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }

    "for fire or flood" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
          )
        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Fire or flood"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url
      }

      "return all keys and the 'Why you did not appeal sooner' text" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          )
        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Fire or flood"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url
        result(2)._1 shouldBe "Why you did not appeal sooner"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }

    "for loss of staff" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Loss of staff essential to the VAT process"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the person become unavailable?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url
      }

      "return all keys and the 'Why you did not appeal sooner' text" in {
        val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Loss of staff essential to the VAT process"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the person become unavailable?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url
        result(2)._1 shouldBe "Why you did not appeal sooner"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }
  }
}
