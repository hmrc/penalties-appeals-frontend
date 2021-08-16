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

package views

import base.{BaseSelectors, SpecBase}
import forms.WhenDidHealthIssueHappenForm
import messages.WhenDidHealthReasonHappenMessages._
import models.{NormalMode, PenaltyTypeEnum, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.health.WhenDidHealthReasonHappenPage

class WhenDidHealthReasonHappenPageSpec extends SpecBase with ViewBehaviours {

  val whenHealthReasonHappenedPage: WhenDidHealthReasonHappenPage = injector.instanceOf[WhenDidHealthReasonHappenPage]

  object Selectors extends BaseSelectors

  def applyVATTraderView(form: Form[_], userRequest: UserRequest[_] = vatTraderUser): HtmlFormat.Appendable = whenHealthReasonHappenedPage.apply(form,
    controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(NormalMode))(implicitly, implicitly, userRequest)

  val vatTraderFormProvider = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, vatTraderUser)

  def applyAgentView(form: Form[_], userRequest: UserRequest[_] = agentUserSessionKeys): HtmlFormat.Appendable = whenHealthReasonHappenedPage.apply(form,
    controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(NormalMode))(implicitly, implicitly, userRequest)

  val agentFormProvider = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserSessionKeys)

  "WhenDidHealthReasonHappenPage" should {

    "when a VAT trader is on the page" must {

      implicit val doc: Document = asDocument(applyVATTraderView(vatTraderFormProvider))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> heading,
        Selectors.hintText -> hintText,
        Selectors.dateEntry(1) -> dayEntry,
        Selectors.dateEntry(2) -> monthEntry,
        Selectors.dateEntry(3) -> yearEntry,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)

      "display the LPP variation when the appeal is for a LPP" must {
        val userRequest = UserRequest("123456789")(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))
        implicit val doc: Document = asDocument(applyVATTraderView(vatTraderFormProvider, userRequest))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> heading,
          Selectors.hintText -> hintTextLpp,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

    }

    "when an agent is on the page must" must {
      implicit val doc: Document = asDocument(applyAgentView(agentFormProvider))

      val expectedContent = Seq(
        Selectors.title -> titleAgentText,
        Selectors.h1 -> headingAgentText,
        Selectors.hintText -> hintTextAgentText,
        Selectors.dateEntry(1) -> dayEntry,
        Selectors.dateEntry(2) -> monthEntry,
        Selectors.dateEntry(3) -> yearEntry,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)

      "display the LPP variation when the appeal is for a LPP" must {
        implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserLPP))

        val expectedContent = Seq(
          Selectors.title -> titleAgentText,
          Selectors.h1 -> headingAgentText,
          Selectors.hintText -> hintTextAgentLpp,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "display the LPP variation when the appeal is for a LPP - Additional" must {
        implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserLPPAdditional))

        val expectedContent = Seq(
          Selectors.title -> titleAgentText,
          Selectors.h1 -> headingAgentText,
          Selectors.hintText -> hintTextAgentLpp,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }

}
