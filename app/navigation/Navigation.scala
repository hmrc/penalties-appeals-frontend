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

import models.{CheckMode, Mode, NormalMode}
import models.pages.Page
import models.pages._
import play.api.mvc.Call
import controllers.routes

import javax.inject.Inject

class Navigation @Inject()() {
  lazy val checkingRoutes: Map[Page, Option[String] => Call] = Map(
    HasCrimeBeenReportedPage -> (_ => routes.CheckYourAnswersController.onPageLoad()),
    WhenDidCrimeHappenPage -> (_ => routes.CheckYourAnswersController.onPageLoad())
  )

  lazy val normalRoutes: Map[Page, Option[String] => Call] = Map(
    HasCrimeBeenReportedPage -> (_ => routes.CheckYourAnswersController.onPageLoad()),
    WhenDidCrimeHappenPage -> (_ => routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode))
  )

  def nextPage(page: Page, mode: Mode, answer: Option[String] = None): Call = {
    mode match {
      case CheckMode => {
        //Added answer here so that we can add custom routing to pages that require extra data when answers change
        checkingRoutes(page)(answer)
      }
      case NormalMode => {
        normalRoutes(page)(answer)
      }
    }
  }
}
