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

package base

class BaseSelectors {
  val title = "title"

  val h1 = "#main-content h1"
  val h2 = "#main-content h2"

  val pElementIndex: Int => String = (index: Int) => s"#main-content p.govuk-body:nth-child($index)"

  val externalGuidanceLink = "#external-guidance-link"

  val labelForRadioButton: Int => String = (index: Int) => if(index == 1) "label[for=value]" else s"label[for=value-$index]"

  val breakerElement = ".govuk-radios__divider"

  val listIndexWithElementIndex: (Int, Int) => String = (ulIndex: Int, liIndex: Int) => s"#main-content ul:nth-child($ulIndex) li:nth-child($liIndex)"

  val button = "#main-content .govuk-button"

  val hintText = "#main-content .govuk-hint"

  val dateEntry: Int => String = (index: Int) => s"#date > div:nth-child($index) > div > label"

  val insetText = "#main-content > div > div > div.govuk-inset-text"

}
