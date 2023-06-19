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

package models.upload

import models.WritableReadableEnumTests
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Format

class FailureReasonEnumSpec extends AnyWordSpec with Matchers with WritableReadableEnumTests {

  implicit val formatter: Format[FailureReasonEnum.Value] = FailureReasonEnum.format

  "FailureReasonEnum" should {

    "be writable to JSON" when {
      writableTest[FailureReasonEnum.Value](FailureReasonEnum.QUARANTINE, "QUARANTINE", "the upload was quarantined")
      writableTest[FailureReasonEnum.Value](FailureReasonEnum.REJECTED, "REJECTED", "the upload was rejected")
      writableTest[FailureReasonEnum.Value](FailureReasonEnum.UNKNOWN, "UNKNOWN", "there was some other problem with the file")
    }

    "be readable from JSON" when {
      readableTest[FailureReasonEnum.Value](FailureReasonEnum.QUARANTINE, "QUARANTINE", "the upload was quarantined")
      readableTest[FailureReasonEnum.Value](FailureReasonEnum.REJECTED, "REJECTED", "the upload was rejected")
      readableTest[FailureReasonEnum.Value](FailureReasonEnum.UNKNOWN, "UNKNOWN", "there was some other problem with the file")
    }

    readableTestError[FailureReasonEnum.Value]("invalid", "an invalid value is passed")
  }
}
