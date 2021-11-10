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

import base.SpecBase
import models.NormalMode
import models.upload.{UploadDetails, UploadJourney, UploadStatusEnum}
import org.jsoup.Jsoup
import play.twirl.api.Html

import java.time.LocalDateTime

class EvidenceFileUploadsHelperSpec extends SpecBase {
  val helper: EvidenceFileUploadsHelper = injector.instanceOf[EvidenceFileUploadsHelper]
  val firstUpload: UploadJourney = UploadJourney(
    reference = "ref1",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url1"),
    uploadDetails = Some(UploadDetails(
      fileName = "file1.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    ))
  )
  val secondUpload: UploadJourney = UploadJourney(
    reference = "ref2",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url2"),
    uploadDetails = Some(UploadDetails(
      fileName = "file2.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    ))
  )
  val thirdUpload: UploadJourney = UploadJourney(
    reference = "ref3",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url3"),
    uploadDetails = Some(UploadDetails(
      fileName = "file3.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1234",
      size = 2
    ))
  )
  val uploadedFiles: Seq[(UploadJourney, Int)] = Seq(
    (firstUpload, 0),
    (secondUpload, 1),
    (thirdUpload, 2)
  )

  val expected = Seq(uploadListRow(0, "file1.txt", "ref1"), uploadListRow(1, "file2.txt", "ref2"), uploadListRow(2, "file3.txt", "ref3"))
  "displayContentForFileUploads" should {
    "return all the rows of files uploaded" in {
      val actualResult: Seq[Html] = helper.displayContentForFileUploads(uploadedFiles, NormalMode)(implicitly, userRequestWithCorrectKeys)
      val actualParsedResult = Jsoup.parse(actualResult.mkString.replaceAll(",",""))
      val expectedResultParsed = Jsoup.parse(expected.mkString.replaceAll(",",""))
      actualParsedResult.getElementById("document-row-1").html() shouldBe expectedResultParsed.getElementById("document-row-1").html()
      actualParsedResult.getElementById("document-row-2").html() shouldBe expectedResultParsed.getElementById("document-row-2").html()
      actualParsedResult.getElementById("document-row-3").html() shouldBe expectedResultParsed.getElementById("document-row-3").html()
    }
  }
}
