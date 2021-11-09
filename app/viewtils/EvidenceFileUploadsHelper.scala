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

package viewtils

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class EvidenceFileUploadsHelper {
  def displayContentForFileUploads(uploadedFiles: Seq[(String, Int)])(implicit messages: Messages): Seq[SummaryListRow] = {
    uploadedFiles.map(fileInfo => SummaryListRow(
        key = Key(
          content = Text(messages("otherReason.uploadList.rowTitle", fileInfo._2 + 1)),
          classes = "govuk-summary-list__key"
        ),
        value = Value(
          content = HtmlContent(fileInfo._1),
          classes = "govuk-summary-list__value"
        ),
        classes = "govuk-summary-list__row",
        actions = Some(Actions(
          classes = "govuk-link",
          items = Seq(
            ActionItem(
              href = "remove-file-upload",
              content = HtmlContent(messages("otherReason.uploadEvidence.button.remove"))
            )
          )
        ))
      )
    )
  }
}
