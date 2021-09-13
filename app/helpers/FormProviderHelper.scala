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

package helpers

import models.appeals.HospitalStayEndInput
import play.api.data.Form
import play.api.mvc.Request

import java.time.LocalDate

object FormProviderHelper {
  def getSessionKeyAndAttemptToFillAnswerAsString(formProvider: Form[String], sessionKeyToQuery: String)(implicit request: Request[_]): Form[String] = {
    if (request.session.get(sessionKeyToQuery).isDefined) {
      formProvider.fill(request.session.get(sessionKeyToQuery).get)
    } else {
      formProvider
    }
  }

  def getSessionKeyAndAttemptToFillAnswerAsOptionString(formProvider: Form[Option[String]], sessionKeyToQuery: String)
                                                       (implicit request: Request[_]): Form[Option[String]] = {
    if (request.session.get(sessionKeyToQuery).isDefined) {
      formProvider.fill(Some(request.session.get(sessionKeyToQuery).get))
    } else {
      formProvider
    }
  }

  def getSessionKeyAndAttemptToFillAnswerAsDate(formProvider: Form[LocalDate], sessionKeyToQuery: String)(implicit request: Request[_]): Form[LocalDate] = {
    if (request.session.get(sessionKeyToQuery).isDefined) {
      val parsedDate: LocalDate = LocalDate.parse(request.session.get(sessionKeyToQuery).get)
      formProvider.fill(parsedDate)
    } else {
      formProvider
    }
  }

  def getSessionKeysAndAttemptToFillConditionalForm(formProvider: Form[HospitalStayEndInput], sessionKvs: (String, String)
                                                   )(implicit request: Request[_]): Form[HospitalStayEndInput] = {
    (request.session.get(sessionKvs._1), request.session.get(sessionKvs._2)) match {
      case (Some(key1), Some(key2)) => {
        val parsedDate: LocalDate = LocalDate.parse(key2)
        val formInput = HospitalStayEndInput(key1, Some(parsedDate))
        formProvider.fill(formInput)
      }
      case (Some(key1), None) => {
        val formInput = HospitalStayEndInput(key1, None)
        formProvider.fill(formInput)
      }
      case (_, _) => formProvider
    }
  }
}
