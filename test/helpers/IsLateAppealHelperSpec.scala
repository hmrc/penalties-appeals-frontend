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
import models.{PenaltyTypeEnum, UserRequest}
import models.session.UserAnswers
import play.api.libs.json.Json
import utils.SessionKeys

import java.time.LocalDate

class IsLateAppealHelperSpec extends SpecBase {
  val helper: IsLateAppealHelper = injector.instanceOf[IsLateAppealHelper]

  "isAppealLate" should {
    "return true" when {
      "user is submitting a single appeal late" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-02-08"),
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = helper.isAppealLate()(userRequest)
        result shouldBe true
      }

      "user is submitting two appeals that are both late" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.parse("2020-01-01"),
          SessionKeys.secondPenaltyCommunicationDate -> LocalDate.parse("2020-01-01"),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-01-01"),
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2021-01-01"),
          SessionKeys.doYouWantToAppealBothPenalties -> "yes"
        )))(fakeRequest)
        val result = helper.isAppealLate()(userRequest)
        result shouldBe true
      }

      "user is submitting two appeals one late and one not late" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.parse("2020-01-01"),
          SessionKeys.secondPenaltyCommunicationDate -> LocalDate.now().plusDays(1),
          SessionKeys.dateCommunicationSent -> LocalDate.parse("2020-01-01"),
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2021-01-01"),
          SessionKeys.doYouWantToAppealBothPenalties -> "yes"
        )))(fakeRequest)
        val result = helper.isAppealLate()(userRequest)
        result shouldBe true
      }
    }

    "return false" when {
      "user is submitting a single appeal that is not late" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
          SessionKeys.reasonableExcuse -> "lossOfStaff",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenPersonLeftTheBusiness -> LocalDate.parse("2022-01-01")
        )))(fakeRequest)
        val result = helper.isAppealLate()(userRequest)
        result shouldBe false
      }

      "user is submitting two appeals that are both not late" in {
        val userRequest = UserRequest("123456789", answers = UserAnswers("1234", Json.obj(
          SessionKeys.penaltyNumber -> "1234",
          SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
          SessionKeys.startDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.endDateOfPeriod -> LocalDate.parse("2020-01-01"),
          SessionKeys.dueDateOfPeriod -> LocalDate.parse("2020-02-07"),
          SessionKeys.firstPenaltyCommunicationDate -> LocalDate.now().plusDays(1),
          SessionKeys.secondPenaltyCommunicationDate -> LocalDate.now().plusDays(1),
          SessionKeys.dateCommunicationSent -> LocalDate.now().plusDays(1),
          SessionKeys.reasonableExcuse -> "bereavement",
          SessionKeys.hasConfirmedDeclaration -> true,
          SessionKeys.whenDidThePersonDie -> LocalDate.parse("2021-01-01"),
          SessionKeys.doYouWantToAppealBothPenalties -> "yes"
        )))(fakeRequest)
        val result = helper.isAppealLate()(userRequest)
        result shouldBe false
      }
    }
  }

}
