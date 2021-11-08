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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class EvidenceFileUploadsHelperSpec extends SpecBase {
  val helper: EvidenceFileUploadsHelper = injector.instanceOf[EvidenceFileUploadsHelper]
  val uploadedFiles: Seq[(String, Int)] = Seq(("file1", 0), ("file2", 1), ("file3", 2))

  val expected = Seq(uploadListRow(1),uploadListRow(2),uploadListRow(3))

  "SummaryCard helper" should {
    "displayContentForFileUploads" should {
      "return row of fileList " in {
        val actualResult:Seq[SummaryListRow] = helper.displayContentForFileUploads(uploadedFiles)
        actualResult shouldBe expected
      }
    }
  }
}
