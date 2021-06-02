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

import base.SpecBase
import models.{CheckMode, NormalMode}
import models.pages._

class NavigationSpec extends SpecBase {
  val navigation = injector.instanceOf[Navigation]

  "nextPage" should {
    "in CheckMode" when {
      s"called with $HasCrimeBeenReportedPage" in {
        val result = navigation.nextPage(HasCrimeBeenReportedPage, CheckMode, None)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidCrimeHappenPage" in {
        val result = navigation.nextPage(WhenDidCrimeHappenPage, CheckMode, None)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "in NormalMode" when {
      s"called with $HasCrimeBeenReportedPage" in {
        val result = navigation.nextPage(HasCrimeBeenReportedPage, NormalMode, None)
        result.url shouldBe controllers.routes.CheckYourAnswersController.onPageLoad().url
      }

      s"called with $WhenDidCrimeHappenPage" in {
        val result = navigation.nextPage(WhenDidCrimeHappenPage, NormalMode, None)
        result.url shouldBe controllers.routes.CrimeReasonController.onPageLoadForHasCrimeBeenReported(NormalMode).url
      }
    }
  }
}
