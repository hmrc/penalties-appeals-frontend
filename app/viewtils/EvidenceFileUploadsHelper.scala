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

import models.{Mode, UserRequest}
import models.upload.UploadJourney
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{InsetText, Text}
import views.html.components.upload.uploadList
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText

import java.io
import javax.inject.Inject

class EvidenceFileUploadsHelper @Inject()(govukInsetText: GovukInsetText,
                                         )(uploadList: uploadList) {

  def displayContentForFileUploads(uploadedFiles: Seq[(UploadJourney, Int)], mode: Mode)(implicit messages: Messages, request: UserRequest[_]): Seq[Html] = {
    uploadedFiles.map(uploadWithIndex =>
      uploadList(uploadWithIndex._1.reference, uploadWithIndex._1.uploadDetails.get.fileName, uploadWithIndex._2 + 1, mode)
    )
  }

  def duplicateDocumentInset(uploadedFiles: Seq[(UploadJourney, Int)])(
    implicit messages: Messages, request: UserRequest[_]): Option[Html] = {

    val mappedDuplicates = uploadedFiles.groupBy(_._1.uploadDetails.map(_.checksum))

    if (!mappedDuplicates.exists(_._2.size > 1)) {
      None
    } else if (mappedDuplicates.count(_._2.size > 1) > 1) {
      Some(govukInsetText(InsetText(content = Text(messages("otherReason.uploadList.duplicateInsetText", mappedDuplicates.map(_._2.size).sum)))))
    } else {
      val duplicates = mappedDuplicates.head._2
      val firstFile: Int = duplicates.head._2 + 1
      val otherUploads = duplicates.tail
      val allOtherUploads = otherUploads.map(_._2 + 1)
      val insetTextMsg = allOtherUploads.size match {
        case 1 => messages("otherReason.uploadList.duplicateInsetText", firstFile, allOtherUploads.head)
        case 2 => messages("otherReason.uploadList.duplicateInsetText.2", firstFile, allOtherUploads.head, allOtherUploads(1))
        case 3 => messages("otherReason.uploadList.duplicateInsetText.3", firstFile, allOtherUploads.head, allOtherUploads(1), allOtherUploads(2))
        case 4 => messages("otherReason.uploadList.duplicateInsetText.4", firstFile, allOtherUploads.head, allOtherUploads(1), allOtherUploads(2), allOtherUploads(3))
      }
      Some(govukInsetText(InsetText(content = Text(insetTextMsg))))
    }

  }
}
