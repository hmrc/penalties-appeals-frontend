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

package views.findOutHowToAppeal

import base.{BaseSelectors, SpecBase}
import messages.findOutHowToAppeal.AppealAfterPaymentPlanProcessedMessages._
import models.NormalMode
import models.pages.{AppealAfterPaymentPlanProcessedPage, PageMode}
import org.jsoup.nodes.Document
import views.behaviours.ViewBehaviours
import views.html.findOutHowToAppeal.AppealAfterPaymentPlanProcessedPage

class AppealAfterPaymentPlanProcessedPageSpec extends SpecBase with ViewBehaviours {

  val page: AppealAfterPaymentPlanProcessedPage = injector.instanceOf[AppealAfterPaymentPlanProcessedPage]

  object Selectors extends BaseSelectors {
    val p1 = "#main-content > div > div > p:nth-child(3)"
    val vatAccountLink = "#view-vat-account-link"
  }

  implicit val doc: Document = asDocument(page(PageMode(AppealAfterPaymentPlanProcessedPage, NormalMode))(implicitly, implicitly, userRequestWithCorrectKeys))

  "AppealAfterPaymentPlanProcessedPage" should {
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.p1 -> p1,
      Selectors.vatAccountLink -> vatAccountLink,
    )

    behave like pageWithExpectedMessages(expectedContent)
  }

}
