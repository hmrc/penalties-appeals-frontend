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

import models.upload.FailureReasonEnum
import play.api.i18n.Messages
import play.twirl.api.Html

object UpscanMessageHelper {

  val jsPrefix = "upscan"
  val noJsPrefix = "upscan.noJs"

  def getLocalisedFailureMessageForFailure(failureReason: FailureReasonEnum.Value, isJsEnabled: Boolean, fileIndex: Option[Int] = None)(implicit messages: Messages): String = {
    failureReason match {
      case FailureReasonEnum.QUARANTINE => messages(getJsOrNonJsFailureMessage("fileHasVirus", isJsEnabled), fileIndex.getOrElse(""))
      case FailureReasonEnum.REJECTED => messages(getJsOrNonJsFailureMessage("invalidMimeType", isJsEnabled), fileIndex.getOrElse(""))
      case FailureReasonEnum.UNKNOWN => messages(getJsOrNonJsFailureMessage("unableToUpload", isJsEnabled), fileIndex.getOrElse(""))
    }
  }

  def getUploadFailureMessage(errorCode: String, isJsEnabled: Boolean, fileIndex: Option[Int] = None)(implicit messages: Messages): String = {
    errorCode match {
      case "EntityTooSmall" => messages(getJsOrNonJsFailureMessage("fileEmpty", isJsEnabled), fileIndex.getOrElse(""))
      case "EntityTooLarge" => messages(getJsOrNonJsFailureMessage("fileTooLarge", isJsEnabled), fileIndex.getOrElse(""))
      case "400" | "InvalidArgument" => "upscan.fileNotSpecified"
      case _ => messages(getJsOrNonJsFailureMessage("unableToUpload", isJsEnabled), fileIndex.getOrElse(""))
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

  def getJsOrNonJsFailureMessage(errorSuffix: String, isJsEnabled: Boolean ): String = {
    if (isJsEnabled) s"$jsPrefix.$errorSuffix" else s"$noJsPrefix.$errorSuffix"
  }
}
