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
import forms.ReasonableExcuseForm
import messages.ReasonableExcuseSelectionMessages._
import models.pages.{PageMode, ReasonableExcuseSelectionPage}
import models.{NormalMode, PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.ReasonableExcuseSelectionPage

class ReasonableExcuseSelectionPageSpec extends SpecBase with ViewBehaviours {
  "ReasonableExcuseSelectionPage" when {
    val reasonableExcuseSelectionPage: ReasonableExcuseSelectionPage = injector.instanceOf[ReasonableExcuseSelectionPage]
    object Selectors extends BaseSelectors {
      val formHint = "#value-hint"

      val otherOptionHint = "#value-8-item-hint"
    }

    def applyView(form: Form[_], seqOfRadioOptions: Seq[RadioItem], request: UserRequest[_], showAgentHintText: Boolean = false, showHintText: Boolean = false): HtmlFormat.Appendable =
      reasonableExcuseSelectionPage.apply(form, seqOfRadioOptions, pageMode = PageMode(ReasonableExcuseSelectionPage, NormalMode), showAgentHintText, showHintText)(request, implicitly, implicitly)

    val seqOfReasonableExcuses: Seq[ReasonableExcuse] = Seq(
      ReasonableExcuse(
        `type` = "bereavement",
        descriptionKey = "reasonableExcuses.bereavementReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "crime",
        descriptionKey = "reasonableExcuses.crimeReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "fireOrFlood",
        descriptionKey = "reasonableExcuses.fireOrFloodReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "health",
        descriptionKey = "reasonableExcuses.healthReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "lossOfStaff",
        descriptionKey = "reasonableExcuses.lossOfStaffReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "technicalIssues",
        descriptionKey = "reasonableExcuses.technicalIssuesReason",
        isOtherOption = false
      ),
      ReasonableExcuse(
        `type` = "other",
        descriptionKey = "reasonableExcuses.otherReason",
        isOtherOption = true
      )
    )

    val formProvider = ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`))
    val seqOfRadioItemsBasedOnReasonableExcuses: Seq[RadioItem] = ReasonableExcuse.optionsWithDivider(formProvider,
      "reasonableExcuses.breakerText",
      seqOfReasonableExcuses, showAgentHintText = false, showHintText = true)


    "an agent is on the page" must {
      implicit val doc: Document = asDocument(applyView(
        formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(correctUserAnswers), showHintText = true))

      val expectedContent = Seq(
        Selectors.title -> agentTitle,
        Selectors.h1 -> agentH1,
        Selectors.formHint -> formHintText,
        Selectors.labelForRadioButton(1) -> radioOption1,
        Selectors.labelForRadioButton(2) -> radioOption2,
        Selectors.labelForRadioButton(3) -> radioOption3,
        Selectors.labelForRadioButton(4) -> radioOption4,
        Selectors.labelForRadioButton(5) -> radioOption5,
        Selectors.labelForRadioButton(6) -> radioOption6,
        Selectors.breakerElement -> breakerText,
        Selectors.labelForRadioButton(8) -> radioOption7,
        Selectors.otherOptionHint -> otherOptionHintText,
        Selectors.button -> submitButton
      )

      behave like pageWithExpectedMessages(expectedContent)

      "show the correct heading content when appealing a late payment penalty" in {
        val answers = Json.obj(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment)
        implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(correctUserAnswers ++ answers)))

        doc.getElementsByTag("title").first().text() shouldBe agentTitleLPP
        doc.select(Selectors.h1).text shouldBe h1
      }

      "show the agent wording when the 'showAgentHintText' is true" in {
        val seqOfRadioItemsBasedOnReasonableExcuses: Seq[RadioItem] = ReasonableExcuse.optionsWithDivider(formProvider,
          "reasonableExcuses.breakerText",
          seqOfReasonableExcuses, showAgentHintText = true, showHintText = true)
        implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(correctUserAnswers), showAgentHintText = true, showHintText = true))

        doc.select(Selectors.formHint).text shouldBe formHintTextAgent
        doc.select(Selectors.otherOptionHint).text shouldBe otherOptionHintTextAgent
      }

      "not show the hint text when the feature switch is disabled" in {
        val seqOfRadioItemsBasedOnReasonableExcuses: Seq[RadioItem] = ReasonableExcuse.optionsWithDivider(formProvider,
          "reasonableExcuses.breakerText",
          seqOfReasonableExcuses, showAgentHintText = true, showHintText = false)
        implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(correctUserAnswers), showAgentHintText = true))

        doc.select(Selectors.formHint).isEmpty shouldBe true
        doc.select(Selectors.otherOptionHint).isEmpty shouldBe true
      }
    }

    "a VAT trader is on the page" must {
      implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, userRequestWithCorrectKeys, showHintText = true))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.formHint -> formHintText,
        Selectors.labelForRadioButton(1) -> radioOption1,
        Selectors.labelForRadioButton(2) -> radioOption2,
        Selectors.labelForRadioButton(3) -> radioOption3,
        Selectors.labelForRadioButton(4) -> radioOption4,
        Selectors.labelForRadioButton(5) -> radioOption5,
        Selectors.labelForRadioButton(6) -> radioOption6,
        Selectors.breakerElement -> breakerText,
        Selectors.labelForRadioButton(8) -> radioOption7,
        Selectors.otherOptionHint -> otherOptionHintText,
        Selectors.button -> submitButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "not show the hint text when the feature switch is disabled" in {
      val seqOfRadioItemsBasedOnReasonableExcuses: Seq[RadioItem] = ReasonableExcuse.optionsWithDivider(formProvider,
        "reasonableExcuses.breakerText",
        seqOfReasonableExcuses, showAgentHintText = false, showHintText = false)
      implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(correctUserAnswers), showAgentHintText = true))

      doc.select(Selectors.formHint).isEmpty shouldBe true
      doc.select(Selectors.otherOptionHint).isEmpty shouldBe true
    }
  }
}
