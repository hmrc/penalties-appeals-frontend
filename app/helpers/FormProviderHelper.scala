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

package helpers

import models.appeals.HospitalStayEndInput
import models.session.UserAnswers
import play.api.data.Form

import java.time.LocalDate

object FormProviderHelper {
  def getSessionKeyAndAttemptToFillAnswerAsString(formProvider: Form[String], sessionKeyToQuery: String, userAnswers: UserAnswers): Form[String] = {
    userAnswers.getAnswer[String](sessionKeyToQuery) match {
      case Some(answer) => formProvider.fill(answer)
      case None => formProvider
    }
  }

  def getSessionKeyAndAttemptToFillAnswerAsDate(formProvider: Form[LocalDate], sessionKeyToQuery: String, userAnswers: UserAnswers): Form[LocalDate] = {
    userAnswers.getAnswer[LocalDate](sessionKeyToQuery) match {
      case Some(answer) => formProvider.fill(answer)
      case None => formProvider
    }
  }

  def getSessionKeysAndAttemptToFillConditionalForm(formProvider: Form[HospitalStayEndInput], sessionKvs: (String, String), userAnswers: UserAnswers): Form[HospitalStayEndInput] = {
    userAnswers.getAnswer[String](sessionKvs._1) match {
      case Some(key1) =>
        userAnswers.getAnswer[LocalDate](sessionKvs._2) match {
          case Some(answer) =>
            val formInput = HospitalStayEndInput(key1, Some(answer))
            formProvider.fill(formInput)
          case None =>
            val formInput = HospitalStayEndInput(key1, None)
            formProvider.fill(formInput)
        }
      case None => formProvider
    }
  }
}
