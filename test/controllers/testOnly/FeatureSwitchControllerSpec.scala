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

import base.SpecBase
import config.featureSwitches.{FeatureSwitch, FeatureSwitching, NonJSRouting}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.Configuration
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import java.time.LocalDate

import play.api.mvc.Result

import scala.concurrent.Future
import scala.language.postfixOps

class FeatureSwitchControllerSpec extends SpecBase with FeatureSwitching {
  val controller: FeatureSwitchController = injector.instanceOf[FeatureSwitchController]
  val mockConfig: Configuration = mock(classOf[Configuration])

  class Setup {
    reset(mockConfig)
    val controller = new FeatureSwitchController()(mcc, mockConfig)
    FeatureSwitch.listOfAllFeatureSwitches.foreach(sys.props -= _.name)
  }

  "enableOrDisableFeature" should {
    "return NOT FOUND when the feature switch is not defined" in {
      val result = controller.enableOrDisableFeature("fake", enable = true)(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return OK and enable the feature switch when specified" in {
      val result = controller.enableOrDisableFeature("feature.switch.non-js-routing", enable = true)(FakeRequest())
      status(result) shouldBe OK
      isEnabled(NonJSRouting) shouldBe true
    }

    "return OK and disable the feature switch when specified" in {
      val result = controller.enableOrDisableFeature("feature.switch.non-js-routing", enable = false)(FakeRequest())
      status(result) shouldBe OK
      isEnabled(NonJSRouting) shouldBe false
    }
  }
  "setTimeMachineDate" should {

    s"return NOT_FOUND (${Status.NOT_FOUND}) when the date provided is invalid" in new Setup {
      val result: Future[Result] = controller.setTimeMachineDate(Some("this-is-invalid"))(FakeRequest())
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "The date provided is in an invalid format"
    }

    s"return OK (${Status.OK}) and reset the date back to today's date if no date provided" in new Setup {
      when(mockConfig.getOptional[String](any())(any()))
        .thenReturn(None)
      val result: Future[Result] = controller.setTimeMachineDate(None)(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.now().toString}"
      controller.getFeatureDate shouldBe LocalDate.now()
    }

    s"return OK (${Status.OK}) and set the correct date provided" in new Setup {
      val result: Future[Result] = controller.setTimeMachineDate(Some("2022-01-01"))(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe s"Time machine set to: ${LocalDate.of(2022, 1, 1).toString}"
      (sys.props get "TIME_MACHINE_NOW" get) shouldBe LocalDate.of(2022, 1, 1).toString
    }
  }
}
