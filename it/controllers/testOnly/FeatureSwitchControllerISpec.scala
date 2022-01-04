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

package controllers.testOnly

import config.featureSwitches.NonJSRouting
import play.api.test.Helpers._
import utils.IntegrationSpecCommonBase

class FeatureSwitchControllerISpec extends IntegrationSpecCommonBase {
  class Setup {
    sys.props -= NonJSRouting.name
    sys.props get NonJSRouting.name shouldBe None
  }

  s"GET /penalties-appeals/test-only/feature-switch" should {
    s"return OK and set the JSRouting feature switch to be enabled" in new Setup {
      val result = await(buildClientForRequestToApp(uri = s"/test-only/feature-switch?name=${NonJSRouting.name}&enable=true").get())
      result.status shouldBe OK
      result.body shouldBe s"$NonJSRouting set to true"
      (sys.props get NonJSRouting.name get) shouldBe "true"
    }

    s"return OK and set the JSRouting feature switch to be disabled" in new Setup {
      val result = await(buildClientForRequestToApp(uri = s"/test-only/feature-switch?name=${NonJSRouting.name}&enable=false").get())
      result.status shouldBe OK
      result.body shouldBe s"$NonJSRouting set to false"
      (sys.props get NonJSRouting.name get) shouldBe "false"
    }

    s"return NOT_FOUND when the feature switch does not exist" in new Setup {
      val result = await(buildClientForRequestToApp(uri = "/test-only/feature-switch?name=fake&enable=true").get())
      result.status shouldBe NOT_FOUND
      sys.props get "fake" shouldBe None
    }
  }
}
