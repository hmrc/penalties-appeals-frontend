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

import models.upload.{UploadJourney, UploadStatusEnum}
import models.{Mode, UserRequest}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{InsetText, Text}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText
import views.html.components.upload.uploadList

import javax.inject.Inject
import repositories.UploadJourneyRepository

import scala.concurrent.{ExecutionContext, Future}

class EvidenceFileUploadsHelper @Inject()(govukInsetText: GovukInsetText,
                                          uploadJourneyRepository: UploadJourneyRepository,
                                          uploadList: uploadList
                                         )(implicit ec: ExecutionContext) {

  def displayContentForFileUploads(uploadedFiles: Seq[(UploadJourney, Int)], mode: Mode)(implicit messages: Messages, request: UserRequest[_]): Seq[Html] = {
    uploadedFiles.map(uploadWithIndex =>
      uploadList(uploadWithIndex._1.reference, uploadWithIndex._1.uploadDetails.get.fileName, uploadWithIndex._2 + 1, mode)
    )
  }

  def getInsetTextForUploadsInRepository(journeyId: String)(implicit messages: Messages): Future[Option[String]] = {
    uploadJourneyRepository.getUploadsForJourney(Some(journeyId)).map(_.fold[Option[String]](None)(
      uploads => {
        val uploadedFiles: Seq[(UploadJourney, Int)] = uploads.filter(file => file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE).zipWithIndex
        getInsetTextMessage(uploadedFiles)
      }
    ))
  }

  def getInsetTextForUploads(uploads: Seq[(UploadJourney, Int)])(implicit messages: Messages): Option[Html] = {
    getInsetTextMessage(uploads).fold[Option[Html]](None)(insetText => Some(govukInsetText(InsetText(content = Text(insetText)))))
  }

  def getInsetTextMessage(uploadedFiles: Seq[(UploadJourney, Int)])(implicit messages: Messages): Option[String] = {
    val mappedDuplicates: Map[Option[String], Seq[(UploadJourney, Int)]] = uploadedFiles.groupBy(_._1.uploadDetails.map(_.checksum))
    val multipleDuplicates = mappedDuplicates.filter(_._2.size > 1)
    if (multipleDuplicates.isEmpty) {
      None
    } else if (multipleDuplicates.size > 1) {
      val noOfDuplicateDocs = multipleDuplicates.map(_._2.size).sum
      Some(messages("otherReason.uploadList.multipleDuplicateInsetText", noOfDuplicateDocs))
    } else {
      val duplicates: Seq[(UploadJourney, Int)] = mappedDuplicates.filter(duplicate => duplicate._2.size > 1).head._2
      val firstFile: Int = duplicates.head._2 + 1
      val otherUploads: Seq[(UploadJourney, Int)] = duplicates.tail
      val allOtherUploads: Seq[Int] = otherUploads.map(_._2 + 1)
      val insetTextMsg: String = allOtherUploads.size match {
        case 1 => messages("otherReason.uploadList.duplicateInsetText", firstFile, allOtherUploads.head)
        case 2 => messages("otherReason.uploadList.duplicateInsetText.2", firstFile, allOtherUploads.head, allOtherUploads(1))
        case 3 => messages("otherReason.uploadList.duplicateInsetText.3", firstFile, allOtherUploads.head, allOtherUploads(1), allOtherUploads(2))
        case 4 => messages("otherReason.uploadList.duplicateInsetText.4", firstFile, allOtherUploads.head, allOtherUploads(1), allOtherUploads(2), allOtherUploads(3))
      }
      Some(insetTextMsg)
    }
  }
}
