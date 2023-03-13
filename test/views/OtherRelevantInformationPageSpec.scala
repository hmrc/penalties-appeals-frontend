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
import forms.OtherRelevantInformationForm
import messages.OtherRelevantInformationMessages._
import models.pages.{OtherRelevantInformationPage, PageMode}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.obligation.OtherRelevantInformationPage

class OtherRelevantInformationPageSpec extends SpecBase with ViewBehaviours {
  "OtherRelevantInformationPage" should {
    val otherRelevantInformationPage: OtherRelevantInformationPage = injector.instanceOf[OtherRelevantInformationPage]
    object Selectors extends BaseSelectors {
      val label = "#main-content .govuk-label--l"
    }
    val postAction: Call = controllers.routes.AppealAgainstObligationController.onSubmit(NormalMode)
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys
    def applyView(form: Form[_]): HtmlFormat.Appendable = otherRelevantInformationPage.apply(form, postAction, pageMode = PageMode(OtherRelevantInformationPage, NormalMode))

    val formProvider = OtherRelevantInformationForm.otherRelevantInformationForm
    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.label -> heading,
      Selectors.hintText -> hintText,
      Selectors.button -> continueBtn
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
