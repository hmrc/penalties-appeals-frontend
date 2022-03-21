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
import forms.WhenDidHealthIssueHappenForm
import messages.WhenDidHealthReasonHappenMessages._
import models.pages.{PageMode, WhenDidHealthIssueHappenPage}
import models.{NormalMode, PenaltyTypeEnum, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.health.WhenDidHealthReasonHappenPage

import java.time.LocalDate

class WhenDidHealthReasonHappenPageSpec extends SpecBase with ViewBehaviours {

  val whenHealthReasonHappenedPage: WhenDidHealthReasonHappenPage = injector.instanceOf[WhenDidHealthReasonHappenPage]

  object Selectors extends BaseSelectors

  def applyVATTraderView(form: Form[_], userRequest: UserRequest[_] = vatTraderLSPUserRequest): HtmlFormat.Appendable = whenHealthReasonHappenedPage.apply(form,
    controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(NormalMode), pageMode = PageMode(WhenDidHealthIssueHappenPage, NormalMode))(implicitly, implicitly, userRequest)

  val vatTraderFormProvider: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, vatTraderLSPUserRequest)

  def applyAgentView(form: Form[_], userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys): HtmlFormat.Appendable = whenHealthReasonHappenedPage.apply(form,
    controllers.routes.HealthReasonController.onSubmitForWhenHealthReasonHappened(NormalMode), pageMode = PageMode(WhenDidHealthIssueHappenPage, NormalMode))(implicitly, implicitly, userRequest)

  val agentFormProvider: Form[LocalDate] = WhenDidHealthIssueHappenForm.whenHealthIssueHappenedForm()(messages, agentUserAgentSubmitButClientWasLateSessionKeys)

  "WhenDidHealthReasonHappenPage" should {

    "when a VAT trader is on the page" must {

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

      behave like pageWithExpectedMessages(expectedContent)


      "display the LPP variation when the appeal is for a LPP" must {
        val userRequest = UserRequest("123456789")(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))
        implicit val doc: Document = asDocument(applyVATTraderView(vatTraderFormProvider, userRequest))

        val expectedContent = Seq(
          Selectors.title -> titleLPP,
          Selectors.h1 -> headingLPP,
          Selectors.hintText -> hintText,
          Selectors.dateEntry(1) -> dayEntry,
          Selectors.dateEntry(2) -> monthEntry,
          Selectors.dateEntry(3) -> yearEntry,
          Selectors.button -> continueButton
        )

        behave like pageWithExpectedMessages(expectedContent)
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

      "appealing a LPP" must {
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
