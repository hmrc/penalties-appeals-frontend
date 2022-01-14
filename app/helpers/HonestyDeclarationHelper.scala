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
import play.api.i18n.Messages
import utils.MessageRenderer.{didClientCauseLateSubmission, getMessage}
import utils.SessionKeys

object HonestyDeclarationHelper {
  def getReasonText(reasonableExcuse: String)(implicit messages: Messages, user: UserRequest[_]): String = {
    reasonableExcuse match {
      case _ if (user.isAgent && didClientCauseLateSubmission &&
        user.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Submission.toString) &&
        !reasonableExcuse.equals("other")) => messages(s"agent.honestyDeclaration.$reasonableExcuse")
      case excuse if excuse == "crime" || excuse == "bereavement" => getMessage(s"honestyDeclaration.$reasonableExcuse")
      case _ => messages(s"honestyDeclaration.$reasonableExcuse")
    }
  }

  def getExtraText(reasonableExcuse: String): Seq[String] = {
    reasonableExcuse match {
      case "lossOfStaff" => Seq("honestyDeclaration.li.extra.lossOfStaff")
      case "health" => Seq("honestyDeclaration.li.extra.health")
      case _ => Seq()
    }
  }
}
