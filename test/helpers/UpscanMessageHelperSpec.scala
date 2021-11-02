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
import models.upload.FailureReasonEnum

class UpscanMessageHelperSpec extends SpecBase {
  "getLocalisedFailureMessageForFailure" should {
    "return virus message when status is QUARANTINE" in {
      val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.QUARANTINE)
      result shouldBe "upscan.fileHasVirus"
    }

    "return MIME type message when status is REJECTED" in {
      val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.REJECTED)
      result shouldBe "upscan.invalidMimeType"
    }

    "return try again message when status is UNKNOWN" in {
      val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.UNKNOWN)
      result shouldBe "upscan.unableToUpload"
    }

    "return try again message when status is DUPLICATE" in {
      val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.DUPLICATE)
      result shouldBe "upscan.duplicateFile"
    }
  }

  "getUploadFailureMessage" should {
    "return empty file message when errorCode is EntityTooSmall" in {
      val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooSmall")
      result shouldBe "upscan.fileEmpty"
    }

    "return file too large message when errorCode is EntityTooLarge" in {
      val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooLarge")
      result shouldBe "upscan.fileTooLarge"
    }

    "return select a file message when errorCode is InvalidArgument" in {
      val result = UpscanMessageHelper.getUploadFailureMessage("InvalidArgument")
      result shouldBe "upscan.fileNotSpecified"
    }

    "return try again message when errorCode is not matched" in {
      val result = UpscanMessageHelper.getUploadFailureMessage("InternalError")
      result shouldBe "upscan.unableToUpload"
    }
  }

  "applyMessage" should {
    "apply the messages API to the key" in {
      val result = UpscanMessageHelper.applyMessage("upscan.fileNotSpecified")
      result shouldBe "Select a file."
    }
  }

  "getPluralOrSingular" should{
    "show the singular wording" when {
      "there is only one total passed in" in {
        val result = UpscanMessageHelper.getPluralOrSingular(1, 1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.singular"
      }
    }
      "show the plural wording" when {
        "there is more than one total passed in" in {
          val result = UpscanMessageHelper.getPluralOrSingular(2, 2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
          result.body shouldBe "this.is.a.message.plural"
        }
      }
  }
}
