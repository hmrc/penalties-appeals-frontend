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

import models.PenaltyTypeEnum
import play.api.i18n.Messages
import play.api.mvc.Request
import utils.SessionKeys

import java.time.LocalDateTime
import scala.util.Try

object PenaltyTypeHelper {
  def convertPenaltyTypeToContentString(penaltyType: String)(implicit messages: Messages): Option[String] = {
    val penaltyAsEnum = PenaltyTypeEnum.withNameOpt(penaltyType)
    penaltyAsEnum match {
      case Some(PenaltyTypeEnum.Late_Submission) => Some(messages("penaltyType.lateSubmission"))
      case Some(PenaltyTypeEnum.Late_Payment) => Some(messages("penaltyType.latePayment"))
      case _ => None
    }
  }

  def getKeysFromSession()(implicit request: Request[_], messages: Messages): Option[Seq[String]] = {
    Try {
      Seq(
        PenaltyTypeHelper.convertPenaltyTypeToContentString(request.session.get(SessionKeys.appealType).get).get,
        ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(request.session.get(SessionKeys.startDateOfPeriod).get)),
        ImplicitDateFormatter.dateTimeToString(LocalDateTime.parse(request.session.get(SessionKeys.endDateOfPeriod).get))
      )
    }.map {
      Some(_)
    }.getOrElse(None)
  }
}
