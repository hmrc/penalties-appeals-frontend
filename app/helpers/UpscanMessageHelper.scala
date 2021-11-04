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

import models.upload.FailureReasonEnum
import play.api.i18n.Messages
import play.twirl.api.Html

object UpscanMessageHelper {
  def getLocalisedFailureMessageForFailure(failureReason: FailureReasonEnum.Value): String = {
    failureReason match {
      case FailureReasonEnum.QUARANTINE => "upscan.fileHasVirus"
      case FailureReasonEnum.REJECTED => "upscan.invalidMimeType"
      case FailureReasonEnum.UNKNOWN => "upscan.unableToUpload"
      case FailureReasonEnum.DUPLICATE => "upscan.duplicateFile"
    }
  }

  def getUploadFailureMessage(errorCode: String): String = {
    errorCode match {
      case "EntityTooSmall" => "upscan.fileEmpty"
      case "EntityTooLarge" => "upscan.fileTooLarge"
      case "400" | "InvalidArgument" => "upscan.fileNotSpecified"
      case _ => "upscan.unableToUpload"
    }
  }

  def applyMessage(msgKey: String)(implicit messages: Messages): String = {
    messages(msgKey)
  }

  def getPluralOrSingular(total: Int)(msgForSingular: String, msgForPlural: String)(implicit messages: Messages): Html = {
    if (total == 1) {
      Html(messages.apply(msgForSingular, total))
    } else {
      Html(messages.apply(msgForPlural, total))
    }
  }
}
