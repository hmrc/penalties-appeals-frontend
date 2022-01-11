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
import forms.HonestyDeclarationForm.honestyDeclarationForm
import messages.HonestyDeclarationMessages._
import models.{PenaltyTypeEnum, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.HonestyDeclarationPage

class HonestyDeclarationPageSpec extends SpecBase with ViewBehaviours {
  "HonestyDeclarationPage" should {
    val honestyDeclarationPage: HonestyDeclarationPage = injector.instanceOf[HonestyDeclarationPage]
    object Selectors extends BaseSelectors

    def applyVATTraderView(form: Form[_],
                  reasonText: String,
                  dueDate: String, startDate: String, endDate: String,
                  extraBullets: Seq[String] = Seq.empty, userRequest: UserRequest[_] = vatTraderUser, isObligation: Boolean = false): HtmlFormat.Appendable = {
      honestyDeclarationPage.apply(form, reasonText, dueDate, startDate, endDate, extraBullets, isObligation)(implicitly, implicitly, userRequest)
    }

    implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, "of reason",
      "1 January 2022", "1 January 2021", "31 January 2021"))

    def applyAgentView(form: Form[_], reasonText: String, dueDate: String, startDate: String, endDate: String, extraBullets: Seq[String] = Seq.empty,
                       userRequest: UserRequest[_] = agentUserSessionKeys, isObligation: Boolean = false): HtmlFormat.Appendable = {
      honestyDeclarationPage.apply(form, reasonText, dueDate, startDate, endDate, extraBullets, isObligation)(implicitly, implicitly, userRequest)
    }

    implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, "of agent context reason",
      "1 January 2022", "1 January 2021", "31 January 2021"))

    "when an agent is on the page" must {

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClient("of agent context reason", "1 January 2022"),
        Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText,
        Selectors.listIndexWithElementIndex(3, 3) -> li3,
        Selectors.button -> acceptAndContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(agentDoc)

      "it is an appeal against an obligation and agent missed the deadline" must {
        implicit val doc: Document = asDocument(applyAgentView(honestyDeclarationForm, "",
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), agentUserSessionKeys.copy()(agentRequest.withSession(
            SessionKeys.whatCausedYouToMissTheDeadline -> "agent")), isObligation = true))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3, 1) -> li1Obligation,
          Selectors.listIndexWithElementIndex(3, 2) -> li2AgentObligation("1 January 2021", "31 January 2021"),
          Selectors.listIndexWithElementIndex(3, 3) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "it is an appeal against an obligation and client missed the deadline" must {
          implicit val doc: Document = asDocument(applyAgentView(honestyDeclarationForm, "",
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq(), isObligation = true))

          val expectedContent = Seq(
            Selectors.title -> title,
            Selectors.h1 -> h1,
            Selectors.pElementIndex(2) -> p1,
            Selectors.listIndexWithElementIndex(3,1) -> li1Obligation,
            Selectors.listIndexWithElementIndex(3, 2) -> li2Obligation("1 January 2021", "31 January 2021"),
            Selectors.listIndexWithElementIndex(3, 3) -> li3,
            Selectors.button -> acceptAndContinueButton
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
      }


      "display the correct variation" when {

        "the client failed to submit" when {

          "the option selected is 'crime'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.crime"),
              "1 January 2022", "1 January 2021", "31 January 2021"))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentText(messages("agent.honestyDeclaration.crime"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'bereavement'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.bereavement"),
              "1 January 2022", "1 January 2021", "31 January 2021"))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentText(messages("agent.honestyDeclaration.bereavement"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }
        }

        "the agent failed to submit" when {

          "the option selected is 'fire or flood'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.fireOrFlood"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.fireOrFlood"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'health'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.health"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.health"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'technical issues'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.technicalIssues"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.technicalIssues"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'loss of staff'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.lossOfStaff"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.lossOfStaff"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }
        }
      }

      "display the correct LPP variation" when {
        "the option selected is 'crime'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.crime"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextLPP(messages("agent.honestyDeclaration.crime"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'bereavement'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.bereavement"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextLPP(messages("agent.honestyDeclaration.bereavement"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'health' and others related" must {
          implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, messages("agent.honestyDeclaration.health"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClientLPP(messages("agent.honestyDeclaration.health"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'other'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView(honestyDeclarationForm, "",
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentOtherLPP("1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }
      }
    }

    "when a VAT trader is on the page " must {

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        Selectors.listIndexWithElementIndex(3, 1) -> li1("of reason", "1 January 2022"),
        Selectors.listIndexWithElementIndex(3, 2) -> li2,
        Selectors.listIndexWithElementIndex(3, 3) -> li3,
        Selectors.button -> acceptAndContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(doc)

      "display the correct variation" when {
        "the option selected is 'crime'" must {
          implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.crime"),
          "1 January 2022", "1 January 2021", "31 January 2021"))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.crime"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'loss of staff'" must {
          implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.lossOfStaff"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq("honestyDeclaration.li.extra.lossOfStaff")))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.lossOfStaff"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> extraLiForLossOfStaff
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'fire or flood'" must {
          implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.fireOrFlood"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq()))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.fireOrFlood"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'technical issues'" must {
          implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.technicalIssues"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq()))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.technicalIssues"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }
      }

      "the option selected is 'bereavement'" must {
        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.bereavement"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq()))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.bereavement"), "1 January 2022")
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the option selected is 'health'" must {
        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.health"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq("honestyDeclaration.li.extra.health")))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.health"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> extraLiForHealth
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the option selected is 'other'" must {
        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.other"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq()))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1Other("1 January 2022")
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the appeal is LPP" must {
        implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")
        val userRequest = UserRequest("123456789")(fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))

        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.crime"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), userRequest))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3, 1) -> li1Lpp(messages("honestyDeclaration.crime"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> li2Lpp,
          Selectors.listIndexWithElementIndex(3, 3) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "it is an appeal against an obligation" must {
        implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")
        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, "",
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), isObligation = true))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3,1) -> li1Obligation,
          Selectors.listIndexWithElementIndex(3, 2) -> li2Obligation("1 January 2021", "31 January 2021"),
          Selectors.listIndexWithElementIndex(3, 3) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "it is a LPP appeal and the reason is technicalIssues" must {
        implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/")
        val userRequest = UserRequest("123456789")(fakeRequest.withSession(
          SessionKeys.reasonableExcuse -> "technicalIssues", SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))
        implicit val doc: Document = asDocument(applyVATTraderView(honestyDeclarationForm, messages("honestyDeclaration.technicalIssues"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), userRequest))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3, 1) -> li1Lpp(messages("honestyDeclaration.technicalIssues"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> li2Lpp,
          Selectors.listIndexWithElementIndex(3, 3) -> liLppTechIssues,
          Selectors.listIndexWithElementIndex(3, 4) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }
  }
}
