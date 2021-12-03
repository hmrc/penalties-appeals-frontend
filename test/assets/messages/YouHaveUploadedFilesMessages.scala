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

package messages

object YouHaveUploadedFilesMessages {

  val titleSingleFile = "You have uploaded 1 document - Appeal a VAT penalty - GOV.UK"

  val h1SingleFile = "You have uploaded 1 document"

  val titleMultipleFiles = "You have uploaded 2 documents - Appeal a VAT penalty - GOV.UK"

  val h1MultipleFiles = "You have uploaded 2 documents"

  val titleMaxFiles = "You have uploaded 5 documents - Appeal a VAT penalty - GOV.UK"

  val h1MaxFiles = "You have uploaded 5 documents"

  val uploadAnotherFile = "Do you want to upload another document?"

  val yesOption = "Yes"

  val noOption = "No"

  val continueButton = "Continue"

  val insetTextMsg = (item1: String, item2: String) => s"Document $item1 has the same contents as Document $item2." +
    s" You can remove duplicate documents using the ‘Remove‘ link."

  val multipleDuplicatesInsetTextMsg  = (item1: String) => s"$item1 of the documents you have uploaded have the same contents." +
    s" You can remove duplicate documents using the ‘Remove‘ link."
}
