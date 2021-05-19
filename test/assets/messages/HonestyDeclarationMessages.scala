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

object HonestyDeclarationMessages {
  val title = "Honesty declaration - Appeal a VAT penalty - GOV.UK"

  val h1 = "Honesty declaration"

  val p1 = "I confirm that:"

  val li1 = (reasonText: String, dueDate: String) => s"because $reasonText, I was unable to submit the VAT Return due on $dueDate"

  val li2 = "no one else was available to make the submission for me"

  val li3 = "I will provide honest and accurate information in this appeal"

  val acceptAndContinueButton = "Accept and continue"
}
