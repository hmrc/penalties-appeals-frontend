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

package forms

import base.SpecBase
import config.AppConfig
import org.mockito.Mockito.mock
import play.api.Configuration
import play.api.data.{Form, FormError}

class CancelVATRegistrationFormSpec extends FormBehaviours with SpecBase {
  implicit val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  override implicit val config: Configuration = mock(classOf[Configuration])
  val form: Form[String] = CancelVATRegistrationForm.cancelVATRegistrationForm
  behave like mandatoryField(form, "value", FormError("value", "cancelVATRegistration.error.required"))

  "the value entered does not exist in the possible, valid values" in {
    val result = form.bind(Map("value" -> "random-value")).apply("value")
    result.errors.headOption shouldBe Some(FormError("value", "cancelVATRegistration.error.required"))
  }
}

