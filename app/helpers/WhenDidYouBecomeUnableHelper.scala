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

package helpers

import models.{PenaltyTypeEnum, UserRequest}
import utils.SessionKeys

object WhenDidYouBecomeUnableHelper {
  def getPageHeadingMessageKey(msgPrefix: String)(implicit userRequest: UserRequest[_]): String = {
    val isLPPAppeal = {
      userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment.toString) ||
        userRequest.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Additional.toString)
    }
    val clientMissedDeadline = userRequest.session.get(SessionKeys.whatCausedYouToMissTheDeadline).contains("client")
    val clientIntendedToSubmit = userRequest.session.get(SessionKeys.whoPlannedToSubmitVATReturn).contains("client")
    val isAgent = userRequest.isAgent
    (isLPPAppeal, clientMissedDeadline, clientIntendedToSubmit, isAgent) match {
      case (true, _, _, true) => s"agent.$msgPrefix.lpp"
      case (true, _, _, false) => s"$msgPrefix.lpp"
      case (_, false, true, true) => s"agent.$msgPrefix.clientIntendedToSubmit"
      case (_, true, false, true) => s"agent.$msgPrefix.clientMissedDeadline"
      case _ => s"$msgPrefix.lsp"
    }
  }
}
