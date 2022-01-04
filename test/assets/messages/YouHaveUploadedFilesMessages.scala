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

package messages

object YouHaveUploadedFilesMessages {

  val titleSingleFile = "You have uploaded 1 file - Appeal a VAT penalty - GOV.UK"

  val h1SingleFile = "You have uploaded 1 file"

  val titleMultipleFiles = "You have uploaded 2 files - Appeal a VAT penalty - GOV.UK"

  val h1MultipleFiles = "You have uploaded 2 files"

  val titleMaxFiles = "You have uploaded 5 files - Appeal a VAT penalty - GOV.UK"

  val h1MaxFiles = "You have uploaded 5 files"

  val uploadAnotherFile = "Do you want to upload another file?"

  val yesOption = "Yes"

  val noOption = "No"

  val continueButton = "Continue"

  val insetTextMsg = (item1: String, item2: String) => s"File $item1 has the same contents as File $item2." +
    s" You can remove duplicate files using the ’Remove’ link."

  val multipleDuplicatesInsetTextMsg  = (item1: String) => s"$item1 of the files you have uploaded have the same contents." +
    s" You can remove duplicate files using the ’Remove’ link."
}
