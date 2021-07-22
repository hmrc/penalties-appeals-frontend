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

package forms

import base.SpecBase
import utils.SessionKeys
import play.api.data.FormError

class MakingALateAppealFormSpec extends SpecBase with FormBehaviours {
  var request = fakeRequest
  "MakingALateAppealForm" should {
    "bind for bereavement" when {
      request = fakeRequest
        .withSession(SessionKeys.reasonableExcuse -> "bereavement")
      val form = MakingALateAppealForm.makingALateAppealForm(request)
      behave like mandatoryField(form, "late-appeal-text",
        FormError("late-appeal-text", "makingALateAppeal.bereavementReason.error.required"))
      "a text value entered for bereavement" in {
        val result = form.bind(
          Map(
            "late-appeal-text" -> "Some reason."
          )
        )
        result.hasErrors shouldBe false
      }
    }
    "bind for crime" when {
      request = fakeRequest
        .withSession(SessionKeys.reasonableExcuse -> "crime")
      val form = MakingALateAppealForm.makingALateAppealForm(request)
      behave like mandatoryField(form, "late-appeal-text",
        FormError("late-appeal-text", "makingALateAppeal.error.required"))
      "a text value entered for crime" in {
        val result = form.bind(
          Map(
            "late-appeal-text" -> "Some reason."
          )
        )
        result.hasErrors shouldBe false
      }
    }
  }
}
