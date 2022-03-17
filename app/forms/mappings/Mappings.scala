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

package forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of
import play.api.i18n.Messages

import java.time.LocalDate

trait Mappings extends Formatters {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def localDate(
                           invalidKey: String,
                           allRequiredKey: String,
                           twoRequiredKey: String,
                           requiredKey: String,
                           dayRequiredKey: Option[String] = None,
                           monthRequiredKey: Option[String] = None,
                           yearRequiredKey:Option[String] = None,
                           futureKey: Option[String] = None,
                           dateNotEqualOrAfterKeyAndCompareDate: Option[(String, LocalDate)] = None,
                           args: Seq[String] = Seq.empty)(implicit messages: Messages): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey,  requiredKey, dayRequiredKey, monthRequiredKey, yearRequiredKey, futureKey, dateNotEqualOrAfterKeyAndCompareDate, args))
}
