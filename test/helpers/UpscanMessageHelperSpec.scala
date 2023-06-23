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
import models.upload.FailureReasonEnum

class UpscanMessageHelperSpec extends SpecBase {

  def testGetLocalisedFailureMessageForFailure(failureReason: FailureReasonEnum.Value, expectedResult: String, isJsEnabled: Boolean): Unit = {
    s"return the correct message when status is $failureReason" in {
      val result = UpscanMessageHelper.getLocalisedFailureMessageForFailure(failureReason, isJsEnabled)
      result shouldBe expectedResult

    }
  }

  def testGetUploadFailureMessage(errorCode: String, expectedResult: String, isJsEnabled: Boolean): Unit = {
    s"return the correct message when error code is $errorCode" in {
      val result = UpscanMessageHelper.getUploadFailureMessage(errorCode, isJsEnabled)
      result shouldBe expectedResult
    }
  }



  "getLocalisedFailureMessageForFailure" when {
    "routing through the js journey " should {
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.QUARANTINE, "upscan.fileHasVirus", isJsEnabled = true)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.REJECTED, "upscan.invalidMimeType", isJsEnabled = true)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.UNKNOWN, "upscan.unableToUpload", isJsEnabled = true)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.DUPLICATE, "upscan.unableToUploadMessageFailure", isJsEnabled = true)
    }

    "routing through the non-js journey" should {
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.QUARANTINE, "upscan.noJs.fileHasVirus", isJsEnabled = false)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.REJECTED, "upscan.noJs.invalidMimeType", isJsEnabled = false)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.UNKNOWN, "upscan.noJs.unableToUpload", isJsEnabled = false)
      testGetLocalisedFailureMessageForFailure(FailureReasonEnum.DUPLICATE, "upscan.noJs.unableToUploadMessageFailure", isJsEnabled = false)
    }
  }

  "getUploadFailureMessage" should {

    "routing through the js journey " should {
      testGetUploadFailureMessage("EntityTooSmall", "upscan.fileEmpty", isJsEnabled = true)
      testGetUploadFailureMessage("EntityTooLarge", "upscan.fileTooLarge", isJsEnabled = true)
      testGetUploadFailureMessage("InvalidArgument", "upscan.fileNotSpecified", isJsEnabled = true)
      testGetUploadFailureMessage("InternalError", "upscan.unableToUpload", isJsEnabled = true)
    }

    "routing through the non-js journey" should {
      testGetUploadFailureMessage("EntityTooSmall", "upscan.noJs.fileEmpty", isJsEnabled = false)
      testGetUploadFailureMessage("EntityTooLarge", "upscan.noJs.fileTooLarge", isJsEnabled = false)
      testGetUploadFailureMessage("InvalidArgument", "upscan.fileNotSpecified", isJsEnabled = false)
      testGetUploadFailureMessage("InternalError", "upscan.noJs.unableToUpload", isJsEnabled = false)
    }
  }

  "applyMessage" should {
    "apply the messages API to the key" in {
      val result = UpscanMessageHelper.applyMessage("upscan.fileTooLarge", 1)
      result shouldBe "File 1 must be smaller than 6MB. Choose another file."
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there 1 is passed to the function" in {
        val result = UpscanMessageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.singular"
      }
    }

    "show the plural wording" when {
      "more than 1 is passed to the function" in {
        val result = UpscanMessageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly)
        result.body shouldBe "this.is.a.message.plural"
      }
    }
  }

  "getJsOrNonJsFailureMessage" should {
    "return the JS message" when {
      "the jsEnabled parameter is true" in {
        val result: String = UpscanMessageHelper.getJsOrNonJsFailureMessage("foo", true)
        result shouldBe "upscan.foo"
      }
    }

    "return the non-JS message" when {
      "the jsEnabled parameter is false" in {
        val result = UpscanMessageHelper.getJsOrNonJsFailureMessage("bar", false)
        result shouldBe "upscan.noJs.bar"
      }
    }
  }
}
