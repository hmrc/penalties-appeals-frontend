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
import models.upload.FailureReasonEnum

class UpscanMessageHelperSpec extends SpecBase {

  "getLocalisedFailureMessageForFailure" when {
    "routing through the js journey " should {
      "return virus message when status is QUARANTINE" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.QUARANTINE, true, Some(1))
        result shouldBe "File 1 contains a virus. Choose another file."
      }

      "return MIME type message when status is REJECTED" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.REJECTED, true, Some(1))
        result shouldBe "File 1 must be a JPG, PNG, TIFF, PDF, TXT, MSG, Word, Excel, Powerpoint or Open Document Format (ODF)"
      }

      "return try again message when status is UNKNOWN" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.UNKNOWN, true, Some(1))
        result shouldBe "File 1 could not be uploaded. Choose another file."
      }
    }

    "routing through the non-js journey" should {
      "return virus message when status is QUARANTINE" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.QUARANTINE, false)
        result shouldBe "The selected file contains a virus. Choose another file."
      }

      "return MIME type message when status is REJECTED" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.REJECTED, false)
        result shouldBe "The selected file must be a JPG, PNG, TIFF, PDF, TXT, MSG, Word, Excel, Powerpoint or Open Document Format (ODF). Choose another file."
      }

      "return try again message when status is UNKNOWN" in {
        val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(FailureReasonEnum.UNKNOWN, false)
        result shouldBe "The selected file could not be uploaded. Choose another file."
      }
    }
  }

  "getUploadFailureMessage" should {

    "routing through the js journey " should {
      "return empty file message when errorCode is EntityTooSmall" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooSmall", true, Some(1))
        result shouldBe "File 1 is empty. Choose another file."
      }

      "return file too large message when errorCode is EntityTooLarge" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooLarge", true, Some(1))
        result shouldBe "File 1 must be smaller than 6MB. Choose another file."
      }

      "return select a file message when errorCode is InvalidArgument" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("InvalidArgument", true, Some(1))
        result shouldBe "upscan.fileNotSpecified"
      }

      "return try again message when errorCode is not matched" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("InternalError", true, Some(1))
        result shouldBe "File 1 could not be uploaded. Choose another file."
      }
    }

    "routing through the non-js journey" should {
      "return empty file message when errorCode is EntityTooSmall" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooSmall", false, None)
        result shouldBe "The selected file is empty. Choose another file."
      }

      "return file too large message when errorCode is EntityTooLarge" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("EntityTooLarge", false, None)
        result shouldBe "The selected file must be smaller than 6MB. Choose another file."
      }

      "return select a file message when errorCode is InvalidArgument" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("InvalidArgument", false, None)
        result shouldBe "upscan.fileNotSpecified"
      }

      "return try again message when errorCode is not matched" in {
        val result = UpscanMessageHelper.getUploadFailureMessage("InternalError", false, None)
        result shouldBe "The selected file could not be uploaded. Choose another file."
      }
    }
  }

  "applyMessage" should {
    "apply the messages API to the key" in {
      val result = UpscanMessageHelper.applyMessage("upscan.fileNotSpecified")
      result shouldBe "Select a file."
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is only one total passed in" in {
        val result = UpscanMessageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.singular"
      }
    }
    "show the plural wording" when {
      "there is more than one total passed in" in {
        val result = UpscanMessageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.plural"
      }
    }
  }

  "getJsOrNonJsFailureMessage" when {
    "a request cookie contains 'jsenabled' and nonJsRoute is disabled" should {
      "return a message with the 'jsPrefix'" in {
        val result: String = UpscanMessageHelper.getJsOrNonJsFailureMessage("foo", true)
        result shouldBe "upscan.foo"
      }
    }

    "a request cookie does not contain 'jsenabled' and nonJsRoute is enabled" should {
      "return a message with the 'noJsPrefix'" in {
        val result = UpscanMessageHelper.getJsOrNonJsFailureMessage("bar", false)
        result shouldBe "upscan.noJs.bar"
      }
    }
  }
}
