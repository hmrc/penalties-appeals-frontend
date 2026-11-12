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

package controllers.testOnly

import config.featureSwitches.FeatureSwitch
import play.api.test.Helpers._
import utils.IntegrationSpecCommonBase

import scala.language.postfixOps

class FeatureSwitchControllerISpec extends IntegrationSpecCommonBase {
  class Setup {
    FeatureSwitch.listOfAllFeatureSwitches.foreach(
      featureSwitch => {
        sys.props -= featureSwitch.name
        sys.props get featureSwitch.name shouldBe None
      }
    )
  }

  s"GET /penalties-appeals/test-only/feature-switch" should {
    FeatureSwitch.listOfAllFeatureSwitches.foreach(
      featureSwitch => {
        s"return OK and set the ${featureSwitch.name} feature switch to be enabled" in new Setup {
          val result = await(buildClientForRequestToApp(uri = s"/test-only/feature-switch?name=${featureSwitch.name}&enable=true").get())
          result.status shouldBe OK
          result.body shouldBe s"$featureSwitch set to true"
          (sys.props get featureSwitch.name get) shouldBe "true"
        }

        s"return OK and set the ${featureSwitch.name} feature switch to be disabled" in new Setup {
          val result = await(buildClientForRequestToApp(uri = s"/test-only/feature-switch?name=${featureSwitch.name}&enable=false").get())
          result.status shouldBe OK
          result.body shouldBe s"$featureSwitch set to false"
          (sys.props get featureSwitch.name get) shouldBe "false"
        }
      }
    )

    s"return NOT_FOUND when the feature switch does not exist" in new Setup {
      val result = await(buildClientForRequestToApp(uri = "/test-only/feature-switch?name=fake&enable=true").get())
      result.status shouldBe NOT_FOUND
      sys.props get "fake" shouldBe None
    }
  }
}
