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
import forms.PenaltySelectionForm
import messages.PenaltySelectionMessages._
import models.NormalMode
import models.pages.{PageMode, PenaltySelectionPage}
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.ViewBehaviours
import views.html.PenaltySelectionPage
import viewtils.RadioOptionHelper


class PenaltySelectionPageSpec extends SpecBase with ViewBehaviours {
  val page: PenaltySelectionPage = injector.instanceOf[PenaltySelectionPage]
  val form: Form[String] = PenaltySelectionForm.doYouWantToAppealBothPenalties
  val yesNoOptions: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(form)
  object Selectors extends BaseSelectors {
    val legend = ".govuk-fieldset__legend"
  }

  implicit val document = asDocument(page(form, yesNoOptions, "50.12", "526.10", PageMode(PenaltySelectionPage, NormalMode))(userRequestWithCorrectKeys, implicitly, implicitly))

  val expectedContent = Seq(
    Selectors.title -> title,
    Selectors.h1 -> heading,
    Selectors.pElementIndex(2) -> p1,
    Selectors.listIndexWithElementIndex(3, 1) -> firstPenalty("50.12"),
    Selectors.listIndexWithElementIndex(3, 2) -> secondPenalty("526.10"),
    Selectors.pElementIndex(4) -> p2,
    Selectors.legend -> formHeading,
    Selectors.labelForRadioButton(1) -> yesOption,
    Selectors.labelForRadioButton(2) -> noOption,
    Selectors.button -> continueBtn
  )

  behave like pageWithExpectedMessages(expectedContent)

  "when agent is on the page" must {
    implicit val document = asDocument(page(form, yesNoOptions, "50.12", "526.10", PageMode(PenaltySelectionPage, NormalMode))(agentFakeRequestConverter(), implicitly, implicitly))

    val expectedContent = Seq(
      Selectors.title -> agentTitle,
      Selectors.h1 -> heading,
      Selectors.pElementIndex(2) -> p1,
      Selectors.listIndexWithElementIndex(3, 1) -> firstPenalty("50.12"),
      Selectors.listIndexWithElementIndex(3, 2) -> secondPenalty("526.10"),
      Selectors.pElementIndex(4) -> p2Agent,
      Selectors.legend -> formHeading,
      Selectors.labelForRadioButton(1) -> yesOption,
      Selectors.labelForRadioButton(2) -> noOption,
      Selectors.button -> continueBtn
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
