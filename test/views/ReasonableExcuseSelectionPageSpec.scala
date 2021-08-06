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
import forms.ReasonableExcuseForm
import models.{PenaltyTypeEnum, ReasonableExcuse, UserRequest}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.ViewBehaviours
import views.html.ReasonableExcuseSelectionPage
import messages.ReasonableExcuseSelectionMessages._
import utils.SessionKeys

class ReasonableExcuseSelectionPageSpec extends SpecBase with ViewBehaviours {
  "ReasonableExcuseSelectionPage" when {
    val reasonableExcuseSelectionPage: ReasonableExcuseSelectionPage = injector.instanceOf[ReasonableExcuseSelectionPage]
    object Selectors extends BaseSelectors

    def applyView(form: Form[_], seqOfRadioOptions: Seq[RadioItem], request: UserRequest[_]): HtmlFormat.Appendable =
      reasonableExcuseSelectionPage.apply(form, seqOfRadioOptions)(request, implicitly, implicitly)

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
      seqOfReasonableExcuses)


    "an agent is on the page" must {
      implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(fakeRequestWithCorrectKeys)))

      val expectedContent = Seq(
        Selectors.title -> agentTitle,
        Selectors.h1 -> agentH1,
        Selectors.labelForRadioButton(1) -> radioOption1,
        Selectors.labelForRadioButton(2) -> radioOption2,
        Selectors.labelForRadioButton(3) -> radioOption3,
        Selectors.labelForRadioButton(4) -> radioOption4,
        Selectors.labelForRadioButton(5) -> radioOption5,
        Selectors.labelForRadioButton(6) -> radioOption6,
        Selectors.breakerElement -> breakerText,
        Selectors.labelForRadioButton(8) -> radioOption7,
        Selectors.button -> submitButton,
        Selectors.externalGuidanceLink -> externalGuidanceLink
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        //TODO: Change to next page link
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "#"
      }

      "show the correct heading content when appealing a late payment penalty" in {
        implicit val doc = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, agentFakeRequestConverter(
          fakeRequestWithCorrectKeys.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString))
        ))

        doc.select(Selectors.title).text shouldBe title
        doc.select(Selectors.h1).text shouldBe h1
      }
    }

    "a VAT trader is on the page" must {
      implicit val doc: Document = asDocument(applyView(formProvider, seqOfRadioItemsBasedOnReasonableExcuses, userRequestWithCorrectKeys))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.labelForRadioButton(1) -> radioOption1,
        Selectors.labelForRadioButton(2) -> radioOption2,
        Selectors.labelForRadioButton(3) -> radioOption3,
        Selectors.labelForRadioButton(4) -> radioOption4,
        Selectors.labelForRadioButton(5) -> radioOption5,
        Selectors.labelForRadioButton(6) -> radioOption6,
        Selectors.breakerElement -> breakerText,
        Selectors.labelForRadioButton(8) -> radioOption7,
        Selectors.button -> submitButton,
        Selectors.externalGuidanceLink -> externalGuidanceLink
      )

      behave like pageWithExpectedMessages(expectedContent)

      "have a link to external guidance" in {
        //TODO: Change to next page link
        doc.select(Selectors.externalGuidanceLink).attr("href") shouldBe "#"
      }
    }
  }
}
