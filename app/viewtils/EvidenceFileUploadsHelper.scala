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

package viewtils

import models.{Mode, UserRequest}
import models.upload.UploadJourney
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.upload.uploadList

import javax.inject.Inject

class EvidenceFileUploadsHelper @Inject()(uploadList: uploadList){

  def displayContentForFileUploads(uploadedFiles: Seq[(UploadJourney, Int)], mode: Mode)(implicit messages: Messages, request: UserRequest[_]): Seq[Html] = {
    uploadedFiles.map(uploadWithIndex =>
      uploadList(uploadWithIndex._1.reference, uploadWithIndex._1.uploadDetails.get.fileName, uploadWithIndex._2 + 1, mode)
    )
  }
}
