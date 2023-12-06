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

package messages.findOutHowToAppeal

object ActionsToTakeBeforeAppealingOnlineMessages {

  object TraderMessages {

    val title = "Actions to take before you can appeal online - Appeal a VAT penalty - GOV.UK"

    def li1(start: String, end: String) = s"Submit your VAT Return for the period $start to $end."

    val li2 = "Wait up to 24 hours for the VAT Return to show on your VAT account."

    val li2LPP = "Pay any VAT you owe for the period."

    val li3LPP = "Wait for the payment to clear – this can take up to 5 days depending on the payment method you choose."

    val li3 = "Return to your VAT penalties and appeals page. There will be an appeal link next to any penalties you can appeal online."

    val detailsP1 = "You can send a letter to HMRC to ask for a penalty review, even if you have not submitted a return or paid your VAT."

    val detailsP2 = "Asking for a review by letter will not stop late payment interest building up on your account. You might also get additional penalties."

    val returnLink = "Return to your VAT penalties"

  }

  object AgentMessages {

    val title = "Actions to take before you can appeal online - Agent - Appeal a VAT penalty - GOV.UK"

    def li1(start: String, end: String) = s"Submit your client’s VAT Return for the period $start to $end."

    val li2LPP = "Ask your client to pay any VAT they owe for the period."

    val li2 = "Wait up to 24 hours for the VAT Return to show on your client’s VAT account."

    val li3 = "Return to your client’s VAT penalties and appeals page. There will be an appeal link next to any penalties you can appeal online."

    val li3LPP = "Wait for the payment to clear – this can take up to 5 days depending on the payment method your client used."

    val detailsP1 = "You can send a letter to HMRC to ask for a penalty review, even if your client has not submitted a return or paid their VAT."

    val detailsP2 = "Asking for a review by letter will not stop late payment interest building up on your client’s account. They might also get additional penalties."

    val returnLink = "Return to your client’s VAT penalties"

  }

  val h1 = "Actions to take before you can appeal online"

  val details = "Other ways to appeal"

  val address1 = "Write to:"

  val address2 = "Solicitor’s Office and Legal Services"

  val address3 = "HMRC"

  val address4 = "BX9 1ZT"

  val detailsP3 = "Alternatively, you can appeal to the tax tribunal (opens in a new tab)."

  val taxTribunalLink = "appeal to the tax tribunal (opens in a new tab)"
}
