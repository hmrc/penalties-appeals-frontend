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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText
import views.html.components.upload.uploadList

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvidenceFileUploadsHelperSpec extends SpecBase {
  val insetText = injector.instanceOf[GovukInsetText]
  val uploadList = injector.instanceOf[uploadList]
  class Setup {
    reset(mockUploadJourneyRepository)
    val helper = new EvidenceFileUploadsHelper(insetText, mockUploadJourneyRepository, uploadList)
  }
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
  val fourthUpload: UploadJourney = UploadJourney(
    reference = "ref3",
    fileStatus = UploadStatusEnum.READY,
    downloadUrl = Some("download.file/url4"),
    uploadDetails = Some(UploadDetails(
      fileName = "file3.txt",
      fileMimeType = "text/plain",
      uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
      checksum = "check1235",
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

  "getInsetTextForUploadsInRepository" should {
    "return None when no duplicates exist" in new Setup {
      when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(firstUpload))))
      val result = await(helper.getInsetTextForUploadsInRepository("123456"))
      result.isEmpty shouldBe true
    }

    "return multiple duplicate sets when there is more than 1 set of duplicates" in new Setup {
      when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, fourthUpload, fourthUpload))))
      val result = await(helper.getInsetTextForUploadsInRepository("123456"))
      result.isDefined shouldBe true
      result.get shouldBe "4 of the documents you have uploaded have the same contents. You can remove duplicate documents using the ’Remove’ link."
    }

    "return the 1 set of duplicates variation text" when {
      "there is 1 duplicate" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "Document 1 has the same contents as Document 2. You can remove duplicate documents using the ’Remove’ link."
      }

      "there is 2 duplicates" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "Document 1 has the same contents as Document 2 and 3. You can remove duplicate documents using the ’Remove’ link."
      }

      "there is 3 duplicates" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "Document 1 has the same contents as Document 2, 3 and 4. You can remove duplicate documents using the ’Remove’ link."
      }

      "there is 4 duplicates" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload, secondUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "Document 1 has the same contents as Document 2, 3, 4 and 5. You can remove duplicate documents using the ’Remove’ link."
      }
    }
  }

  "getInsetTextForUploads" should {
    "return None when no duplicates exist" in new Setup {
      val result = helper.getInsetTextForUploads(Seq((firstUpload, 0)))
      result.isEmpty shouldBe true
    }

    "return an inset text component" when {
      "multiple duplicate sets exist when there is more than 1 set of duplicates" in new Setup {
        val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (fourthUpload, 1), (fourthUpload, 2), (firstUpload, 3)))
        result.isDefined shouldBe true
        val parsedResult = Jsoup.parse(result.get.toString())
        parsedResult.select(".govuk-inset-text").text() shouldBe "4 of the documents you have uploaded have the same contents. You can remove duplicate documents using the ’Remove’ link."
      }

      "1 set of duplicates exists - showing 1 set of duplicates variation text" when {
        "there is 1 duplicate" in new Setup {
          val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1)))
          result.isDefined shouldBe true
          val parsedResult = Jsoup.parse(result.get.toString())
          parsedResult.select(".govuk-inset-text").text() shouldBe "Document 1 has the same contents as Document 2. You can remove duplicate documents using the ’Remove’ link."
        }

        "there is 2 duplicates" in new Setup {
          val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2)))
          result.isDefined shouldBe true
          val parsedResult = Jsoup.parse(result.get.toString())
          parsedResult.select(".govuk-inset-text").text() shouldBe "Document 1 has the same contents as Document 2 and 3. You can remove duplicate documents using the ’Remove’ link."
        }

        "there is 3 duplicates" in new Setup {
          val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3)))
          result.isDefined shouldBe true
          val parsedResult = Jsoup.parse(result.get.toString())
          parsedResult.select(".govuk-inset-text").text() shouldBe "Document 1 has the same contents as Document 2, 3 and 4. You can remove duplicate documents using the ’Remove’ link."
        }

        "there is 4 duplicates" in new Setup {
          val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3), (firstUpload, 4)))
          result.isDefined shouldBe true
          val parsedResult = Jsoup.parse(result.get.toString())
          parsedResult.select(".govuk-inset-text").text() shouldBe "Document 1 has the same contents as Document 2, 3, 4 and 5. You can remove duplicate documents using the ’Remove’ link."
        }
      }
    }

    "getInsetTextMessage" should {
      "return None when there is no duplicates" in new Setup {
        val result = helper.getInsetTextMessage(Seq((firstUpload, 0)))
        result.isEmpty shouldBe true
      }

      "return Some" when {
        "there is multiple sets of duplicates" in new Setup {
          val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (fourthUpload, 2), (fourthUpload, 3)))
          result.isDefined shouldBe true
          result.get shouldBe "4 of the documents you have uploaded have the same contents. You can remove duplicate documents using the ’Remove’ link."
        }

        "there is one set of duplicates" when {
          "there is 1 duplicate" in new Setup {
            val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1)))
            result.isDefined shouldBe true
            result.get shouldBe "Document 1 has the same contents as Document 2. You can remove duplicate documents using the ’Remove’ link."
          }

          "there is 2 duplicates" in new Setup {
            val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2)))
            result.isDefined shouldBe true
            result.get shouldBe "Document 1 has the same contents as Document 2 and 3. You can remove duplicate documents using the ’Remove’ link."
          }

          "there is 3 duplicates" in new Setup {
            val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3)))
            result.isDefined shouldBe true
            result.get shouldBe "Document 1 has the same contents as Document 2, 3 and 4. You can remove duplicate documents using the ’Remove’ link."
          }

          "there is 4 duplicates" in new Setup {
            val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3), (firstUpload, 4)))
            result.isDefined shouldBe true
            result.get shouldBe "Document 1 has the same contents as Document 2, 3, 4 and 5. You can remove duplicate documents using the ’Remove’ link."
          }
        }
      }
    }
  }
}
