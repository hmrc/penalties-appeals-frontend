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

package config.featureSwitches

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.Configuration

class FeatureSwitchingSpec extends SpecBase {
  val mockConfig: Configuration = mock(classOf[Configuration])

  class Setup {
    reset(mockConfig)
    val featureSwitching: FeatureSwitching = new FeatureSwitching {
      override implicit val config: Configuration = mockConfig
    }
    sys.props -= NonJSRouting.name
    sys.props -= UseNewAPIModel.name
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    sys.props -= NonJSRouting.name
    sys.props -= UseNewAPIModel.name
  }

  "listOfAllFeatureSwitches" should {
    "be all the featureswitches in the app" in {
      FeatureSwitch.listOfAllFeatureSwitches shouldBe List(NonJSRouting, UseNewAPIModel)
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
      featureSwitching.enableFeatureSwitch(NonJSRouting)
      featureSwitching.isEnabled(NonJSRouting) shouldBe true
    }

    s"return false if feature switch is disabled" in new Setup {
      featureSwitching.disableFeatureSwitch(NonJSRouting)
      featureSwitching.isEnabled(NonJSRouting) shouldBe false
    }

    "return true if system props is empty but config has value" in new Setup {
      when(mockConfig.get[Boolean](any())(any()))
        .thenReturn(true)
      featureSwitching.isEnabled(NonJSRouting) shouldBe true
    }
  }

  "enableFeatureSwitch" should {
    s"set ${NonJSRouting.name} property to true" in new Setup {
      featureSwitching.enableFeatureSwitch(NonJSRouting)
      (sys.props get NonJSRouting.name get) shouldBe "true"
    }

    s"set ${UseNewAPIModel.name} property to true" in new Setup {
      featureSwitching.enableFeatureSwitch(UseNewAPIModel)
      (sys.props get UseNewAPIModel.name get) shouldBe "true"
    }
  }

  "disableFeatureSwitch" should {
    s"set ${NonJSRouting.name} property to false" in new Setup {
      featureSwitching.disableFeatureSwitch(NonJSRouting)
      (sys.props get NonJSRouting.name get) shouldBe "false"
    }

    s"set ${UseNewAPIModel.name} property to false" in new Setup {
      featureSwitching.disableFeatureSwitch(UseNewAPIModel)
      (sys.props get UseNewAPIModel.name get) shouldBe "false"
    }
  }
}
