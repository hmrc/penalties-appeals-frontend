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

import models.{PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import utils.SessionKeys

import java.time.LocalDate
import scala.util.Try

object PenaltyTypeHelper {
  def convertPenaltyTypeToContentString(penaltyType: PenaltyTypeEnum.Value)(implicit userRequest: UserRequest[_], messages: Messages): String = {
    val isAppealingMultiplePenalties: Boolean = userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties).contains("yes")
    (penaltyType, isAppealingMultiplePenalties) match {
      case (_, true) => messages("penaltyType.latePayment.multiple")
      case (PenaltyTypeEnum.Late_Submission, _) => messages("penaltyType.lateSubmission")
      case (PenaltyTypeEnum.Late_Payment | PenaltyTypeEnum.Additional, _) => messages("penaltyType.latePayment")
      case _ => throw new MatchError(s"[PenaltyTypeHelper][convertPenaltyTypeToContentString] - unknown penalty type $penaltyType")
    }
  }

  def getKeysFromSession()(implicit userRequest: UserRequest[_], messages: Messages): Option[Seq[String]] = {
    Try {
        Seq(
          PenaltyTypeHelper.convertPenaltyTypeToContentString(userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).get),
          ImplicitDateFormatter.dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.startDateOfPeriod).get),
          ImplicitDateFormatter.dateToString(userRequest.answers.getAnswer[LocalDate](SessionKeys.endDateOfPeriod).get)
        )
    }.map {
      Some(_)
    }.getOrElse(None)
  }
}
