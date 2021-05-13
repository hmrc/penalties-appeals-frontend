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

package models

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class ReasonableExcuseSpec extends WordSpec with Matchers {
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

    s"parse and omit any values not needed in the model" in {
      val jsonRepresentingInvalidReasonableExcuse: JsValue = Json.parse(
        """
          |{
          |   "type": "this_is_a_great_type",
          |   "descriptionKey": "message_in_a_bottle",
          |   "isEnabled": true
          |}
          |""".stripMargin
      )
      val result = Json.fromJson[ReasonableExcuse](jsonRepresentingInvalidReasonableExcuse)(ReasonableExcuse.singularReads)
      result.isSuccess shouldBe true
      result.get shouldBe ReasonableExcuse(
        `type` = "this_is_a_great_type",
        descriptionKey = "message_in_a_bottle",
        isOtherOption = false
      )
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
}
