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
import messages.HonestyDeclarationMessages._
import models.pages.{HonestyDeclarationPage, PageMode}
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.HonestyDeclarationPage

class HonestyDeclarationPageSpec extends SpecBase with ViewBehaviours {
  "HonestyDeclarationPage" should {
    val honestyDeclarationPage: HonestyDeclarationPage = injector.instanceOf[HonestyDeclarationPage]
    object Selectors extends BaseSelectors

    def applyVATTraderView(reasonableExcuse: String,
                           reasonText: String,
                           dueDate: String, startDate: String, endDate: String,
                           extraBullets: Seq[String] = Seq.empty, userRequest: UserRequest[_] = vatTraderLSPUserRequest, isObligation: Boolean = false): HtmlFormat.Appendable = {
      honestyDeclarationPage.apply(reasonableExcuse, reasonText, dueDate, startDate, endDate, extraBullets, isObligation,
        pageMode = PageMode(HonestyDeclarationPage, NormalMode))(implicitly, implicitly, userRequest)
    }

    implicit val doc: Document = asDocument(applyVATTraderView( "technicalIssues", "of reason",
      "1 January 2022", "1 January 2021", "31 January 2021"))

    def applyAgentView(reasonableExcuse: String, reasonText: String, dueDate: String, startDate: String, endDate: String, extraBullets: Seq[String] = Seq.empty,
                       userRequest: UserRequest[_] = agentUserAgentSubmitButClientWasLateSessionKeys, isObligation: Boolean = false): HtmlFormat.Appendable = {
      honestyDeclarationPage.apply(reasonableExcuse, reasonText, dueDate, startDate, endDate, extraBullets, isObligation, pageMode = PageMode(HonestyDeclarationPage, NormalMode))(implicitly, implicitly, userRequest)
    }

    implicit val agentDoc: Document = asDocument(applyAgentView( "technicalIssues", "of agent context reason",
      "1 January 2022", "1 January 2021", "31 January 2021"))

    "when an agent is on the page" must {

      val expectedContent = Seq(
        Selectors.title -> titleAgent,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        Selectors.listIndexWithElementIndex(3, 1) -> li1("of agent context reason", "1 January 2022"),
        Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent,
        Selectors.listIndexWithElementIndex(3, 3) -> li3,
        Selectors.button -> acceptAndContinueButton
      )

      behave like pageWithExpectedMessages(expectedContent)(agentDoc)

      "it is an appeal against an obligation and agent missed the deadline" must {
        implicit val doc: Document = asDocument(applyAgentView("obligation","",
          "1 January 2022", "1 January 2021", "31 January 2021", isObligation = true))

        val expectedContent = Seq(
          Selectors.title -> titleAgent,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3, 1) -> li1Obligation,
          Selectors.listIndexWithElementIndex(3, 2) -> li2Obligation("1 January 2021", "31 January 2021"),
          Selectors.listIndexWithElementIndex(3, 3) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "display the correct variation" when {

        "the agent planned to submit" when {

          "the agent missed the deadline" must {
            "the option selected is 'crime'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("crime", messages("honestyDeclaration.crime"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.crime"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'bereavement'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("bereavement", messages("honestyDeclaration.bereavement"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.bereavement"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'fire or flood'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("fireOrFlood", messages("honestyDeclaration.fireOrFlood"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.fireOrFlood"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'health'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("health", messages("honestyDeclaration.health"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.health"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'technical issues'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("technicalIssues", messages("honestyDeclaration.technicalIssues"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.technicalIssues"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'loss of staff'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("lossOfStaff", messages("honestyDeclaration.lossOfStaff"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.lossOfStaff"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2LossOfStaff
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }


            "the option selected is 'other'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("other", messages("honestyDeclaration.other"),
                "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserAgentMissedSessionKeys))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1Other("1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> li2
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }
          }

          "the client missed the deadline" must {
            "the option selected is 'crime'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("crime",
                messages("agent.honestyDeclaration.crime"), "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.crime"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'bereavement'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("bereavement",
                messages("agent.honestyDeclaration.bereavement"), "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.bereavement"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'fire or flood'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("fireOrFlood",
                messages("agent.honestyDeclaration.fireOrFlood"), "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.fireOrFlood"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'health'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("health", messages("agent.honestyDeclaration.health"),
                "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.health"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'technical issues'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("technicalIssues",
                messages("agent.honestyDeclaration.technicalIssues"), "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.technicalIssues"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }

            "the option selected is 'loss of staff'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("lossOfStaff",
                messages("agent.honestyDeclaration.lossOfStaff"), "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.lossOfStaff"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }


            "the option selected is 'other'" must {
              implicit val agentDoc: Document = asDocument(applyAgentView("other", messages("agent.honestyDeclaration.other"),
                "1 January 2022", "1 January 2021", "31 January 2021"))

              val expectedContent = Seq(
                Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("agent.honestyDeclaration.other"), "1 January 2022"),
                Selectors.listIndexWithElementIndex(3, 2) -> extraLiForAgent
              )

              behave like pageWithExpectedMessages(expectedContent)(agentDoc)
            }
          }
        }

        "the client planned to submit" must {
          val agentUserClientPlannedSessionKeys: UserRequest[AnyContent] = UserRequest("123456789", arn = Some("AGENT1"), answers = userAnswers(correctUserAnswers ++ Json.obj(
              SessionKeys.whoPlannedToSubmitVATReturn -> "client"
          )))
          "the option selected is 'crime'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("crime", messages("agent.honestyDeclaration.crime"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentText(messages("agent.honestyDeclaration.crime"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'bereavement'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("bereavement", messages("agent.honestyDeclaration.bereavement"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentText(messages("agent.honestyDeclaration.bereavement"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'fire or flood'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("fireOrFlood", messages("honestyDeclaration.fireOrFlood"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClient(messages("honestyDeclaration.fireOrFlood"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'health'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("health", messages("agent.honestyDeclaration.health"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClient(messages("agent.honestyDeclaration.health"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'technical issues'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("technicalIssues", messages("agent.honestyDeclaration.technicalIssues"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClient(messages("agent.honestyDeclaration.technicalIssues"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }

          "the option selected is 'loss of staff'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("lossOfStaff", messages("agent.honestyDeclaration.lossOfStaff"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClient(messages("agent.honestyDeclaration.lossOfStaff"), "1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2LossOfStaff
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }


          "the option selected is 'other'" must {
            implicit val agentDoc: Document = asDocument(applyAgentView("other", messages("honestyDeclaration.other"),
              "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserClientPlannedSessionKeys))

            val expectedContent = Seq(
              Selectors.listIndexWithElementIndex(3, 1) -> li1AgentOther("1 January 2022"),
              Selectors.listIndexWithElementIndex(3, 2) -> li2AgentText
            )

            behave like pageWithExpectedMessages(expectedContent)(agentDoc)
          }
        }
      }

      "display the correct LPP variation" when {
        "the option selected is 'crime'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView("crime", messages("agent.honestyDeclaration.crime"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextLPP(messages("agent.honestyDeclaration.crime"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'bereavement'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView("bereavement", messages("agent.honestyDeclaration.bereavement"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextLPP(messages("agent.honestyDeclaration.bereavement"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'health' and others related" must {
          implicit val agentDoc: Document = asDocument(applyAgentView("health", messages("agent.honestyDeclaration.health"),
            "1 January 2022", "1 January 2021", "31 January 2021", userRequest = agentUserLPP))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1AgentTextMyClientLPP(messages("agent.honestyDeclaration.health"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> li2AgentTextLPP
          )

          behave like pageWithExpectedMessages(expectedContent)(agentDoc)
        }

        "the option selected is 'other'" must {
          implicit val agentDoc: Document = asDocument(applyAgentView("other", "",
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
          implicit val doc: Document = asDocument(applyVATTraderView("crime", messages("honestyDeclaration.crime"),
          "1 January 2022", "1 January 2021", "31 January 2021"))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.crime"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'loss of staff'" must {
          implicit val doc: Document = asDocument(applyVATTraderView("lossOfStaff", messages("honestyDeclaration.lossOfStaff"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq("honestyDeclaration.li.extra.lossOfStaff")))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.lossOfStaff"), "1 January 2022"),
            Selectors.listIndexWithElementIndex(3, 2) -> extraLiForLossOfStaff,
            Selectors.listIndexWithElementIndex(3, 3) -> li2LossOfStaff
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'fire or flood'" must {
          implicit val doc: Document = asDocument(applyVATTraderView("fireOrFlood", messages("honestyDeclaration.fireOrFlood"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq()))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.fireOrFlood"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }

        "the option selected is 'technical issues'" must {
          implicit val doc: Document = asDocument(applyVATTraderView("technicalIssues", messages("honestyDeclaration.technicalIssues"),
            "1 January 2022", "1 January 2021", "31 January 2021",
            Seq()))

          val expectedContent = Seq(
            Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.technicalIssues"), "1 January 2022")
          )

          behave like pageWithExpectedMessages(expectedContent)(doc)
        }
      }

      "the option selected is 'bereavement'" must {
        implicit val doc: Document = asDocument(applyVATTraderView("bereavement", messages("honestyDeclaration.bereavement"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq()))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.bereavement"), "1 January 2022")
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the option selected is 'health'" must {
        implicit val doc: Document = asDocument(applyVATTraderView("health", messages("honestyDeclaration.health"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq("honestyDeclaration.li.extra.health")))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1(messages("honestyDeclaration.health"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> extraLiForHealth
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the option selected is 'other'" must {
        implicit val doc: Document = asDocument(applyVATTraderView("other", messages("honestyDeclaration.other"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq()))

        val expectedContent = Seq(
          Selectors.listIndexWithElementIndex(3, 1) -> li1Other("1 January 2022")
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }

      "the appeal is LPP" must {
        implicit val doc: Document = asDocument(applyVATTraderView("crime", messages("honestyDeclaration.crime"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), vatTraderLPPUserRequest))

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
        implicit val doc: Document = asDocument(applyVATTraderView("other", "",
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
        implicit val doc: Document = asDocument(applyVATTraderView("technicalIssues", messages("honestyDeclaration.technicalIssues"),
          "1 January 2022", "1 January 2021", "31 January 2021",
          Seq(), UserRequest("123456789", answers = userAnswers(correctLPPUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "technicalIssues")))))

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

      "it is a LPP appeal and the reason is lossOfStaff" must {
        implicit val doc: Document = asDocument(applyVATTraderView(reasonableExcuse = "lossOfStaff", reasonText = messages("honestyDeclaration.lossOfStaff"),
          dueDate = "1 January 2022", startDate = "1 January 2021", endDate = "31 January 2021",
          extraBullets = Seq("honestyDeclaration.li.extra.lossOfStaff"), userRequest = UserRequest("123456789", answers = userAnswers(correctLPPUserAnswers ++ Json.obj(SessionKeys.reasonableExcuse -> "lossOfStaff")))))

        val expectedContent = Seq(
          Selectors.title -> title,
          Selectors.h1 -> h1,
          Selectors.pElementIndex(2) -> p1,
          Selectors.listIndexWithElementIndex(3, 1) -> li1Lpp(messages("honestyDeclaration.lossOfStaff"), "1 January 2022"),
          Selectors.listIndexWithElementIndex(3, 2) -> extraLiForLossOfStaff,
          Selectors.listIndexWithElementIndex(3, 3) -> li2LossOfStaffLPP,
          Selectors.listIndexWithElementIndex(3, 4) -> li3,
          Selectors.button -> acceptAndContinueButton
        )

        behave like pageWithExpectedMessages(expectedContent)(doc)
      }
    }
  }
}
