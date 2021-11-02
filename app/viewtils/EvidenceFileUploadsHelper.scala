
package viewtils

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class EvidenceFileUploadsHelper {

  def displayContentForFileUploads(uploadedFiles: Seq[(String, Int)])(implicit messages: Messages): Seq[SummaryListRow] = {

    uploadedFiles.map(fileInfo =>
      SummaryListRow(
        key = Key(
          content = Text(messages("", fileInfo._2 + 1)), // need to include message key for file number e.g otherReason.uploadList.rowTitle = File {0}
          classes = "govuk-summary-list__key",
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
              href = "#",
              content = HtmlContent(messages("otherReason.uploadEvidence.button.remove"))
            )
          )
        ))
      )
    )
  }

}
