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

import play.api.data.Form
import play.api.mvc.Request
import utils.SessionKeys
import java.time.LocalDate

import models.appeals.HospitalStayEndInput

object FormProviderHelper {
  def getSessionKeyAndAttemptToFillAnswerAsString(formProvider: Form[String], sessionKeyToQuery: String)(implicit request: Request[_]): Form[String] = {
    if (request.session.get(sessionKeyToQuery).isDefined) {
      formProvider.fill(request.session.get(sessionKeyToQuery).get)
    } else {
      formProvider
    }
  }

  def getSessionKeyAndAttemptToFillAnswerAsOptionString(formProvider: Form[Option[String]], sessionKeyToQuery: String)(implicit request: Request[_]): Form[Option[String]] = {
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

  def getSessionKeysAndAttemptToFillConditionalForm(formProvider: Form[HospitalStayEndInput], sessionKeysToQuery: (String, String)
                                                   )(implicit request: Request[_]): Form[HospitalStayEndInput] = {
    sessionKeysToQuery match {
      case (key1, key2) if request.session.get(key1).isDefined && request.session.get(key2).isDefined => {
        val parsedDate: LocalDate = LocalDate.parse(request.session.get(key2).get)
        val formInput = HospitalStayEndInput(request.session.get(key1).get, Some(parsedDate))
        formProvider.fill(formInput)
      }
      case (key1, key2) if request.session.get(key1).isDefined && !request.session.get(key2).isDefined => {
        val formInput = HospitalStayEndInput(request.session.get(key1).get, None)
        formProvider.fill(formInput)
      }
      case (_, _) => formProvider
    }
  }
}
