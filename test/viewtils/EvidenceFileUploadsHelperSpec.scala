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

package viewtils

import base.SpecBase
import models.NormalMode
import models.upload._
import org.jsoup.Jsoup
import play.twirl.api.Html
import views.html.components.upload.uploadList

import java.time.LocalDateTime

class EvidenceFileUploadsHelperSpec extends SpecBase {

  val uploadList = injector.instanceOf[uploadList]
  class Setup {
    val helper = new EvidenceFileUploadsHelper(uploadList)
  }

  val helper: EvidenceFileUploadsHelper = injector.instanceOf[EvidenceFileUploadsHelper]
  val uploadDetails: UploadDetails = UploadDetails(
    fileName = "file1.txt",
    fileMimeType = "text/plain",
    uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
    checksum = "check1234",
    size = 2
  )
  val firstUpload: UploadJourney = callbackModel

  val secondUpload: UploadJourney = callbackModel.copy(reference = "ref2",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url2"),
    uploadDetails = Some(uploadDetails.copy(fileName = "file2.txt"))
  )
  val thirdUpload: UploadJourney = callbackModel.copy(reference = "ref3",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url3"),
    uploadDetails = Some(uploadDetails.copy(fileName = "file3.txt"))
  )
  val fourthUpload: UploadJourney = callbackModel.copy(reference = "ref3",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url4"),
    uploadDetails = Some(uploadDetails.copy(fileName = "file4.txt", checksum = "check1235"))
  )

  val uploadAsFailure: UploadJourney = UploadJourney(
    reference = "ref2000",
    fileStatus = UploadStatusEnum.FAILED,
    uploadDetails = None,
    failureDetails = Some(FailureDetails(FailureReasonEnum.UNKNOWN, "message.unknown"))
  )

  val uploadAsWaiting: UploadJourney = UploadJourney(
    reference = "ref2010",
    fileStatus = UploadStatusEnum.WAITING,
    uploadDetails = None,
    failureDetails = None
  )

  val uploadedFiles: Seq[(UploadJourney, Int)] = Seq(
    (firstUpload, 0),
    (secondUpload, 1),
    (thirdUpload, 2)
  )

  val expected = Seq(uploadListRow(0, "file1.txt", "ref1"), uploadListRow(1, "file2.txt", "ref2"), uploadListRow(2, "file3.txt", "ref3"))

  "displayContentForFileUploads" should {
    "return all the rows of files uploaded" in {
      val actualResult: Seq[Html] = helper.getFileUploadRows(uploadedFiles, NormalMode)
      val actualParsedResult = Jsoup.parse(actualResult.mkString.replaceAll(",", ""))
      val expectedResultParsed = Jsoup.parse(expected.mkString.replaceAll(",", ""))
      actualParsedResult.getElementById("document-row-1").html() shouldBe expectedResultParsed.getElementById("document-row-1").html()
      actualParsedResult.getElementById("document-row-2").html() shouldBe expectedResultParsed.getElementById("document-row-2").html()
      actualParsedResult.getElementById("document-row-3").html() shouldBe expectedResultParsed.getElementById("document-row-3").html()
    }
  }
}
