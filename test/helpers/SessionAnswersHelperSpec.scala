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

package helpers

import base.SpecBase
import models.pages._
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import org.mockito.Mockito.{mock, when}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import utils.SessionKeys
import java.time.{LocalDate, LocalDateTime}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionAnswersHelperSpec extends SpecBase {
  val mockRepository: UploadJourneyRepository = mock(classOf[UploadJourneyRepository])
  val sessionAnswersHelper = new SessionAnswersHelper(mockRepository)

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
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(fakeRequestWithAllCrimeKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeCrimeKeysPresent = fakeRequest
          .withSession(
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("crime")(fakeRequestWithSomeCrimeKeysPresent)
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
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeLossOfStaffKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("lossOfStaff")(fakeRequestWithSomeLossOfStaffKeysPresent)
        result shouldBe false
      }
    }

    "for fire or flood" must {
      "return true - when all keys present" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent)
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
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeTechnicalIssuesKeysPresent = fakeRequest
          .withSession(
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("technicalIssues")(fakeRequestWithSomeTechnicalIssuesKeysPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllNonHospitalStayKeysPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllOngoingHospitalStayKeysPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithAllEndedHospitalStayKeysPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
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
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithNoEventOngoingKeyPresent)
          result shouldBe false
        }

        "not all keys are present" in {
          val fakeRequestWithSomeHealthKeysPresent = fakeRequest
            .withSession(
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
              SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
            )
          val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("health")(fakeRequestWithSomeHealthKeysPresent)
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
            SessionKeys.isUploadEvidence -> "yes"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(fakeRequestWithAllOtherReasonKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithSomeOtherReasonKeysPresent = fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01"
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("other")(fakeRequestWithSomeOtherReasonKeysPresent)
        result shouldBe false
      }
    }
  }

  "getContentForReasonableExcuseCheckYourAnswersPage" should {
    "for crime" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "crime",
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("crime")(fakeRequestWithAllCrimeKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Crime"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the crime happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
          WhenDidCrimeHappenPage.toString
        ).url
        result(2)._1 shouldBe "Has this crime been reported to the police?"
        result(2)._2 shouldBe "Yes"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
          HasCrimeBeenReportedPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val fakeRequestWithAllCrimeKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "crime",
            SessionKeys.hasCrimeBeenReportedToPolice -> "yes",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.dateOfCrime -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("crime")(fakeRequestWithAllCrimeKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Crime"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the crime happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url,
          WhenDidCrimeHappenPage.toString
        ).url
        result(2)._1 shouldBe "Has this crime been reported to the police?"
        result(2)._2 shouldBe "Yes"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url,
          HasCrimeBeenReportedPage.toString
        ).url
        result(3)._1 shouldBe "Reason for appealing after 30 days"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for fire or flood" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true"
          ))
        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Fire or flood"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
          WhenDidFireOrFloodHappenPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))
        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Fire or flood"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url,
          WhenDidFireOrFloodHappenPage.toString
        ).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for loss of staff" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Loss of staff essential to the VAT process"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the person leave the business?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
          WhenDidPersonLeaveTheBusinessPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "lossOfStaff",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenPersonLeftTheBusiness -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("lossOfStaff")(fakeRequestWithAllLossOfStaffKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Loss of staff essential to the VAT process"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the person leave the business?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url,
          WhenDidPersonLeaveTheBusinessPage.toString
        ).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for technical issues" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Technology issues"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the technology issues begin?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
          WhenDidTechnologyIssuesBeginPage.toString
        ).url
        result(2)._1 shouldBe "When did the technology issues end?"
        result(2)._2 shouldBe "2 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
          WhenDidTechnologyIssuesEndPage.toString
        ).url
      }

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Technology issues"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the technology issues begin?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url,
          WhenDidTechnologyIssuesBeginPage.toString
        ).url
        result(2)._1 shouldBe "When did the technology issues end?"
        result(2)._2 shouldBe "2 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url,
          WhenDidTechnologyIssuesEndPage.toString
        ).url
        result(3)._1 shouldBe "Reason for appealing after 30 days"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "for LPP on other journey" must {
      "display the correct wording for trader" in {
        val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT payment was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
            SessionKeys.isUploadEvidence -> "no"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "The reason does not fit into any of the other categories"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the issue first stop you paying the VAT bill?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
        result(2)._1 shouldBe "Why was the VAT bill paid late?"
        result(2)._2 shouldBe "This is why my VAT payment was late."
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
        result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(3)._2 shouldBe "No"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4)._1 shouldBe "Reason for appealing after 30 days"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "when an agent is on the page" should {
      "for health" must {
        "for no hospital stay" should {
          "return all the keys from the session ready to be passed to the view" in {
            val fakeRequestWithNoHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
              .withSession(
                SessionKeys.reasonableExcuse -> "health",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.wasHospitalStayRequired -> "no",
                SessionKeys.whenHealthIssueHappened -> "2022-01-01",
                SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
                SessionKeys.whatCausedYouToMissTheDeadline -> "client"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result.head._1 shouldBe "Reason for missing the VAT deadline"
            result.head._2 shouldBe "Health"
            result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
            result(2)._1 shouldBe "When did the health issue first stop your client getting information to you?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
          }

          "return all keys and the 'Reason for appealing after 30 days' text" in {
            val fakeRequestWithNoHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
              .withSession(
                SessionKeys.reasonableExcuse -> "health",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.wasHospitalStayRequired -> "no",
                SessionKeys.whenHealthIssueHappened -> "2022-01-01",
                SessionKeys.lateAppealReason -> "Lorem ipsum",
                SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
                SessionKeys.whatCausedYouToMissTheDeadline -> "client"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result.head._1 shouldBe "Reason for missing the VAT deadline"
            result.head._2 shouldBe "Health"
            result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
            result(2)._1 shouldBe "When did the health issue first stop your client getting information to you?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
            result(3)._1 shouldBe "Reason for appealing after 30 days"
            result(3)._2 shouldBe "Lorem ipsum"
            result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.MakingALateAppealController.onPageLoad().url,
              MakingALateAppealPage.toString
            ).url
          }
        }
      }

      "for other" must {
        "display the correct wording for agent" when {
          "the client planned to submit" in {
            val request = agentFakeRequestConverter(agentRequest
              .withSession(
                SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
                SessionKeys.reasonableExcuse -> "other",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.whyReturnSubmittedLate -> "This is why my VAT payment was late.",
                SessionKeys.whenDidBecomeUnable -> "2022-01-01",
                SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
                SessionKeys.isUploadEvidence -> "no",
                SessionKeys.whoPlannedToSubmitVATReturn -> "client"
              ))
            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
              "other")(request, implicitly)
            result(1)._1 shouldBe "When did the issue first stop your client submitting the VAT Return?"
            result(1)._2 shouldBe "1 January 2022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }

          "the agent planned to submit and client missed deadline" in {
            val request = agentFakeRequestConverter(agentRequest
              .withSession(
                SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
                SessionKeys.reasonableExcuse -> "other",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.whyReturnSubmittedLate -> "This is why my VAT payment was late.",
                SessionKeys.whenDidBecomeUnable -> "2022-01-01",
                SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
                SessionKeys.isUploadEvidence -> "no",
                SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
                SessionKeys.whatCausedYouToMissTheDeadline -> "client"
              ))
            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
              "other")(request, implicitly)
            result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
            result(1)._2 shouldBe "1 January 2022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }

          "the agent planned to submit and missed deadline" in {
            val request = agentFakeRequestConverter(agentRequest
              .withSession(
                SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
                SessionKeys.reasonableExcuse -> "other",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.whyReturnSubmittedLate -> "This is why my VAT payment was late.",
                SessionKeys.whenDidBecomeUnable -> "2022-01-01",
                SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
                SessionKeys.isUploadEvidence -> "no",
                SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
                SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
              ))
            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
              "other")(request, implicitly)
            result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
            result(1)._2 shouldBe "1 January 2022"
            controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
              WhenDidBecomeUnablePage.toString
            ).url
          }
        }

        "for no upload" in {
          val fakeRequestWithOtherNoUploadKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.whatCausedYouToMissTheDeadline -> "client",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other")(fakeRequestWithOtherNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "Not provided"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for no upload - and late appeal" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.whatCausedYouToMissTheDeadline -> "client",
              SessionKeys.isUploadEvidence -> "no"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "No"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "for upload" in {
          val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.whatCausedYouToMissTheDeadline -> "client",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "file.docx"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for upload - and late appeal" in {
          val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.whatCausedYouToMissTheDeadline -> "client",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "file.docx"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(5)._1 shouldBe "Reason for appealing after 30 days"
          result(5)._2 shouldBe "This is the reason why my appeal was late."
          result(5)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }
      }

      "for LPP" in {
        val fakeRequestWithLPPKeysPresent = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT bill was paid late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
            SessionKeys.isUploadEvidence -> "yes"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "other", Some("file.docx"))(fakeRequestWithLPPKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "The reason does not fit into any of the other categories"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the issue first stop your client paying the VAT bill?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
        result(2)._1 shouldBe "Why was the VAT bill paid late?"
        result(2)._2 shouldBe "This is why my VAT bill was paid late."
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
        result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(3)._2 shouldBe "Yes"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4)._1 shouldBe "Evidence to support this appeal"
        result(4)._2 shouldBe "file.docx"
        result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        result(5)._1 shouldBe "Reason for appealing after 30 days"
        result(5)._2 shouldBe "This is the reason why my appeal was late."
        result(5)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }

      "display no upload details row if the user has uploaded files but has changed their mind and selects 'no' - 'hide' the files uploaded" in {
        val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "client",
            SessionKeys.isUploadEvidence -> "no"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "The reason does not fit into any of the other categories"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
        result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(3)._2 shouldBe "No"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4)._1 shouldBe "Reason for appealing after 30 days"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }

      "display no upload details row if the user selected no to uploading files" in {
        val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "client",
            SessionKeys.isUploadEvidence -> "no"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "other")(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "The reason does not fit into any of the other categories"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the issue first stop your client getting information to you?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is why my VAT return was late."
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
        result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(3)._2 shouldBe "No"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4)._1 shouldBe "Reason for appealing after 30 days"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }

    "when a VAT trader is on the page" should {
      "for health" must {
        "for no hospital stay" should {
          "return all the keys from the session ready to be passed to the view" in {
            val fakeRequestWithNoHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
              .withSession(
                SessionKeys.reasonableExcuse -> "health",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.wasHospitalStayRequired -> "no",
                SessionKeys.whenHealthIssueHappened -> "2022-01-01"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
              "health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result.head._1 shouldBe "Reason for missing the VAT deadline"
            result.head._2 shouldBe "Health"
            result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
            result(2)._1 shouldBe "When did the health issue first stop you submitting the VAT Return?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
          }

          "return all keys and the 'Reason for appealing after 30 days' text" in {
            val fakeRequestWithNoHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
              .withSession(
                SessionKeys.reasonableExcuse -> "health",
                SessionKeys.hasConfirmedDeclaration -> "true",
                SessionKeys.wasHospitalStayRequired -> "no",
                SessionKeys.whenHealthIssueHappened -> "2022-01-01",
                SessionKeys.lateAppealReason -> "Lorem ipsum"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
              "health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result.head._1 shouldBe "Reason for missing the VAT deadline"
            result.head._2 shouldBe "Health"
            result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
            result(2)._1 shouldBe "When did the health issue first stop you submitting the VAT Return?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
            result(3)._1 shouldBe "Reason for appealing after 30 days"
            result(3)._2 shouldBe "Lorem ipsum"
            result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
              controllers.routes.MakingALateAppealController.onPageLoad().url,
              MakingALateAppealPage.toString
            ).url
          }
        }
      }

      "for other" must {
        "for no upload" in {
          val fakeRequestWithOtherNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other")(fakeRequestWithOtherNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "Not provided"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for no upload - and late appeal" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "Not provided"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(5)._1 shouldBe "Reason for appealing after 30 days"
          result(5)._2 shouldBe "This is the reason why my appeal was late."
          result(5)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "for upload" in {
          val fakeRequestWithNoLateAppealButUploadPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "file.docx"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for upload - and late appeal" in {
          val fakeRequestWithNoLateAppealButUploadPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.isUploadEvidence -> "yes"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "Yes"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Evidence to support this appeal"
          result(4)._2 shouldBe "file.docx"
          result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(5)._1 shouldBe "Reason for appealing after 30 days"
          result(5)._2 shouldBe "This is the reason why my appeal was late."
          result(5)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "when the user clicked no to upload don't show the upload evidence row" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.isUploadEvidence -> "no"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "No"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }

        "when the user has changed their answer and does not want to upload files - but existing files have been uploaded - 'hide' the row" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.isUploadEvidence -> "no"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
            "other", Some("file.docx"))(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "The reason does not fit into any of the other categories"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
          result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(3)._2 shouldBe "No"
          result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.MakingALateAppealController.onPageLoad().url,
            MakingALateAppealPage.toString
          ).url
        }
      }
    }
  }

  "getHealthReasonAnswers" must {
    "when an agent is on the page" should {
      "when there is no hospital stay" should {
        "return rows of answers" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.whatCausedYouToMissTheDeadline -> "client"
            ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "Health"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2)._1 shouldBe "When did the health issue first stop your client getting information to you?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }

        "when the client intended to submit the VAT return" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "no",
            SessionKeys.whenHealthIssueHappened -> "2022-01-01",
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "Health"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2)._1 shouldBe "When did the health issue first stop your client submitting the VAT Return?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }

        "when the agent intended to submit but missed the deadline" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "no",
            SessionKeys.whenHealthIssueHappened -> "2022-01-01",
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
          ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "Health"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.ReasonableExcuseController.onPageLoad().url,
            ReasonableExcuseSelectionPage.toString
          ).url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2)._1 shouldBe "When did the health issue first stop you submitting the VAT Return?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }
      }

      "when it is an LPP show the correct message" in {
        val fakeRequestWithAllNonHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
        .withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
          SessionKeys.reasonableExcuse -> "health",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.wasHospitalStayRequired -> "no",
          SessionKeys.whenHealthIssueHappened -> "2022-01-01"
        ))

        val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Health"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "No"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
          WasHospitalStayRequiredPage.toString
        ).url
        result(2)._1 shouldBe "When did the health issue first stop your client paying the VAT bill?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
          WhenDidHealthIssueHappenPage.toString
        ).url
      }
    }

    "when a VAT trader is on the page" should {
      "when there is no hospital stay" should {
        "return rows of answers" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01"
            ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result.head._1 shouldBe "Reason for missing the VAT deadline"
          result.head._2 shouldBe "Health"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
            WasHospitalStayRequiredPage.toString
          ).url
          result(2)._1 shouldBe "When did the health issue first stop you submitting the VAT Return?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
            WhenDidHealthIssueHappenPage.toString
          ).url
        }
      }

      "when it is an LPP show the correct message" in {
        val fakeRequestWithAllNonHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "no",
            SessionKeys.whenHealthIssueHappened -> "2022-01-01"
          ))

        val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Health"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "No"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
          WasHospitalStayRequiredPage.toString
        ).url
        result(2)._1 shouldBe "When did the health issue first stop you paying the VAT bill?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url,
          WhenDidHealthIssueHappenPage.toString
        ).url
      }
    }

    "when there is hospital stay ended" should {
      "return a Seq[String, String, String, String, String] of answers" in {
        val fakeRequestWithAllHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.whenHealthIssueStarted -> "2022-01-01",
            SessionKeys.hasHealthEventEnded -> "yes",
            SessionKeys.whenHealthIssueEnded -> "2022-02-02"
          ))

        val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllHospitalStayKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Health"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
          WasHospitalStayRequiredPage.toString
        ).url
        result(2)._1 shouldBe "When did the hospital stay begin?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
          WhenDidHospitalStayBeginPage.toString
        ).url
        result(3)._1 shouldBe "Has the hospital stay ended?"
        result(3)._2 shouldBe "Yes"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
          DidHospitalStayEndPage.toString
        ).url
        result(4)._1 shouldBe "When did it end?"
        result(4)._2 shouldBe "2 February 2022"
        result(4)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
          DidHospitalStayEndPage.toString
        ).url
      }
    }
    "when there is hospital stay not ended" should {
      "return a Seq[String, String, String, String] of answers" in {
        val fakeRequestWithAllHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "health",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.wasHospitalStayRequired -> "yes",
            SessionKeys.whenHealthIssueStarted -> "2022-01-01",
            SessionKeys.hasHealthEventEnded -> "no"
          ))

        val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllHospitalStayKeysPresent, implicitly)
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Health"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url,
          WasHospitalStayRequiredPage.toString
        ).url
        result(2)._1 shouldBe "When did the hospital stay begin?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url,
          WhenDidHospitalStayBeginPage.toString
        ).url
        result(3)._1 shouldBe "Has the hospital stay ended?"
        result(3)._2 shouldBe "No"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url,
          DidHospitalStayEndPage.toString
        ).url
      }
    }
  }

  "for bereavement (someone died)" must {
    "return all the keys from the session ready to be passed to the view" in {
      val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequestConverter(fakeRequest
        .withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01"
        ))

      val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
        "bereavement")(UserRequest("123456789")(fakeRequestWithAllLossOfStaffKeysPresent), implicitly)
      result.head._1 shouldBe "Reason for missing the VAT deadline"
      result.head._2 shouldBe "Bereavement (someone died)"
      result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
      result(1)._1 shouldBe "When did the person die?"
      result(1)._2 shouldBe "1 January 2022"
      result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
          WhenDidThePersonDiePage.toString
        ).url
    }

    "return all keys and the 'Reason for appealing after 30 days' text" in {
      val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequestConverter(fakeRequest
        .withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01",
          SessionKeys.lateAppealReason -> "Lorem ipsum"
        ))

      val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
        "bereavement")(UserRequest("123456789")(fakeRequestWithAllLossOfStaffKeysPresent), implicitly)
      result.head._1 shouldBe "Reason for missing the VAT deadline"
      result.head._2 shouldBe "Bereavement (someone died)"
      result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
      result(1)._1 shouldBe "When did the person die?"
      result(1)._2 shouldBe "1 January 2022"
      result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
          WhenDidThePersonDiePage.toString
        ).url
      result(2)._1 shouldBe "Reason for appealing after 30 days"
      result(2)._2 shouldBe "Lorem ipsum"
      result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.MakingALateAppealController.onPageLoad().url,
        MakingALateAppealPage.toString
      ).url
    }
  }

  "getContentForAgentsCheckYourAnswersPage" should {
    "when the client planned to submit VAT return (so no cause Of LateSubmission chosen)" should {
      "return rows of answers" in {
        val fakeRequestWithClientPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithClientPresent, implicitly)
        result.head._1 shouldBe "Before the deadline, who planned to submit the return?"
        result.head._2 shouldBe "My client did"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage.toString
        ).url
      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being agent" should {
      "return rows of answers" in {
        val fakeRequestWithAgentKeysPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "agent"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithAgentKeysPresent, implicitly)
        result.head._1 shouldBe "Before the deadline, who planned to submit the return?"
        result.head._2 shouldBe "I did"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage.toString
        ).url
        result(1)._1 shouldBe "What caused you to miss the deadline?"
        result(1)._2 shouldBe "Something else happened to delay me"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
          WhatCausedYouToMissTheDeadlinePage.toString
        ).url

      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being client" should {
      "return rows of answers" in {
        val fakeRequestWithAgentKeysPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.whatCausedYouToMissTheDeadline -> "client"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithAgentKeysPresent, implicitly)
        result.head._1 shouldBe "Before the deadline, who planned to submit the return?"
        result.head._2 shouldBe "I did"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url,
          WhoPlannedToSubmitVATReturnAgentPage.toString
        ).url
        result(1)._1 shouldBe "What caused you to miss the deadline?"
        result(1)._2 shouldBe "My client did not get information to me on time"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AgentsController.onPageLoadForWhatCausedYouToMissTheDeadline(CheckMode).url,
          WhatCausedYouToMissTheDeadlinePage.toString
        ).url
      }
    }
  }

  "getAllTheContentForCheckYourAnswersPage" should {
    "when agent session is present" should {
      "return getAllTheContentForCheckYourAnswersPage as list of getContentForAgentsCheckYourAnswersPage and  " +
        "getContentForReasonableExcuseCheckYourAnswersPage" in {
        val fakeRequest = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.agentSessionVrn -> "123456789",
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          ))

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequest, implicitly)
        val resultAgent = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequest, implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(fakeRequest, implicitly)

        resultAgent ++ resultReasonableExcuses shouldBe resultAllContent
      }

      "return getAllTheContentForCheckYourAnswersPage as list of ONLY getContentForReasonableExcuseCheckYourAnswersPage when it is a LPP appeal" in {
        val fakeRequest = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.agentSessionVrn -> "123456789"
          ))

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "technicalIssues")(fakeRequest, implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(fakeRequest, implicitly)

        resultReasonableExcuses shouldBe resultAllContent
      }

      "return getAllTheContentForCheckYourAnswersPage as list of ONLY getContentForReasonableExcuseCheckYourAnswersPage" +
        " when it is a LPP appeal (Additional)" in {
        val fakeRequest = agentFakeRequestConverter(agentRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.appealType -> PenaltyTypeEnum.Additional.toString,
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.agentSessionVrn -> "123456789"
          ))

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequest, implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(fakeRequest, implicitly)

        resultReasonableExcuses shouldBe resultAllContent
      }
    }
    "agent session is not present" when {
      "the appeal is against the obligation" must {
        "show the obligation variation of the page" in {
          val fakeRequestForAppealingTheObligation = UserRequest(vrn)(fakeRequest.withSession(
            SessionKeys.isObligationAppeal -> "true",
            SessionKeys.otherRelevantInformation -> "This is some relevant information",
            SessionKeys.isUploadEvidence -> "yes"
          ))

          val result = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(Some("file.txt"))(fakeRequestForAppealingTheObligation, implicitly)
          result.head._1 shouldBe "Tell us why you want to appeal the penalty"
          result.head._2 shouldBe "This is some relevant information"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
            OtherRelevantInformationPage.toString
          ).url
          result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(1)._2 shouldBe "Yes"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result(2)._1 shouldBe "Evidence to support this appeal"
          result(2)._2 shouldBe "file.txt"
          result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "show the obligation variation of the page - 'hide' the files uploaded if user selected no to uploading files" in {
          val fakeRequestForAppealingTheObligation = UserRequest(vrn)(fakeRequest.withSession(
            SessionKeys.isObligationAppeal -> "true",
            SessionKeys.otherRelevantInformation -> "This is some relevant information",
            SessionKeys.isUploadEvidence -> "no"
          ))

          val result = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(Some("file.txt"))(fakeRequestForAppealingTheObligation, implicitly)
          result.head._1 shouldBe "Tell us why you want to appeal the penalty"
          result.head._2 shouldBe "This is some relevant information"
          result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
            OtherRelevantInformationPage.toString
          ).url
          result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
            UploadEvidenceQuestionPage.toString
          ).url
          result.size shouldBe 2
        }
      }

      "return getAllTheContentForCheckYourAnswersPage as list of getContentForReasonableExcuseCheckYourAnswersPage only" in {
        val fakeRequestWithCorrectKeysAndReasonableExcuseSet = (reasonableExcuse: String) => UserRequest(vrn)(fakeRequest
          .withSession(SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            (SessionKeys.reasonableExcuse, reasonableExcuse)))

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage(
          "technicalIssues")(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"), implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(
          fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"), implicitly)

        resultReasonableExcuses shouldBe resultAllContent

      }
    }
  }

  "getContentForObligationAppealCheckYourAnswersPage" should {
    "when no evidence file uploaded" should {
      "return rows of answers" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information",
            SessionKeys.isUploadEvidence -> "yes"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage()(fakeRequestWithObligationKeysPresent, implicitly)
        result.head._1 shouldBe "Tell us why you want to appeal the penalty"
        result.head._2 shouldBe "Some Information"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(2)._1 shouldBe "Evidence to support this appeal"
        result(2)._2 shouldBe "Not provided"
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }

      "return rows of answers - without uploaded files row" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information",
            SessionKeys.isUploadEvidence -> "no"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage()(fakeRequestWithObligationKeysPresent, implicitly)
        result.head._1 shouldBe "Tell us why you want to appeal the penalty"
        result.head._2 shouldBe "Some Information"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(1)._2 shouldBe "No"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result.size shouldBe 2
      }
    }

    "when evidence file is uploaded" should {
      "return rows of answers" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information",
            SessionKeys.isUploadEvidence -> "yes"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage(
          Some("some-file-name.txt"))(fakeRequestWithObligationKeysPresent, implicitly)
        result.head._1 shouldBe "Tell us why you want to appeal the penalty"
        result.head._2 shouldBe "Some Information"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(2)._1 shouldBe "Evidence to support this appeal"
        result(2)._2 shouldBe "some-file-name.txt"
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }

      "the user has selected no to uploaded files - 'hide' the row" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information",
            SessionKeys.isUploadEvidence -> "no"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage(
          Some("some-file-name.txt"))(fakeRequestWithObligationKeysPresent, implicitly)
        result.head._1 shouldBe "Tell us why you want to appeal the penalty"
        result.head._2 shouldBe "Some Information"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(1)._2 shouldBe "No"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result.size shouldBe 2
      }
    }
  }

  "getPreviousUploadsFileNames" should {
    "return the file names" in {
      val fakeRequestForOtherJourney: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
        SessionKeys.reasonableExcuse -> "other",
        SessionKeys.hasConfirmedDeclaration -> "true",
        SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
        SessionKeys.whenDidBecomeUnable -> "2022-01-02",
        SessionKeys.journeyId -> "4321"
      ))
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
      when(mockRepository.getUploadsForJourney(Some("4321"))).thenReturn(Future.successful(Option(Seq(callBackModel))))
      await(sessionAnswersHelper.getPreviousUploadsFileNames()(fakeRequestForOtherJourney)) shouldBe "file1.txt"
    }
  }

  "getContentWithExistingUploadFileNames" should {
    "when reason is 'other' (that requires a file upload call)" should {
      "return the rows for CYA page" in {
        val fakeRequestForOtherJourney: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.journeyId -> "4321",
          SessionKeys.isUploadEvidence -> "yes"
        ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("other")(fakeRequestForOtherJourney, messages))
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "The reason does not fit into any of the other categories"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the issue first stop you submitting the VAT Return?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url,
            WhenDidBecomeUnablePage.toString
          ).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is a reason."
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
            controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url,
            WhyWasReturnSubmittedLatePage.toString
          ).url
        result(3)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(3)._2 shouldBe "Yes"
        result(3)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(4)._1 shouldBe "Evidence to support this appeal"
        result(4)._2 shouldBe "file1.txt"
        result(4)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }

    "when there's an Obligation Appeal Journey (that requires a file upload call) " should {
      "return the rows for CYA page " in {
        val fakeRequestForAppealingTheObligation: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.journeyId -> "4321",
          SessionKeys.isObligationAppeal -> "true",
          SessionKeys.otherRelevantInformation -> "This is some relevant information",
          SessionKeys.isUploadEvidence -> "yes"
        ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("other")(fakeRequestForAppealingTheObligation, messages))
        result.head._1 shouldBe "Tell us why you want to appeal the penalty"
        result.head._2 shouldBe "This is some relevant information"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
          OtherRelevantInformationPage.toString
        ).url
        result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
          UploadEvidenceQuestionPage.toString
        ).url
        result(2)._1 shouldBe "Evidence to support this appeal"
        result(2)._2 shouldBe "file1.txt"
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }

    "when the user has files uploaded - but changed their mind - 'hide' the files uploaded" in {
      val fakeRequestForAppealingTheObligation: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
        SessionKeys.journeyId -> "4321",
        SessionKeys.isObligationAppeal -> "true",
        SessionKeys.otherRelevantInformation -> "This is some relevant information",
        SessionKeys.isUploadEvidence -> "no"
      ))
      val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("other")(fakeRequestForAppealingTheObligation, messages))
      result.head._1 shouldBe "Tell us why you want to appeal the penalty"
      result.head._2 shouldBe "This is some relevant information"
      result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url,
        OtherRelevantInformationPage.toString
      ).url
      result(1)._1 shouldBe "Do you want to upload evidence to support your appeal?"
      result(1)._2 shouldBe "No"
      result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
        controllers.routes.OtherReasonController.onPageLoadForUploadEvidenceQuestion(CheckMode).url,
        UploadEvidenceQuestionPage.toString
      ).url
      result.size shouldBe 2
    }

    "when reason is 'bereavement' (that doesn't require a file upload call)" should {
      "return the rows for CYA page " in {
        val fakeRequestWithBereavementKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "bereavement",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidThePersonDie -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum",
            SessionKeys.isUploadEvidence -> "yes"
          ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("bereavement")(fakeRequestWithBereavementKeysPresent, messages))
        result.head._1 shouldBe "Reason for missing the VAT deadline"
        result.head._2 shouldBe "Bereavement (someone died)"
        result.head._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.ReasonableExcuseController.onPageLoad().url,
          ReasonableExcuseSelectionPage.toString
        ).url
        result(1)._1 shouldBe "When did the person die?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url,
          WhenDidThePersonDiePage.toString
        ).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.CheckYourAnswersController.changeAnswer(
          controllers.routes.MakingALateAppealController.onPageLoad().url,
          MakingALateAppealPage.toString
        ).url
      }
    }
  }
}
