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
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import models.{CheckMode, PenaltyTypeEnum, UserRequest}
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import utils.SessionKeys

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SessionAnswersHelperSpec extends SpecBase with ScalaFutures{
  val mockRepository = mock(classOf[UploadJourneyRepository])
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
            SessionKeys.hasConfirmedDeclaration -> "true",
          )
        val result = sessionAnswersHelper.isAllAnswerPresentForReasonableExcuse("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent)
        result shouldBe true
      }

      "return false - when not all keys are present" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequest
          .withSession(
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Crime"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the crime happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForWhenCrimeHappened(CheckMode).url
        result(2)._1 shouldBe "Has this crime been reported to the police?"
        result(2)._2 shouldBe "Yes"
        result(2)._3 shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(CheckMode).url
        result(3)._1 shouldBe "Reason for appealing after 30 days"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }

    "for fire or flood" must {
      "return all the keys from the session ready to be passed to the view" in {
        val fakeRequestWithAllFireOrFloodKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "fireOrFlood",
            SessionKeys.dateOfFireOrFlood -> "2022-01-01",
            SessionKeys.hasConfirmedDeclaration -> "true",
          ))
        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("fireOrFlood")(fakeRequestWithAllFireOrFloodKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Fire or flood"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Fire or flood"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the fire or flood happen?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.FireOrFloodReasonController.onPageLoad(CheckMode).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Loss of staff essential to the VAT process"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the person become unavailable?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Loss of staff essential to the VAT process"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the person become unavailable?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.LossOfStaffReasonController.onPageLoad(CheckMode).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
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

      "return all keys and the 'Reason for appealing after 30 days' text" in {
        val fakeRequestWithAllTechnicalIssuesKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequestWithAllTechnicalIssuesKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Technology issues"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the technology issues begin?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesBegan(CheckMode).url
        result(2)._1 shouldBe "When did the technology issues end?"
        result(2)._2 shouldBe "2 January 2022"
        result(2)._3 shouldBe controllers.routes.TechnicalIssuesReasonController.onPageLoadForWhenTechnologyIssuesEnded(CheckMode).url
        result(3)._1 shouldBe "Reason for appealing after 30 days"
        result(3)._2 shouldBe "Lorem ipsum"
        result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }

    "for LPP on other journey" must {
      "display the correct wording" in {
        val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
            SessionKeys.reasonableExcuse -> "other",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whyReturnSubmittedLate -> "This is why my VAT payment was late.",
            SessionKeys.whenDidBecomeUnable -> "2022-01-01",
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the VAT bill paid late?"
        result(2)._2 shouldBe "This is why my VAT payment was late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        result(3)._2 shouldBe "Not provided"
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        result(4)._1 shouldBe "Reason for appealing after 30 days"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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
                SessionKeys.causeOfLateSubmissionAgent -> "client"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result(0)._1 shouldBe "Reason for missing the VAT deadline"
            result(0)._2 shouldBe "Health"
            result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
            result(2)._1 shouldBe "When did your client become unable to manage the VAT account?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
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
                SessionKeys.causeOfLateSubmissionAgent -> "client"
              ))

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result(0)._1 shouldBe "Reason for missing the VAT deadline"
            result(0)._2 shouldBe "Health"
            result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
            result(2)._1 shouldBe "When did your client become unable to manage the VAT account?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
            result(3)._1 shouldBe "Reason for appealing after 30 days"
            result(3)._2 shouldBe "Lorem ipsum"
            result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          }
        }
      }

      "for other" must {
        "for no upload" in {
          val fakeRequestWithOtherNoUploadKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.causeOfLateSubmissionAgent -> "client"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherNoUploadKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "The reason does not fit into any of the other categories"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "When did your client become unable to manage the VAT account?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
          result(3)._1 shouldBe "Evidence to support this appeal"
          result(3)._2 shouldBe "Not provided"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for no upload - and late appeal" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.causeOfLateSubmissionAgent -> "client"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "The reason does not fit into any of the other categories"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "When did your client become unable to manage the VAT account?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
          result(3)._1 shouldBe "Evidence to support this appeal"
          result(3)._2 shouldBe "Not provided"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }

        "for upload" in {
          val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.causeOfLateSubmissionAgent -> "client"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "The reason does not fit into any of the other categories"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "When did your client become unable to manage the VAT account?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
          result(3)._1 shouldBe "Evidence to support this appeal"
          result(3)._2 shouldBe "file.docx"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for upload - and late appeal" in {
          val fakeRequestWithNoLateAppealButUploadPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late.",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.causeOfLateSubmissionAgent -> "client"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "The reason does not fit into any of the other categories"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "When did your client become unable to manage the VAT account?"
          result(1)._2 shouldBe "1 January 2022"
          result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
          result(2)._1 shouldBe "Why was the return submitted late?"
          result(2)._2 shouldBe "This is why my VAT return was late."
          result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
          result(3)._1 shouldBe "Evidence to support this appeal"
          result(3)._2 shouldBe "file.docx"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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
            SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
          ))

        val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other", Some("file.docx"))(fakeRequestWithLPPKeysPresent, implicitly)
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did your client become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the VAT bill paid late?"
        result(2)._2 shouldBe "This is why my VAT bill was paid late."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        result(3)._2 shouldBe "file.docx"
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        result(4)._1 shouldBe "Reason for appealing after 30 days"
        result(4)._2 shouldBe "This is the reason why my appeal was late."
        result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
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

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result(0)._1 shouldBe "Reason for missing the VAT deadline"
            result(0)._2 shouldBe "Health"
            result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
            result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
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

            val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("health")(fakeRequestWithNoHospitalStayKeysPresent, implicitly)
            result(0)._1 shouldBe "Reason for missing the VAT deadline"
            result(0)._2 shouldBe "Health"
            result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
            result(1)._1 shouldBe "Did this health issue include a hospital stay?"
            result(1)._2 shouldBe "No"
            result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
            result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
            result(2)._2 shouldBe "1 January 2022"
            result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
            result(3)._1 shouldBe "Reason for appealing after 30 days"
            result(3)._2 shouldBe "Lorem ipsum"
            result(3)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
          }
        }
      }

      "for other" must {
        "for no upload" in {
          val fakeRequestWithOtherNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherNoUploadKeysPresent, implicitly)
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
          result(3)._2 shouldBe "Not provided"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for no upload - and late appeal" in {
          val fakeRequestWithOtherLateAppealAndNoUploadKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other")(fakeRequestWithOtherLateAppealAndNoUploadKeysPresent, implicitly)
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
          result(3)._2 shouldBe "Not provided"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }

        "for upload" in {
          val fakeRequestWithNoLateAppealButUploadPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01"
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
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
          result(3)._2 shouldBe "file.docx"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }

        "for upload - and late appeal" in {
          val fakeRequestWithNoLateAppealButUploadPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "other",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.whyReturnSubmittedLate -> "This is why my VAT return was late.",
              SessionKeys.whenDidBecomeUnable -> "2022-01-01",
              SessionKeys.lateAppealReason -> "This is the reason why my appeal was late."
            ))

          val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("other", Some("file.docx"))(fakeRequestWithNoLateAppealButUploadPresent, implicitly)
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
          result(3)._2 shouldBe "file.docx"
          result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
          result(4)._1 shouldBe "Reason for appealing after 30 days"
          result(4)._2 shouldBe "This is the reason why my appeal was late."
          result(4)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
        }
      }
    }
  }

  "getHealthReasonAnswers" must {
    "when an agent is on the page" should {
      "when there is no hospital stay" should {
        "return a Seq[String, String, String] of answers" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = agentFakeRequestConverter(agentRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01",
              SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
              SessionKeys.causeOfLateSubmissionAgent -> "client"
            ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "Health"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
          result(2)._1 shouldBe "When did your client become unable to manage the VAT account?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
        }
      }
    }

    "when a VAT trader is on the page" should {
      "when there is no hospital stay" should {
        "return a Seq[String, String, String] of answers" in {
          val fakeRequestWithAllNonHospitalStayKeysPresent = fakeRequestConverter(fakeRequest
            .withSession(
              SessionKeys.reasonableExcuse -> "health",
              SessionKeys.hasConfirmedDeclaration -> "true",
              SessionKeys.wasHospitalStayRequired -> "no",
              SessionKeys.whenHealthIssueHappened -> "2022-01-01"
            ))

          val result = sessionAnswersHelper.getHealthReasonAnswers()(fakeRequestWithAllNonHospitalStayKeysPresent, implicitly)
          result(0)._1 shouldBe "Reason for missing the VAT deadline"
          result(0)._2 shouldBe "Health"
          result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
          result(1)._1 shouldBe "Did this health issue include a hospital stay?"
          result(1)._2 shouldBe "No"
          result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
          result(2)._1 shouldBe "When did you become unable to manage the VAT account?"
          result(2)._2 shouldBe "1 January 2022"
          result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenHealthReasonHappened(CheckMode).url
        }
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Health"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
        result(2)._1 shouldBe "When did the hospital stay begin?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url
        result(3)._1 shouldBe "Has the hospital stay ended?"
        result(3)._2 shouldBe "Yes"
        result(3)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
        result(4)._1 shouldBe "When did it end?"
        result(4)._2 shouldBe "2 February 2022"
        result(4)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
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
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Health"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "Did this health issue include a hospital stay?"
        result(1)._2 shouldBe "Yes"
        result(1)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWasHospitalStayRequired(CheckMode).url
        result(2)._1 shouldBe "When did the hospital stay begin?"
        result(2)._2 shouldBe "1 January 2022"
        result(2)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForWhenDidHospitalStayBegin(CheckMode).url
        result(3)._1 shouldBe "Has the hospital stay ended?"
        result(3)._2 shouldBe "No"
        result(3)._3 shouldBe controllers.routes.HealthReasonController.onPageLoadForHasHospitalStayEnded(CheckMode).url
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

      val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("bereavement")(UserRequest("123456789")(fakeRequestWithAllLossOfStaffKeysPresent), implicitly)
      result(0)._1 shouldBe "Reason for missing the VAT deadline"
      result(0)._2 shouldBe "Bereavement (someone died)"
      result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      result(1)._1 shouldBe "When did the person die?"
      result(1)._2 shouldBe "1 January 2022"
      result(1)._3 shouldBe controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url
    }

    "return all keys and the 'Reason for appealing after 30 days' text" in {
      val fakeRequestWithAllLossOfStaffKeysPresent = fakeRequestConverter(fakeRequest
        .withSession(
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whenDidThePersonDie -> "2022-01-01",
          SessionKeys.lateAppealReason -> "Lorem ipsum"
        ))

      val result = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("bereavement")(UserRequest("123456789")(fakeRequestWithAllLossOfStaffKeysPresent), implicitly)
      result(0)._1 shouldBe "Reason for missing the VAT deadline"
      result(0)._2 shouldBe "Bereavement (someone died)"
      result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
      result(1)._1 shouldBe "When did the person die?"
      result(1)._2 shouldBe "1 January 2022"
      result(1)._3 shouldBe controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url
      result(2)._1 shouldBe "Reason for appealing after 30 days"
      result(2)._2 shouldBe "Lorem ipsum"
      result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }
  }

  "getContentForAgentsCheckYourAnswersPage" should {
    "when the client planned to submit VAT return (so no cause Of LateSubmission chosen)" should {
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithClientPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithClientPresent, implicitly)
        result(0)._1 shouldBe "Before the deadline, who planned to submit the return?"
        result(0)._2 shouldBe "My client did"
        result(0)._3 shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url
      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being agent" should {
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithAgentKeysPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.causeOfLateSubmissionAgent -> "agent"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithAgentKeysPresent, implicitly)
        result(0)._1 shouldBe "Before the deadline, who planned to submit the return?"
        result(0)._2 shouldBe "I did"
        result(0)._3 shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url
        result(1)._1 shouldBe "Why was the return submitted late?"
        result(1)._2 shouldBe "Something else happened to delay me"
        result(1)._3 shouldBe controllers.routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url

      }
    }

    "when the agent planned to submit VAT return with cause Of LateSubmission being client" should {
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithAgentKeysPresent = fakeRequest
          .withSession(
            SessionKeys.whoPlannedToSubmitVATReturn -> "agent",
            SessionKeys.causeOfLateSubmissionAgent -> "client"
          )

        val result = sessionAnswersHelper.getContentForAgentsCheckYourAnswersPage()(fakeRequestWithAgentKeysPresent, implicitly)
        result(0)._1 shouldBe "Before the deadline, who planned to submit the return?"
        result(0)._2 shouldBe "I did"
        result(0)._3 shouldBe controllers.routes.AgentsController.onPageLoadForWhoPlannedToSubmitVATReturn(CheckMode).url
        result(1)._1 shouldBe "Why was the return submitted late?"
        result(1)._2 shouldBe "My client did not get information to me on time"
        result(1)._3 shouldBe controllers.routes.AgentsController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url

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

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequest, implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(fakeRequest, implicitly)

        resultReasonableExcuses shouldBe resultAllContent
      }

      "return getAllTheContentForCheckYourAnswersPage as list of ONLY getContentForReasonableExcuseCheckYourAnswersPage when it is a LPP appeal (Additional)" in {
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
    "when agent session is not present" should {
      "the appeal is against the obligation" when {
        "show the obligation variation of the page" in {
          val fakeRequestForAppealingTheObligation = UserRequest(vrn)(fakeRequest.withSession(
            SessionKeys.isObligationAppeal -> "true",
            SessionKeys.otherRelevantInformation -> "This is some relevant information"
          ))

          val result = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage(Some("file.txt"))(fakeRequestForAppealingTheObligation, implicitly)
          result(0)._1 shouldBe "Tell us why you want to appeal the penalty"
          result(0)._2 shouldBe "This is some relevant information"
          result(0)._3 shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
          result(1)._1 shouldBe "Evidence to support this appeal"
          result(1)._2 shouldBe "file.txt"
          result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
        }
      }

      "return getAllTheContentForCheckYourAnswersPage as list of getContentForReasonableExcuseCheckYourAnswersPage only" in {
        val fakeRequestWithCorrectKeysAndReasonableExcuseSet = (reasonableExcuse: String) => UserRequest(vrn)(fakeRequest
          .withSession(SessionKeys.reasonableExcuse -> "technicalIssues",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidTechnologyIssuesBegin -> "2022-01-01",
            SessionKeys.whenDidTechnologyIssuesEnd -> "2022-01-02",
            (SessionKeys.reasonableExcuse, reasonableExcuse)))

        val resultReasonableExcuses = sessionAnswersHelper.getContentForReasonableExcuseCheckYourAnswersPage("technicalIssues")(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"), implicitly)
        val resultAllContent = sessionAnswersHelper.getAllTheContentForCheckYourAnswersPage()(fakeRequestWithCorrectKeysAndReasonableExcuseSet("technicalIssues"), implicitly)

        resultReasonableExcuses shouldBe resultAllContent

      }
    }
  }

  "getContentForObligationAppealCheckYourAnswersPage" should{
    "when no evidence file uploaded" should{
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage()(fakeRequestWithObligationKeysPresent, implicitly)
        result(0)._1 shouldBe "Tell us why you want to appeal the penalty"
        result(0)._2 shouldBe "Some Information"
        result(0)._3 shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
        result(1)._1 shouldBe "Evidence to support this appeal"
        result(1)._2 shouldBe "Not provided"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }
    "when evidence file is uploaded" should{
      "return a Seq[String, String, String] of answers" in {
        val fakeRequestWithObligationKeysPresent = fakeRequest
          .withSession(
            SessionKeys.otherRelevantInformation -> "Some Information"
          )
        val result = sessionAnswersHelper.getContentForObligationAppealCheckYourAnswersPage(Some("some-file-name.txt"))(fakeRequestWithObligationKeysPresent, implicitly)
        result(0)._1 shouldBe "Tell us why you want to appeal the penalty"
        result(0)._2 shouldBe "Some Information"
        result(0)._3 shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
        result(1)._1 shouldBe "Evidence to support this appeal"
        result(1)._2 shouldBe "some-file-name.txt"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }
  }

  "getPreviousUploadsFileNames" should{
    "return Future[String] " in {
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
      "return Future[Seq[(String, String, String)]] " in {
        val fakeRequestForOtherJourney: UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.reasonableExcuse -> "other",
          SessionKeys.hasConfirmedDeclaration -> "true",
          SessionKeys.whyReturnSubmittedLate -> "This is a reason.",
          SessionKeys.whenDidBecomeUnable -> "2022-01-01",
          SessionKeys.journeyId -> "4321"
        ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("other")(fakeRequestForOtherJourney, messages))
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "The reason does not fit into any of the other categories"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did you become unable to manage the VAT account?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhenDidBecomeUnable(CheckMode).url
        result(2)._1 shouldBe "Why was the return submitted late?"
        result(2)._2 shouldBe "This is a reason."
        result(2)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(CheckMode).url
        result(3)._1 shouldBe "Evidence to support this appeal"
        result(3)._2 shouldBe "file1.txt"
        result(3)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }

    "when there's an Obligation Appeal Journey (that requires a file upload call) " should {
      "return Future[Seq[(String, String, String)]] " in {
        val fakeRequestForAppealingTheObligation : UserRequest[AnyContent] = fakeRequestConverter(fakeRequestWithCorrectKeys.withSession(
          SessionKeys.journeyId -> "4321",
          SessionKeys.isObligationAppeal -> "true",
          SessionKeys.otherRelevantInformation -> "This is some relevant information"
        ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("other")(fakeRequestForAppealingTheObligation, messages))
        result(0)._1 shouldBe "Tell us why you want to appeal the penalty"
        result(0)._2 shouldBe "This is some relevant information"
        result(0)._3 shouldBe controllers.routes.AppealAgainstObligationController.onPageLoad(CheckMode).url
        result(1)._1 shouldBe "Evidence to support this appeal"
        result(1)._2 shouldBe "file1.txt"
        result(1)._3 shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(CheckMode).url
      }
    }

    "when reason is 'bereavement' (that doesn't require a file upload call)" should {
      "return Future[Seq[(String, String, String)]] " in {
        val fakeRequestWithBereavementKeysPresent = fakeRequestConverter(fakeRequest
          .withSession(
            SessionKeys.reasonableExcuse -> "bereavement",
            SessionKeys.hasConfirmedDeclaration -> "true",
            SessionKeys.whenDidThePersonDie -> "2022-01-01",
            SessionKeys.lateAppealReason -> "Lorem ipsum"
          ))
        val result = await(sessionAnswersHelper.getContentWithExistingUploadFileNames("bereavement")(fakeRequestWithBereavementKeysPresent, messages))
        result(0)._1 shouldBe "Reason for missing the VAT deadline"
        result(0)._2 shouldBe "Bereavement (someone died)"
        result(0)._3 shouldBe controllers.routes.ReasonableExcuseController.onPageLoad().url
        result(1)._1 shouldBe "When did the person die?"
        result(1)._2 shouldBe "1 January 2022"
        result(1)._3 shouldBe controllers.routes.BereavementReasonController.onPageLoadForWhenThePersonDied(CheckMode).url
        result(2)._1 shouldBe "Reason for appealing after 30 days"
        result(2)._2 shouldBe "Lorem ipsum"
        result(2)._3 shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
      }
    }
  }
}
