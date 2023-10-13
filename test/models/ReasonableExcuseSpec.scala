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

package models

import base.SpecBase
import forms.ReasonableExcuseForm
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class ReasonableExcuseSpec extends SpecBase {
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

  val seqOfRadioOptions = Seq(
    RadioItem(
      value = Some("bereavement"),
      content = Text(messages("reasonableExcuses.bereavementReason")),
      checked = false
    ),
    RadioItem(
      value = Some("crime"),
      content = Text(messages("reasonableExcuses.crimeReason")),
      checked = false
    ),
    RadioItem(
      value = Some("fireOrFlood"),
      content = Text(messages("reasonableExcuses.fireOrFloodReason")),
      checked = false
    ),
    RadioItem(
      value = Some("health"),
      content = Text(messages("reasonableExcuses.healthReason")),
      checked = false
    ),
    RadioItem(
      value = Some("lossOfStaff"),
      content = Text(messages("reasonableExcuses.lossOfStaffReason")),
      checked = false
    ),
    RadioItem(
      value = Some("technicalIssues"),
      content = Text(messages("reasonableExcuses.technicalIssuesReason")),
      checked = false
    ),
    RadioItem(
      value = Some("other"),
      content = Text(messages("reasonableExcuses.otherReason")),
      checked = false,
      hint = Some(Hint(content = Text(messages("reasonableExcuses.otherReason.hintText"))))
    )
  )

  "singularReads" should {
    s"be able to read one $ReasonableExcuse" in {
      val jsonRepresentingOneReasonableExcuse: JsValue = Json.parse(
        """
          |{
          |   "type": "this_is_a_great_type",
          |   "descriptionKey": "message_in_a_bottle"
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[ReasonableExcuse](jsonRepresentingOneReasonableExcuse)(ReasonableExcuse.singularReads)
      result.isSuccess shouldBe true
      result.get shouldBe ReasonableExcuse(
        `type` = "this_is_a_great_type",
        descriptionKey = "message_in_a_bottle",
        isOtherOption = false
      )
    }

    "set the flag to true - when the type of the excuse is other" in {
      val jsonRepresentingOneReasonableExcuse: JsValue = Json.parse(
        """
          |{
          |   "type": "other",
          |   "descriptionKey": "message_in_a_bottle"
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[ReasonableExcuse](jsonRepresentingOneReasonableExcuse)(ReasonableExcuse.singularReads)
      result.isSuccess shouldBe true
      result.get shouldBe ReasonableExcuse(
        `type` = "other",
        descriptionKey = "message_in_a_bottle",
        isOtherOption = true
      )
    }

    s"fail to parse if the schema does not include the mandatory keys" in {
      val jsonRepresentingInvalidReasonableExcuse: JsValue = Json.parse(
        """
          |{
          |   "typeOfReasonableExcuse3000": "this_is_a_great_type",
          |   "descriptionKey": "message_in_a_bottle"
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[ReasonableExcuse](jsonRepresentingInvalidReasonableExcuse)(ReasonableExcuse.singularReads)
      result.isSuccess shouldBe false
    }
  }

  "seqReads" should {
    s"be able to read a $Seq of $ReasonableExcuse" in {
      val jsonRepresentingSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crimeReason"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[Seq[ReasonableExcuse]](jsonRepresentingSeqOfReasonableExcuses)(ReasonableExcuse.seqReads)
      result.isSuccess shouldBe true
      result.get shouldBe Seq(
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
        )
      )
    }

    s"be able to read a $Seq of $ReasonableExcuse and set the isOtherOption flag when there is an 'option' type in the response" in {
      val jsonRepresentingSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crimeReason"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    },
          |    {
          |      "type": "other",
          |      "descriptionKey": "reasonableExcuses.otherReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[Seq[ReasonableExcuse]](jsonRepresentingSeqOfReasonableExcuses)(ReasonableExcuse.seqReads)
      result.isSuccess shouldBe true
      result.get shouldBe Seq(
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
          `type` = "other",
          descriptionKey = "reasonableExcuses.otherReason",
          isOtherOption = true
        )
      )
    }

    "not parse when the root key is wrong i.e. not 'excuses'" in {
      val jsonRepresentingInvalidSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "many_excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime",
          |      "descriptionKey": "reasonableExcuses.crimeReason"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[Seq[ReasonableExcuse]](jsonRepresentingInvalidSeqOfReasonableExcuses)(ReasonableExcuse.seqReads)
      result.isSuccess shouldBe false
    }

    "not parse when the structure is correct - but there is a key missing in one of the objects" in {
      val jsonRepresentingInvalidSeqOfReasonableExcuses: JsValue = Json.parse(
        """
          |{
          |  "many_excuses": [
          |    {
          |      "type": "bereavement",
          |      "descriptionKey": "reasonableExcuses.bereavementReason"
          |    },
          |    {
          |      "type": "crime"
          |    },
          |    {
          |      "type": "fireOrFlood",
          |      "descriptionKey": "reasonableExcuses.fireOrFloodReason"
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[Seq[ReasonableExcuse]](jsonRepresentingInvalidSeqOfReasonableExcuses)(ReasonableExcuse.seqReads)
      result.isSuccess shouldBe false
    }
  }

  "options" should {
    "return a Seq of RadioItems that will be passed to the view - no pre-selection" in {
      val result = ReasonableExcuse.options(ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`)),
        seqOfReasonableExcuses, hintTextMessageKey = "reasonableExcuses.otherReason.hintText", showHintText = true)

      result shouldBe seqOfRadioOptions
    }

    "return a Seq of RadioItems that will be passed to the view - pre-selection: bereavement" in {
      val result = ReasonableExcuse.options(ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`)).fill("bereavement"),
        seqOfReasonableExcuses, hintTextMessageKey = "reasonableExcuses.otherReason.hintText", showHintText = true)
      val expectedResult = seqOfRadioOptions.drop(1).+:(RadioItem(
        value = Some("bereavement"),
        content = Text(messages("reasonableExcuses.bereavementReason")),
        checked = true
      ))

      result shouldBe expectedResult
    }
  }

  "optionsWithDivider" should {
    "insert a divider before the 'Other' option" in {
      val result = ReasonableExcuse.optionsWithDivider(
        ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`)),
        "reasonableExcuses.breakerText",
        seqOfReasonableExcuses, showAgentHintText = false, showHintText = true)
      val divider = RadioItem(
        divider = Some(messages("reasonableExcuses.breakerText"))
      )

      result shouldBe seqOfRadioOptions.dropRight(1) ++ Seq(divider) ++ Seq(seqOfRadioOptions.last)
    }

    "insert a divider before the 'Other' option - use agent wording when 'isAgentHintText is true" in {
      val result = ReasonableExcuse.optionsWithDivider(
        ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`)),
        "reasonableExcuses.breakerText",
        seqOfReasonableExcuses, showAgentHintText = true, showHintText = true)
      val divider = RadioItem(
        divider = Some(messages("reasonableExcuses.breakerText"))
      )

      result shouldBe seqOfRadioOptions.dropRight(1) ++ Seq(divider) ++ Seq(seqOfRadioOptions.last.copy(
        hint = Some(Hint(content = Text(messages("agent.reasonableExcuses.otherReason.hintText"))))
      ))
    }

    "insert a divider before the 'Other' option - now show hint text when feature switch disabled" in {
      val result = ReasonableExcuse.optionsWithDivider(
        ReasonableExcuseForm.reasonableExcuseForm(seqOfReasonableExcuses.map(_.`type`)),
        "reasonableExcuses.breakerText",
        seqOfReasonableExcuses, showAgentHintText = true, showHintText = false)
      val divider = RadioItem(
        divider = Some(messages("reasonableExcuses.breakerText"))
      )

      result shouldBe seqOfRadioOptions.dropRight(1) ++ Seq(divider) ++ Seq(seqOfRadioOptions.last.copy(
        hint = None
      ))
    }
  }
}
