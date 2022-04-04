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

package viewtils

import base.SpecBase
import models.PenaltyTypeEnum
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.SessionKeys

import java.time.LocalDateTime

class PenaltyTypeHelperSpec extends SpecBase {

  "convertPenaltyTypeToContentString" should {
    s"attempt to convert a string to a $PenaltyTypeEnum " when {
      s"the string matches an enum value - return $Some with the correct message for Late_Submission" in {
        val result = PenaltyTypeHelper.convertPenaltyTypeToContentString("Late_Submission")
        result.isDefined shouldBe true
        result.get shouldBe messages("penaltyType.lateSubmission")
      }

      s"the string matches an enum value - return $Some with the correct message for Late_Payment" in {
        val result = PenaltyTypeHelper.convertPenaltyTypeToContentString("Late_Payment")
        result.isDefined shouldBe true
        result.get shouldBe messages("penaltyType.latePayment")
      }

      s"the string matches an enum value - return $Some with the correct message for Additional" in {
        val result = PenaltyTypeHelper.convertPenaltyTypeToContentString("Additional")
        result.isDefined shouldBe true
        result.get shouldBe messages("penaltyType.additional")
      }

      s"the string does not match an enum value - return $None" in {
        val result = PenaltyTypeHelper.convertPenaltyTypeToContentString("what")
        result.isDefined shouldBe false
      }
    }
  }

  "getKeysFromSession" should {
    s"return $Some $Seq String when all the keys exist in the session and the enum can be parsed" in {
      val result = PenaltyTypeHelper.getKeysFromSession()(userRequestWithCorrectKeys, implicitly)
      result.isDefined shouldBe true
      result.get.head shouldBe messages("penaltyType.lateSubmission")
      result.get(1) shouldBe ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse("2020-01-01T12:00:00.500"))
      result.get(2) shouldBe ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse("2020-01-01T12:00:00.500"))
    }

    s"return $None when all the keys exist in the session and the enum can not be parsed" in {
      val fakeRequestWithWrongAppealType: FakeRequest[AnyContent] = fakeRequest.withSession(
        (SessionKeys.appealType, "invalid"),
        (SessionKeys.penaltyNumber, "123"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500")
      )
      val result = PenaltyTypeHelper.getKeysFromSession()(fakeRequestWithWrongAppealType, implicitly)
      result.isDefined shouldBe false
    }

    s"return $None when some of the keys do not exist in the session" in {
      val fakeRequestWithSomeKeysNotExisting: FakeRequest[AnyContent] = FakeRequest().withSession(
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.penaltyNumber, "123"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00.500")
      )
      val result = PenaltyTypeHelper.getKeysFromSession()(fakeRequestWithSomeKeysNotExisting, implicitly)
      result.isDefined shouldBe false
    }

    s"return $None when the dates stored in the session can not be parsed" in {
      val fakeRequestWithInvalidDates: FakeRequest[AnyContent] = fakeRequest.withSession(
        (SessionKeys.appealType, "invalid"),
        (SessionKeys.penaltyNumber, "123"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00over5thousand"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00.500")
      )
      val result = PenaltyTypeHelper.getKeysFromSession()(fakeRequestWithInvalidDates, implicitly)
      result.isDefined shouldBe false
    }
  }
}
