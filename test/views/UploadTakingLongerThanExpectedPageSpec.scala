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

package views

import base.{BaseSelectors, SpecBase}
import messages.UploadTakingLongerThanExpectedMessages._
import models.{NormalMode, UserRequest}
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.noJs.UploadTakingLongerThanExpectedPage

class UploadTakingLongerThanExpectedPageSpec extends SpecBase with ViewBehaviours {
  "UploadTakingLongerThanExpectedPage" should {
    val uploadTakingLongerThanExpectedPage: UploadTakingLongerThanExpectedPage = injector.instanceOf[UploadTakingLongerThanExpectedPage]
    object Selectors extends BaseSelectors
    def applyView(request: UserRequest[_]): HtmlFormat.Appendable = {
      uploadTakingLongerThanExpectedPage.apply(controllers.routes.OtherReasonController.onSubmitForUploadTakingLongerThanExpected(NormalMode))(request, implicitly, implicitly)
    }

    implicit val doc: Document = asDocument(applyView(userRequestWithCorrectKeys))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.button -> button
    )

    behave like pageWithExpectedMessages(expectedContent)
  }
}
