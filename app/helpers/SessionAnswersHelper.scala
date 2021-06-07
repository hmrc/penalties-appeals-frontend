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

import play.api.i18n.Messages
import play.api.mvc.Request
import utils.SessionKeys
import viewtils.ImplicitDateFormatter

import java.time.LocalDate

object SessionAnswersHelper extends ImplicitDateFormatter {
  val answersRequiredForReasonableExcuseJourney: Map[String, Seq[String]] = Map(
    "crime" -> Seq(SessionKeys.hasCrimeBeenReportedToPolice, SessionKeys.reasonableExcuse, SessionKeys.dateOfCrime, SessionKeys.hasConfirmedDeclaration)
  )

  def isAllAnswerPresentForReasonableExcuse(reasonableExcuse: String)(implicit request: Request[_]): Boolean = {
    val answersRequired = answersRequiredForReasonableExcuseJourney(reasonableExcuse).toSet
    val keysInSession = request.session.data.keys.toSet
    answersRequired.subsetOf(keysInSession)
  }

  def getContentForReasonableExcuseCheckYourAnswersPage(reasonableExcuse: String)(implicit request: Request[_],
                                                                                  messages: Messages): Seq[(String, String, String)] = {
    reasonableExcuse match {
      case "crime" => Seq(
          (messages("checkYourAnswers.reasonableExcuse"), messages(s"checkYourAnswers.${request.session.get(SessionKeys.reasonableExcuse).get}.reasonableExcuse"), "#"),
          (messages("checkYourAnswers.crime.whenDidTheCrimeHappen"), dateToString(LocalDate.parse(request.session.get(SessionKeys.dateOfCrime).get)), "#"),
          (messages("checkYourAnswers.crime.hasCrimeBeenReported"), messages(s"checkYourAnswers.crime.${request.session.get(SessionKeys.hasCrimeBeenReportedToPolice).get}"), "#")
        )
      }
  }
}
