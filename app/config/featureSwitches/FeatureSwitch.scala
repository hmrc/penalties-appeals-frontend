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

package config.featureSwitches

sealed trait FeatureSwitch {
  val name: String
  val displayText: String
}

object FeatureSwitch {
  val prefix: String = "feature.switch"
  val listOfAllFeatureSwitches: List[FeatureSwitch] = List(NonJSRouting, ShowDigitalCommsMessage, WarnForDuplicateFiles, ShowFullAppealAgainstTheObligation)
}

case object NonJSRouting extends FeatureSwitch {
  override val name: String = s"${FeatureSwitch.prefix}.non-js-routing"
  override val displayText: String = "To enable routing to JS pages"
}

case object ShowDigitalCommsMessage extends FeatureSwitch {
  override val name: String = s"${FeatureSwitch.prefix}.show-digital-comms-message"
  override val displayText: String = "To enable routing to JS pages"
}

case object WarnForDuplicateFiles extends FeatureSwitch {
  override val name: String = s"${FeatureSwitch.prefix}.warn-for-duplicate-files"
  override val displayText: String = "To enable warnings for duplicate files"
}

case object ShowFullAppealAgainstTheObligation extends FeatureSwitch {
  override val name: String = s"${FeatureSwitch.prefix}.show-full-appeal-against-the-obligation-journey"
  override val displayText: String = "To show the full appeal against the obligation journey"
}
