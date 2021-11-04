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

package config.featureSwitches

import base.SpecBase

class FeatureSwitchingSpec extends SpecBase {
  class Setup {
    val featureSwitching: FeatureSwitching = new FeatureSwitching {}
    sys.props -= JSRouteCheckingPrevention.name
  }

  "listOfAllFeatureSwitches" should {
    "be all the featureswitches in the app" in {
      FeatureSwitch.listOfAllFeatureSwitches shouldBe List(JSRouteCheckingPrevention)
    }
  }

  "constants" should {
    "be true and false" in new Setup {
      featureSwitching.FEATURE_SWITCH_ON shouldBe "true"
      featureSwitching.FEATURE_SWITCH_OFF shouldBe "false"
    }
  }

  "isEnabled" should {
    s"return true if feature switch is enabled" in new Setup {
      featureSwitching.enableFeatureSwitch(JSRouteCheckingPrevention)
      featureSwitching.isEnabled(JSRouteCheckingPrevention) shouldBe true
    }
    s"return false if feature switch is disabled" in new Setup {
      featureSwitching.disableFeatureSwitch(JSRouteCheckingPrevention)
      featureSwitching.isEnabled(JSRouteCheckingPrevention) shouldBe false
    }
    "return false if feature switch does not exist" in new Setup {
      featureSwitching.isEnabled(JSRouteCheckingPrevention) shouldBe false
    }
  }

  "enableFeatureSwitch" should {
    s"set ${JSRouteCheckingPrevention.name} property to true" in new Setup {
      featureSwitching.enableFeatureSwitch(JSRouteCheckingPrevention)
      (sys.props get JSRouteCheckingPrevention.name get) shouldBe "true"
    }
  }

  "disableFeatureSwitch" should {
    s"set ${JSRouteCheckingPrevention.name} property to false" in new Setup {
      featureSwitching.disableFeatureSwitch(JSRouteCheckingPrevention)
      (sys.props get JSRouteCheckingPrevention.name get) shouldBe "false"
    }
  }
}
