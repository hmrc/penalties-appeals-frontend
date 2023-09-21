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

import models.session.UserAnswers.SensitiveJsObject
import play.api.libs.json._
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, Sensitive}
import play.api.libs.functional.syntax._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class UserAnswers(
                        journeyId: String,
                        data: SensitiveJsObject = SensitiveJsObject(Json.obj()),
                        lastUpdated: Instant = Instant.now
                      ) {

  def getAnswer[A](key: String)(implicit reads: Reads[A]): Option[A] = {
    (data.decryptedValue \ key).validate.fold(_ => None, Some(_))
  }

  def setAnswer[A](key: String, value: A)(implicit writes: Writes[A]): UserAnswers = {
    UserAnswers(journeyId, SensitiveJsObject(data.decryptedValue ++ Json.obj(key -> value)))
  }

}

object UserAnswers {
  def reads(implicit crypto: Encrypter with Decrypter): Reads[UserAnswers] = {
    (__ \ "journeyId").read[String].and((__ \ "data").read[SensitiveJsObject]).and(
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat))(UserAnswers.apply _)
  }

  def writes(implicit crypto: Encrypter with Decrypter): OWrites[UserAnswers] = {
    (__ \ "journeyId").write[String].and((__ \ "data").write[SensitiveJsObject]).and(
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat))(unlift(UserAnswers.unapply))
  }

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] = OFormat(reads, writes)

  case class SensitiveJsObject(override val decryptedValue: JsObject) extends Sensitive[JsObject]

  implicit def sensitiveJsObjectFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveJsObject] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveJsObject.apply)

}