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
import models.CheckMode
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
        val fakeRequestWithSomeCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(fakeRequestWithSomeCrimeKeysPresent)
        result shouldBe false
      }
    }

    "for loss of staff" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeLossOfStaffKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(fakeRequestWithSomeLossOfStaffKeysPresent)
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

    "for technical issues" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeTechnicalIssuesKeysPresent = fakeRequest
          .withSession(
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(fakeRequestWithSomeTechnicalIssuesKeysPresent)
        result shouldBe false
      }
    }

    "for health" must {
      "return true" when {
        "the keys are present for no hospital stay journey" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllNonHospitalStayKeysPresent)
          result shouldBe true
        }

        "the keys are present for ongoing hospital stay journey" in {
          val fakeRequestWithAllOngoingHospitalStayKeysPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "yes",
              SessionKeys.hasHealthEventEnded -> "no",
              SessionKeys.whenHealthIssueStarted -> "2022-01-01"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllOngoingHospitalStayKeysPresent)
          result shouldBe true
        }

        "the keys are present for an ended hospital stay" in {
          val fakeRequestWithAllEndedHospitalStayKeysPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "yes",
              SessionKeys.hasHealthEventEnded -> "yes",
              SessionKeys.whenHealthIssueStarted -> "2022-01-01",
              SessionKeys.whenHealthIssueEnded -> "2022-01-02"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllEndedHospitalStayKeysPresent)
          result shouldBe true
        }
      }

      "return false" when {
        "there was a hospital stay but there event ongoing question hasn't been answered" in {
          val fakeRequestWithNoEventOngoingKeyPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "yes",
              SessionKeys.whenHealthIssueStarted -> "2022-01-01"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
          result shouldBe false
        }

        "the hospital stay question hasn't been answered" in {
          val fakeRequestWithNoEventOngoingKeyPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.hasHealthEventEnded -> "yes",
              SessionKeys.whenHealthIssueStarted -> "2022-01-01"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
          result shouldBe false
        }

        "there is an ongoing hospital stay but no startDate has been provided" in {
          val fakeRequestWithNoEventOngoingKeyPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "yes",
              SessionKeys.hasHealthEventEnded -> "no"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
          result shouldBe false
        }

        "there is a hospital stay that has ended but no end date has been provided" in {
          val fakeRequestWithNoEventOngoingKeyPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "yes",
              SessionKeys.hasHealthEventEnded -> "yes",
              SessionKeys.whenHealthIssueStarted -> "2022-01-01"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
          result shouldBe false
        }

        "not all keys are present" in {
          val fakeRequestWithSomeHealthKeysPresent = fakeRequest
            .withSession(
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
              SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
            )
          val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithSomeHealthKeysPresent)
          result shouldBe false
        }
      }
    }

    "for other" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllOtherReasonKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(fakeRequestWithAllOtherReasonKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeOtherReasonKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01"
          )
        val result = SessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(fakeRequestWithSomeOtherReasonKeysPresent)
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

    "for technical issues" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Technology issues"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the technology issues begin?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url
        result(2)._1 shouldBe "When did the technology issues end?"
        result(2)._2 shouldBe "2 January 2022"
        result(2)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
      }

      "return all keys and the 'Why you did not appeal sooner' text" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Technology issues"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the technology issues begin?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url
        result(2)._1 shouldBe "When did the technology issues end?"
        result(2)._2 shouldBe "2 January 2022"
        result(2)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
        result(3)._1 shouldBe "Why you did not appeal sooner"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }

    "for health" must {
      "for no hospital stay" should {
        "return all the keys from the session ready to be passed to the view" in {
          val fakeRequestWithNoHospitalStayKeysPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01"
            )

          val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "Health"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "Did this health issue include an unexpected hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
          result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
        }

        "return all keys and the 'Why you did not appeal sooner' text" in {
          val fakeRequestWithNoHospitalStayKeysPresent = fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01",
              SessionKeys.lateAppealReason -> "Lorem ipsum"
            )

          val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "Health"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "Did this health issue include an unexpected hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
          result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
          result(3)._1 shouldBe "Why you did not appeal sooner"
          result(3)._2 shouldBe "Lorem ipsum"
          result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }
      }
    }

    "for other" must {
      "for no upload" in {
        val fakeRequestWithOtherNoUploadKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherNoUploadKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        //TODO: may need to change with default message
        result(3)._2 shouldBe ""
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }

      "for no upload - and late appeal" in {
        val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        //TODO: may need to change with default message
        result(3)._2 shouldBe ""
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        result(4)._1 shouldBe "Why you did not appeal sooner"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }

      "for upload" in {
        val fakeRequestWithNoLateAppealButUploadPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.evidenceFileName -> "file.docx"
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        //TODO: may need to change with default message
        result(3)._2 shouldBe "file.docx"
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }

      "for upload - and late appeal" in {
        val fakeRequestWithNoLateAppealButUploadPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.evidenceFileName -> "file.docx",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
          )

        val result = SessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        //TODO: may need to change with default message
        result(3)._2 shouldBe "file.docx"
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        result(4)._1 shouldBe "Why you did not appeal sooner"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }
  }

  "getHealthReasonAnswers" must {
    "when there is no hospital stay" should {
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithAllNonHospitalStayKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "no",
            SessionKeys.whenHealthIssueHappened -> "2022-01-01"
        )

        val result = SessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Health"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "Did this health issue include an unexpected hospital stay?"
        result(1)._2 shouldBe "No"
        result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
        result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
      }
    }
  }
}
