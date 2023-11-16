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
import forms.AppealAfterPaymentPlanSetUpForm
import messages.AppealAfterPaymentPlanSetUpMessages._
import models.pages.{AppealAfterPaymentPlanSetUpPage, PageMode}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AppealAfterPaymentPlanSetUpPage
import viewtils.RadioOptionHelper

class AppealAfterPaymentPlanSetUpPageSpec extends SpecBase with ViewBehaviours {
  "YouCanAppealPenaltyPage" should {
    implicit val request: UserRequest[AnyContent] = userRequestWithCorrectKeys

    val appealAfterPaymentPlanSetUpPage: AppealAfterPaymentPlanSetUpPage = injector.instanceOf[AppealAfterPaymentPlanSetUpPage]
    object Selectors extends BaseSelectors {
      val p = "p.govuk-body"
      val noOptionHint = "#value-2-item-hint"


    }
    val formProvider = AppealAfterPaymentPlanSetUpForm.appealAfterPaymentPlanSetUpForm
    val radioOptions = RadioOptionHelper.radioOptionsForPaymentPlanSetUpPage(formProvider)

    def applyView(form: Form[_]): HtmlFormat.Appendable = appealAfterPaymentPlanSetUpPage
      .apply(form, radioOptions,
        controllers.routes.AppealAfterPaymentPlanSetUpController.onSubmit(),
        pageMode = PageMode(AppealAfterPaymentPlanSetUpPage, NormalMode))

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.p -> p1,
      Selectors.legend -> radioTitle,
      Selectors.labelForRadioButton(1) -> radioOption1,
      Selectors.labelForRadioButton(2) -> radioOption2,
      Selectors.noOptionHint -> radioOption2HintText,
      Selectors.button -> submitButton
    )
    behave like pageWithExpectedMessages(expectedContent)
  }
}
