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

package models.upload

import play.api.libs.json.{Format, JsResult, JsValue, Json}
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, Sensitive}

case class SensitiveUploadJourney(override val decryptedValue: UploadJourney) extends Sensitive[UploadJourney]

object SensitiveUploadJourney {

  private def encryptFields(uploadJourney: UploadJourney)(implicit crypto: Encrypter): UploadJourney = {
    uploadJourney.copy(
      uploadDetails = uploadJourney.uploadDetails.map(uploadDetails => uploadDetails.copy(fileName = crypto.encrypt(PlainText(uploadDetails.fileName)).value)),
      downloadUrl = uploadJourney.downloadUrl.map(url => crypto.encrypt(PlainText(url)).value),
      uploadFields = uploadJourney.uploadFields.map(_.map(
        contents => {
          (crypto.encrypt(PlainText(contents._1)).value, crypto.encrypt(PlainText(contents._2)).value)
        }
      ))
    )
  }

  private def decryptFields(securedUploadJourney: UploadJourney)(implicit crypto: Decrypter): UploadJourney =
    securedUploadJourney.copy(
      uploadDetails = securedUploadJourney.uploadDetails.map(uploadDetails => uploadDetails.copy(fileName = crypto.decrypt(Crypted(uploadDetails.fileName)).value)),
      downloadUrl = securedUploadJourney.downloadUrl.map(url => crypto.decrypt(Crypted(url)).value),
      uploadFields = securedUploadJourney.uploadFields.map(_.map(
        contents => {
          (crypto.decrypt(Crypted(contents._1)).value, crypto.decrypt(Crypted(contents._2)).value)
        }
      ))
    )

  implicit def format(implicit crypto: Encrypter with Decrypter): Format[SensitiveUploadJourney] =
    new Format[SensitiveUploadJourney] {
      def reads(json: JsValue): JsResult[SensitiveUploadJourney] =
        Json.fromJson[UploadJourney](json).map(o => SensitiveUploadJourney(decryptFields(o)))
      def writes(o: SensitiveUploadJourney): JsValue =
        Json.toJson(encryptFields(o.decryptedValue))
    }
}