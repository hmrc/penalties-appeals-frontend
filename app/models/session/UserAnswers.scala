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

package models.session

import play.api.libs.json._

case class UserAnswers(
                        journeyId: String,
                        data: JsObject = Json.obj()
                      ) {

  def getAnswer[A](key: String)(implicit reads: Reads[A]): Option[A] = {
    (data \ key).validate.fold(_ => None, Some(_))
  }

  def setAnswer[A](key: String, value: A)(implicit writes: Writes[A]): UserAnswers = {
    UserAnswers(journeyId, data ++ Json.obj(key -> value))
  }

}

object UserAnswers {
  implicit val format: Format[UserAnswers] = Json.format[UserAnswers]
}