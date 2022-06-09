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
import forms.WhenDidBecomeUnableForm
import messages.WhenDidBecomeUnableMessages._
import models.pages.{PageMode, WhenDidBecomeUnablePage}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.WhenDidBecomeUnablePage

class WhenDidBecomeUnablePageSpec extends SpecBase with ViewBehaviours {
  "WhenDidBecomeUnablePage" should {
    val whenDidBecomeUnablePage: WhenDidBecomeUnablePage = injector.instanceOf[WhenDidBecomeUnablePage]
    object Selectors extends BaseSelectors

    def applyVATTraderView(form: Form[_], userRequest: UserRequest[_] = vatTraderLSPUserRequest): HtmlFormat.Appendable = whenDidBecomeUnablePage.apply(form,
      controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(NormalMode), pageMode = PageMode(WhenDidBecomeUnablePage, NormalMode))(messages, appConfig, userRequest)

    val vatTraderFormProvider = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, vatTraderLSPUserRequest)


    def applyAgentView(form: Form[_], userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys): HtmlFormat.Appendable = whenDidBecomeUnablePage.apply(form,
      controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(NormalMode), pageMode = PageMode(WhenDidBecomeUnablePage, NormalMode))(messages, appConfig, userRequest)

    val agentFormProvider = WhenDidBecomeUnableForm.whenDidBecomeUnableForm()(messages, agentUserAgentSubmitButClientWasLateSessionKeys)

    "when a VAT trader is on the page" when {
      "appealing a LSP" must {
        implicit val doc: Document = asDocument(applyVATTraderView(vatTraderFormProvider))

        val expectedContent = Seq(
          Selectors.title -> titleLSP,
          Selectors.h1 -> headingLSP,
          Selectors.hintText -> hintText,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "appealing a LPP" must {
        implicit val doc: Document = asDocument(applyVATTraderView(vatTraderFormProvider, vatTraderLPPUserRequest))

        val expectedContent = Seq(
          Selectors.title -> titleLPP,
          Selectors.h1 -> headingLPP,
          Selectors.hintText -> hintText,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }

    "when an agent is on the page" when {
      "appealing a LSP" when {
        "the client planned to submit" must {
          implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserAgentClientPlannedToSubmitSessionKeys))

          val expectedContent = Seq(
            Selectors.title -> titleAgentClientSubmit,
            Selectors.h1 -> headingAgentClientSubmit,
            Selectors.hintText -> hintText,
            Selectors.dateEntry(1) -> dayEntry,
            Selectors.dateEntry(2) -> monthEntry,
            Selectors.dateEntry(3) -> yearEntry,
            Selectors.button -> continueButton
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the agent planned to submit" when {
          "the client didn't get the information on time" must {
            implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserAgentSubmitButClientWasLateSessionKeys))

            val expectedContent = Seq(
              Selectors.title -> titleAgentSubmitClientLate,
              Selectors.h1 -> headingAgentSubmitClientLate,
              Selectors.hintText -> hintText,
              Selectors.dateEntry(1) -> dayEntry,
              Selectors.dateEntry(2) -> monthEntry,
              Selectors.dateEntry(3) -> yearEntry,
              Selectors.button -> continueButton
            )

            behave like pageWithExpectedMessages(expectedContent)(doc)
          }

          "the agent missed the deadline" must {
            implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserAgentMissedSessionKeys))

            val expectedContent = Seq(
              Selectors.title -> titleLSP,
              Selectors.h1 -> headingLSP,
              Selectors.hintText -> hintText,
              Selectors.dateEntry(1) -> dayEntry,
              Selectors.dateEntry(2) -> monthEntry,
              Selectors.dateEntry(3) -> yearEntry,
              Selectors.button -> continueButton
            )

            behave like pageWithExpectedMessages(expectedContent)(doc)
          }
        }
      }

      "Appealing a LPP" must {
        implicit val doc: Document = asDocument(applyAgentView(agentFormProvider, agentUserLPP))

        val expectedContent = Seq(
          Selectors.title -> titleAgentLPP,
          Selectors.h1 -> headingAgentLPP,
          Selectors.hintText -> hintText,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }
  }
}
