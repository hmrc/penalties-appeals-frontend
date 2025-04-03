/*
 * Copyright 2024 HM Revenue & Customs
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

package views.errors

import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import utils.ViewUtils
import views.behaviours.ViewBehaviours
import views.html.errors.{InternalServerErrorGenericPage, InternalServerErrorPage}
import messages.ISECustomMessages._

class InternalServerErrorPageSpec extends SpecBase with ViewBehaviours with ViewUtils {

  val iseCustomPage: InternalServerErrorPage = injector.instanceOf[InternalServerErrorPage]
  val iseCustomGenericPage: InternalServerErrorGenericPage = injector.instanceOf[InternalServerErrorGenericPage]

  object Selector extends BaseSelectors {
    val link = "#main-content p:nth-child(3) a"
    val pNthChild: Int => String = (nThChild: Int) => s"#main-content p:nth-child($nThChild)"
  }

  "InternalServerError" should {

    "display the correct page" when {

      "the user is a trader" must {
        def applyView() = {
          iseCustomPage.apply()(
            implicitly, implicitly, vatTraderUserLSP
          )
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> title,
          Selector.h1 -> heading,
          Selector.pNthChild(2) -> p,
          Selector.link -> link
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "the user is a agent" must {
        def applyView() = {
          iseCustomPage.apply()(
            implicitly, implicitly, agentUserLSP
          )
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> agentTitle,
          Selector.h1 -> heading,
          Selector.pNthChild(2) -> p,
          Selector.link -> agentLink
        )

        behave like pageWithExpectedMessages(expectedContent)
      }

      "it is unknown if user is trader or agent" must {
        def applyView() = {
          iseCustomGenericPage.apply()(
            implicitly, implicitly, implicitly
          )
        }

        implicit val doc: Document = asDocument(applyView())

        val expectedContent = Seq(
          Selector.title -> title,
          Selector.h1 -> heading,
          Selector.pNthChild(2) -> p,
          Selector.link -> linkGeneric
        )

        behave like pageWithExpectedMessages(expectedContent)
      }
    }
  }
}
