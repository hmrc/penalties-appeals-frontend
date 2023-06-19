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

class UploadStatusEnumSpec extends AnyWordSpec with Matchers with WritableReadableEnumTests {

  implicit val formatter: Format[UploadStatusEnum.Value] = UploadStatusEnum.format

  "UploadStatusEnum" should {

    "be writable to JSON" when {
      writableTest[UploadStatusEnum.Value](UploadStatusEnum.WAITING, "WAITING", "waiting for callback from Upscan")
      writableTest[UploadStatusEnum.Value](UploadStatusEnum.READY, "READY", "the upload succeeded")
      writableTest[UploadStatusEnum.Value](UploadStatusEnum.FAILED, "FAILED", "the upload failed")
    }

    "be readable from JSON" when {
      readableTest[UploadStatusEnum.Value](UploadStatusEnum.WAITING, "WAITING", "waiting for callback from Upscan")
      readableTest[UploadStatusEnum.Value](UploadStatusEnum.READY, "READY", "the upload succeeded")
      readableTest[UploadStatusEnum.Value](UploadStatusEnum.FAILED, "FAILED", "the upload failed")
    }

    readableTestError[UploadStatusEnum.Value]("invalid", "an invalid value is passed")
  }
}
