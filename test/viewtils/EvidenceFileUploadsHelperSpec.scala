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
import config.featureSwitches.{FeatureSwitching, WarnForDuplicateFiles}
import models.NormalMode
import models.upload._
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

class EvidenceFileUploadsHelperSpec extends SpecBase with FeatureSwitching {
  val insetText = injector.instanceOf[GovukInsetText]
  val uploadList = injector.instanceOf[uploadList]
  class Setup(enableDuplicateWarn: Boolean = true) {
    disableFeatureSwitch(WarnForDuplicateFiles)
    reset(mockUploadJourneyRepository)
    val helper = new EvidenceFileUploadsHelper(insetText, mockUploadJourneyRepository, uploadList)
    if(enableDuplicateWarn) enableFeatureSwitch(WarnForDuplicateFiles)
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
      val actualResult: Seq[Html] = helper.displayContentForFileUploads(uploadedFiles, NormalMode)
      val actualParsedResult = Jsoup.parse(actualResult.mkString.replaceAll(",", ""))
      val expectedResultParsed = Jsoup.parse(expected.mkString.replaceAll(",", ""))
      actualParsedResult.getElementById("document-row-1").html() shouldBe expectedResultParsed.getElementById("document-row-1").html()
      actualParsedResult.getElementById("document-row-2").html() shouldBe expectedResultParsed.getElementById("document-row-2").html()
      actualParsedResult.getElementById("document-row-3").html() shouldBe expectedResultParsed.getElementById("document-row-3").html()
    }
  }

  "getInsetTextForUploadsInRepository" should {
    "WarnForDuplicateFiles is enabled" when {
      "return None when no duplicates exist" in new Setup {
      when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(firstUpload))))
      val result = await(helper.getInsetTextForUploadsInRepository("123456"))
      result.isEmpty shouldBe true
    }

      "return multiple duplicate sets when there is more than 1 set of duplicates and WarnForDuplicateFiles is enabled" in new Setup {
      when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, fourthUpload, fourthUpload))))
      val result = await(helper.getInsetTextForUploadsInRepository("123456"))
      result.isDefined shouldBe true
      result.get shouldBe "Some of the files have the same contents. Check your uploaded files and remove duplicates using the ’Remove’ link."
    }

      "return the 1 set of duplicates variation text" when {
        "there is 1 duplicate" in new Setup {
          when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload))))
          val result = await(helper.getInsetTextForUploadsInRepository("123456"))
          result.isDefined shouldBe true
          result.get shouldBe "File 1 has the same contents as File 2. You can remove duplicate files using the ’Remove’ link."
        }

        "there is 2 duplicates" in new Setup {
          when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload))))
          val result = await(helper.getInsetTextForUploadsInRepository("123456"))
          result.isDefined shouldBe true
          result.get shouldBe "File 1 has the same contents as Files 2 and 3. You can remove duplicate files using the ’Remove’ link."
        }

        "there is 3 duplicates" in new Setup {
          when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload, secondUpload))))
          val result = await(helper.getInsetTextForUploadsInRepository("123456"))
          result.isDefined shouldBe true
          result.get shouldBe "File 1 has the same contents as Files 2, 3 and 4. You can remove duplicate files using the ’Remove’ link."
        }

        "there is 4 duplicates" in new Setup {
          when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
            .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, secondUpload, secondUpload, secondUpload))))
          val result = await(helper.getInsetTextForUploadsInRepository("123456"))
          result.isDefined shouldBe true
          result.get shouldBe "File 1 has the same contents as Files 2, 3, 4 and 5. You can remove duplicate files using the ’Remove’ link."
        }
      }

      "return the correct wording for when there is file with errors (in between files that are duplicates)" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, uploadAsFailure, secondUpload, secondUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "File 1 has the same contents as Files 3, 4 and 5. You can remove duplicate files using the ’Remove’ link."
      }

      "return the correct wording for when there is files waiting to be uploaded (in between files that are duplicates)" in new Setup {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, uploadAsWaiting, secondUpload, secondUpload, secondUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isDefined shouldBe true
        result.get shouldBe "File 1 has the same contents as Files 3, 4 and 5. You can remove duplicate files using the ’Remove’ link."
      }
    }
    "WarnForDuplicateFiles is disabled" when {
      "return None when duplicates exist" in new Setup(false) {
        when(mockUploadJourneyRepository.getUploadsForJourney(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(Seq(firstUpload, secondUpload, fourthUpload, fourthUpload))))
        val result = await(helper.getInsetTextForUploadsInRepository("123456"))
        result.isEmpty shouldBe true
      }
    }
  }

  "getInsetTextForUploads" should {
    "WarnForDuplicateFiles is enabled" must {
      "return None when no duplicates exist" in new Setup {
        val result = helper.getInsetTextForUploads(Seq((firstUpload, 0)))
        result.isEmpty shouldBe true
      }

      "return an inset text component" when {
        "multiple duplicate sets exist when there is more than 1 set of duplicates" in new Setup {
          val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (fourthUpload, 1), (fourthUpload, 2), (firstUpload, 3)))
          result.isDefined shouldBe true
          val parsedResult = Jsoup.parse(result.get.toString())
          parsedResult.select(".govuk-inset-text").text() shouldBe "Some of the files have the same contents. Check your uploaded files and remove duplicates using the ’Remove’ link."
        }

        "1 set of duplicates exists - showing 1 set of duplicates variation text" when {
          "there is 1 duplicate" in new Setup {
            val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1)))
            result.isDefined shouldBe true
            val parsedResult = Jsoup.parse(result.get.toString())
            parsedResult.select(".govuk-inset-text").text() shouldBe "File 1 has the same contents as File 2. You can remove duplicate files using the ’Remove’ link."
          }

          "there is 2 duplicates" in new Setup {
            val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2)))
            result.isDefined shouldBe true
            val parsedResult = Jsoup.parse(result.get.toString())
            parsedResult.select(".govuk-inset-text").text() shouldBe "File 1 has the same contents as Files 2 and 3. You can remove duplicate files using the ’Remove’ link."
          }

          "there is 3 duplicates" in new Setup {
            val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3)))
            result.isDefined shouldBe true
            val parsedResult = Jsoup.parse(result.get.toString())
            parsedResult.select(".govuk-inset-text").text() shouldBe "File 1 has the same contents as Files 2, 3 and 4. You can remove duplicate files using the ’Remove’ link."
          }

          "there is 4 duplicates" in new Setup {
            val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3), (firstUpload, 4)))
            result.isDefined shouldBe true
            val parsedResult = Jsoup.parse(result.get.toString())
            parsedResult.select(".govuk-inset-text").text() shouldBe "File 1 has the same contents as Files 2, 3, 4 and 5. You can remove duplicate files using the ’Remove’ link."
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
            result.get shouldBe "Some of the files have the same contents. Check your uploaded files and remove duplicates using the ’Remove’ link."
          }

          "there is one set of duplicates" when {
            "there is 1 duplicate" in new Setup {
              val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1)))
              result.isDefined shouldBe true
              result.get shouldBe "File 1 has the same contents as File 2. You can remove duplicate files using the ’Remove’ link."
            }

            "there is 2 duplicates" in new Setup {
              val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2)))
              result.isDefined shouldBe true
              result.get shouldBe "File 1 has the same contents as Files 2 and 3. You can remove duplicate files using the ’Remove’ link."
            }

            "there is 3 duplicates" in new Setup {
              val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3)))
              result.isDefined shouldBe true
              result.get shouldBe "File 1 has the same contents as Files 2, 3 and 4. You can remove duplicate files using the ’Remove’ link."
            }

            "there is 4 duplicates" in new Setup {
              val result = helper.getInsetTextMessage(Seq((firstUpload, 0), (firstUpload, 1), (firstUpload, 2), (firstUpload, 3), (firstUpload, 4)))
              result.isDefined shouldBe true
              result.get shouldBe "File 1 has the same contents as Files 2, 3, 4 and 5. You can remove duplicate files using the ’Remove’ link."
            }
          }
        }
      }
    }
    "WarnForDuplicateFiles is disabled" must {
      "return None when duplicates exist" in new Setup(false) {
        val result = helper.getInsetTextForUploads(Seq((firstUpload, 0), (fourthUpload, 1), (fourthUpload, 2), (firstUpload, 3)))
        result.isEmpty shouldBe true
      }
    }
  }
}
