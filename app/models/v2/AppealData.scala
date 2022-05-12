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

package models.v2

import models.PenaltyTypeEnum
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

//TODO: quick workaround for type difference, remove when old API is removed
trait AppealInformation[A] {
  val `type`: PenaltyTypeEnum.Value
  val startDate: A
  val endDate: A
  val dueDate: A
  val dateCommunicationSent: A
}
case class AppealData[A](
                       `type`: PenaltyTypeEnum.Value,
                       startDate: A,
                       endDate: A,
                       dueDate: A,
                       dateCommunicationSent: A
                     ) extends AppealInformation[A]

object AppealData {
  implicit val format: Format[AppealData[LocalDate]] = Json.format[AppealData[LocalDate]]
}
