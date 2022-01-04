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

package viewtils

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

trait ImplicitDateFormatter {

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val monthYearDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

  implicit def dateToString(date: LocalDate): String = dateFormatter.format(date)

  implicit def dateTimeToString(dateTime: LocalDateTime): String = dateFormatter.format(dateTime)

  implicit def dateTimeToMonthYearString(dateTime: LocalDateTime): String = monthYearDateFormatter.format(dateTime)

}

object ImplicitDateFormatter extends ImplicitDateFormatter
