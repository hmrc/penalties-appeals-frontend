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

import config.featureSwitches.{FeatureSwitch, FeatureSwitching}
import play.api.Configuration
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class FeatureSwitchController @Inject()(implicit mcc: MessagesControllerComponents,
                                        val config: Configuration) extends FrontendController(mcc) with FeatureSwitching {
  def enableOrDisableFeature(name: String, enable: Boolean): Action[AnyContent] = Action {
    val matchedFeatureSwitch: Option[FeatureSwitch] = FeatureSwitch.listOfAllFeatureSwitches.find(_.name == name)
    matchedFeatureSwitch.fold[Result](NotFound)(
      featureSwitch => {
        if (enable) {
          enableFeatureSwitch(featureSwitch)
        } else {
          disableFeatureSwitch(featureSwitch)
        }
        Ok(s"$featureSwitch set to $enable")
      })
  }
}
