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

package helpers

import base.SpecBase
import config.AppConfig
import config.featureSwitches.FeatureSwitching
import org.mockito.Mockito.mock

class DateTimeHelperSpec extends SpecBase with FeatureSwitching {
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  val dateTimeHelper: DateTimeHelper = injector.instanceOf[DateTimeHelper]

  "dateNow" should {
    "return getFeatureDate" in {
      dateTimeHelper.dateNow shouldBe getFeatureDate
    }
  }
}
