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

package views

import base.{BaseSelectors, SpecBase}
import messages.AppealSinglePenaltyMessages._
import models.NormalMode
import models.pages.{AppealSinglePenaltyPage, PageMode}
import org.jsoup.nodes.Document
import views.behaviours.ViewBehaviours
import views.html.AppealSinglePenaltyPage


class AppealSinglePenaltyPageSpec extends SpecBase with ViewBehaviours {
  val page: AppealSinglePenaltyPage = injector.instanceOf[AppealSinglePenaltyPage]
  object Selectors extends BaseSelectors

  implicit val document: Document = asDocument(page(PageMode(AppealSinglePenaltyPage, NormalMode), controllers.routes.AppealStartController.onPageLoad().url, "4.20", true)(userRequestWithCorrectKeys, implicitly, implicitly))

  val expectedContent = Seq(
    Selectors.title -> title,
    Selectors.h1 -> heading,
    Selectors.pElementIndex(3) -> p1,
    Selectors.pElementIndex(4) -> p2,
    Selectors.button -> continueBtn
  )

  behave like pageWithExpectedMessages(expectedContent)
}
