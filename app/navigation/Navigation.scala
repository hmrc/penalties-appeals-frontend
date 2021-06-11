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

package navigation

import config.AppConfig
import models.{CheckMode, Mode, NormalMode, UserRequest}
import models.pages.Page
import models.pages._
import play.api.mvc.Call
import controllers.routes
import helpers.DateTimeHelper
import utils.Logger.logger
import utils.SessionKeys

import java.time.LocalDateTime
import javax.inject.Inject

class Navigation @Inject()(dateTimeHelper: DateTimeHelper,
                           appConfig: AppConfig) {
  lazy val checkingRoutes: Map[Page, (Option[String], UserRequest[_]) => Call] = Map(
    HasCrimeBeenReportedPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode)),
    WhenDidCrimeHappenPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, CheckMode))
  )

  lazy val normalRoutes: Map[Page, (Option[String], UserRequest[_]) => Call] = Map(
    HasCrimeBeenReportedPage -> ((_, request) => routeToMakingALateAppealOrCYAPage(request, NormalMode)),
    WhenDidCrimeHappenPage -> ((_, _) => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode))
  )

  def nextPage(page: Page, mode: Mode, answer: Option[String] = None)(implicit userRequest: UserRequest[_]): Call = {
    mode match {
      case CheckMode => {
        //Added answer here so that we can add custom routing to pages that require extra data when answers change
        checkingRoutes(page)(answer, userRequest)
      }
      case NormalMode => {
        normalRoutes(page)(answer, userRequest)
      }
    }
  }

  def routeToMakingALateAppealOrCYAPage(userRequest: UserRequest[_], mode: Mode): Call = {
    val dateSentParsed: LocalDateTime = LocalDateTime.parse(userRequest.session.get(SessionKeys.dateCommunicationSent).get)
    val daysResultingInLateAppeal: Int = appConfig.daysRequiredForLateAppeal
    val dateTimeNow: LocalDateTime = dateTimeHelper.dateTimeNow
    if(dateSentParsed.isBefore(dateTimeNow.minusDays(daysResultingInLateAppeal))
      && (userRequest.session.get(SessionKeys.lateAppealReason).isEmpty || mode == NormalMode)) {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - " +
        s"Date now: $dateTimeNow :: Date communication sent: $dateSentParsed - redirect to 'Making a Late Appeal' page")
      controllers.routes.MakingALateAppealController.onPageLoad()
    } else {
      logger.debug(s"[Navigation][routeToMakingALateAppealOrCYAPage] - Date now: $dateTimeNow :: Date communication sent: $dateSentParsed - redirect to CYA page")
      controllers.routes.CheckYourAnswersController.onPageLoad()
    }
  }
}
