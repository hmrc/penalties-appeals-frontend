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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Format

class PenaltyTypeEnumSpec extends AnyWordSpec with Matchers with WritableReadableEnumTests {

  implicit val formatter: Format[PenaltyTypeEnum.Value] = PenaltyTypeEnum.format

  "be writable to JSON" when {
    writableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Late_Submission, "LATE_SUBMISSION", "be writable to JSON for 'LATE_SUBMISSION'")
    writableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Late_Payment, "LATE_PAYMENT", "be writable to JSON for 'LATE_PAYMENT'")
    writableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Additional, "ADDITIONAL", "be writable to JSON for 'ADDITIONAL'")
  }

  "be readable from JSON" when {
    readableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Late_Submission, "LATE_SUBMISSION", "be readable from JSON for 'LATE_SUBMISSION'")
    readableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Late_Payment, "LATE_PAYMENT", "be readable from JSON for 'LATE_PAYMENT'")
    readableTest[PenaltyTypeEnum.Value](PenaltyTypeEnum.Additional, "ADDITIONAL", "be readable from JSON for 'ADDITIONAL'")
  }

  readableTestError[PenaltyTypeEnum.Value]("invalid", "return a JSError when there is no matches for the specified value")
}
