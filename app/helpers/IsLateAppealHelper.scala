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

package helpers

import config.AppConfig
import models.UserRequest
import utils.SessionKeys

import java.time.LocalDate
import javax.inject.Inject

class IsLateAppealHelper @Inject()(dateTimeHelper: DateTimeHelper,
                                   appConfig: AppConfig) {
  def isAppealLate()(implicit userRequest: UserRequest[_]): Boolean = {
    val dateNow: LocalDate = dateTimeHelper.dateNow
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties) match {
      case Some("yes") =>
        val dateOfFirstComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate).get
        val dateOfSecondComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate).get
        dateOfFirstComms.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal)) ||
          dateOfSecondComms.isBefore(dateNow.minusDays(appConfig.daysRequiredForLateAppeal))
      case _ =>
        val dateOfComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.dateCommunicationSent).get
        dateOfComms.isBefore(
          dateNow.minusDays(
            appConfig.daysRequiredForLateAppeal))
    }
  }
}
