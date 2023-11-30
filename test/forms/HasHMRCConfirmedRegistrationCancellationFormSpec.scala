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

package forms

import base.SpecBase
import models.UserRequest
import org.mockito.Mockito.mock
import play.api.Configuration
import play.api.data.FormError
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.SessionKeys

class HasHMRCConfirmedRegistrationCancellationFormSpec extends FormBehaviours with SpecBase {

  "HasHMRCConfirmedRegistrationCancellationForm" must {
    val vatTraderForm = HasHMRCConfirmedRegistrationCancellationForm.hasHMRCConfirmedRegistrationCancellationForm
    behave like mandatoryField(vatTraderForm, "value", FormError("value", "hasHMRCConfirmedRegistration.error.required"))

    "the value entered does not exist in the possible, valid values" in {
      val result = vatTraderForm.bind(Map("value" -> "random-value")).apply("value")
      result.errors.headOption shouldBe Some(FormError("value", "hasHMRCConfirmedRegistration.error.required"))
    }
  }

}
