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

package views

import base.{BaseSelectors, SpecBase}
import messages.AppealCoverBothPenaltiesMessages._
import models.NormalMode
import models.pages.{AppealCoverBothPenaltiesPage, PageMode}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealCoverBothPenaltiesPage

class AppealCoverBothPenaltiesPageSpec extends SpecBase with ViewBehaviours {
  val appealCoverBothPenaltiesPage: AppealCoverBothPenaltiesPage = injector.instanceOf[AppealCoverBothPenaltiesPage]
  object Selectors extends BaseSelectors {
    val p = "p.govuk-body"
  }
  def applyView(): HtmlFormat.Appendable = {
    appealCoverBothPenaltiesPage.apply(PageMode(AppealCoverBothPenaltiesPage, NormalMode),
      controllers.routes.AppealStartController.onPageLoad().url)(messages, appConfig, userRequestWithCorrectKeys)
  }
  "AppealCoverBothPenaltiesPage" should {
    implicit val doc: Document = asDocument(applyView())
    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.p -> p,
      Selectors.button -> button
    )
    behave like pageWithExpectedMessages(expectedContent)
  }
}
