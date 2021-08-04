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

  val li1: (String, String) => String = (reasonText: String, dueDate: String) => s"because $reasonText, I was unable to submit the VAT Return due on $dueDate"

  val li1Lpp: (String, String) => String = (reasonText: String, dueDate: String) => s"because $reasonText, I was unable to pay the VAT bill due on $dueDate"

  val li1Other: String => String = (dueDate: String) => s"I was unable to submit the VAT Return due on $dueDate"

  val li1AgentOtherLPP: String => String = (dueDate: String) => s"my client was unable to pay the VAT bill due on $dueDate"

  val li1Obligation = "HMRC has been asked to cancel the VAT registration"

  val li1AgentText = (reasonText: String, dueDate: String) => s"because $reasonText, they were unable to submit the VAT Return due on $dueDate"

  val li1AgentTextLPP = (reasonText: String, dueDate: String) => s"because $reasonText, they were unable to pay the VAT bill due on $dueDate"

  val li1AgentTextMyClient = (reasonText: String, dueDate: String) => s"because $reasonText, my client was unable to submit the VAT Return due on $dueDate"

  val li1AgentTextMyClientLPP = (reasonText: String, dueDate: String) => s"because $reasonText, my client was unable to pay the VAT bill due on $dueDate"

  val li2 = "no one else was available to make the submission for me"

  val li2AgentText = "no one else was available to make the submission for them"

  val li2AgentTextLPP = "no one else was available to make the payment for them"

  val li2Lpp = "no one else was available to make the payment for me"

  val liLppTechIssues = "the technical failure was not due to lack of funds"

  val li2Obligation: (String, String) => String = (startDate: String, endDate: String) => s"there was no VAT Return due for the period $startDate to $endDate"

  val li3 = "I will provide honest and accurate information in this appeal"

  val extraLiForLossOfStaff = "the staff member did not return or get replaced before the due date"

  val extraLiForHealth = "the timing of the health issue was unexpected"

  val acceptAndContinueButton = "Accept and continue"
}
