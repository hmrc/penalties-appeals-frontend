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

object PenaltySelectionMessages {

  val title = "There are 2 penalties for this VAT charge - Appeal a VAT penalty - GOV.UK"

  val heading = "There are 2 penalties for this VAT charge"

  val p1 = "These are:"

  val firstPenalty = (penaltyAmount: String) => s"£$penaltyAmount first late payment penalty"

  val secondPenalty = (penaltyAmount: String) => s"£$penaltyAmount second late payment penalty"

  val p2 = "You can do a combined appeal if the reason you did not pay VAT on time is the same for both penalties."

  val p2Agent = "You can do a combined appeal if the reason your client did not pay VAT on time is the same for both penalties."

  val formHeading = "Do you intend to appeal both penalties for the same reason?"

  val yesOption = "Yes"

  val noOption = "No"

  val continueBtn = "Continue"

}
