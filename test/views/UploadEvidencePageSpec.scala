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

package views

import base.{BaseSelectors, SpecBase}
import forms.UploadEvidenceForm
import messages.UploadEvidenceMessages._
import models.{NormalMode, PenaltyTypeEnum}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import utils.SessionKeys
import views.behaviours.ViewBehaviours
import views.html.reasonableExcuseJourneys.other.UploadEvidencePage

class UploadEvidencePageSpec extends SpecBase with ViewBehaviours {

  "UploadEvidencePage" should {
    val uploadEvidencePage: UploadEvidencePage = injector.instanceOf[UploadEvidencePage]
    object Selectors extends BaseSelectors {
      val label = ".govuk-label"
    }
    val formProvider = UploadEvidenceForm.uploadEvidenceForm

    def applyView(form: Form[_], request: FakeRequest[_] = fakeRequest): HtmlFormat.Appendable = {
      uploadEvidencePage.apply(form, controllers.routes.OtherReasonController.onSubmitForUploadEvidence(NormalMode))(request, implicitly, implicitly)
    }

    implicit val doc: Document = asDocument(applyView(formProvider))

    val expectedContent = Seq(
      Selectors.title -> title,
      Selectors.h1 -> h1,
      Selectors.pElementIndex(2) -> p1,
      Selectors.pElementIndex(3) -> p2,
      Selectors.pElementIndex(4) -> p3,
      Selectors.label -> uploadLabel,
      Selectors.button -> continueButton
    )

    behave like pageWithExpectedMessages(expectedContent)

    "display the LPP variation when the appeal is for a LPP" must {
      implicit val doc: Document = asDocument(applyView(formProvider, fakeRequest.withSession(SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString)))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        Selectors.pElementIndex(3) -> p2Lpp,
        Selectors.pElementIndex(4) -> p3,
        Selectors.label -> uploadLabel,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "display appeal against the obligation page when the appeal is for LSP" must {
      implicit val doc: Document = asDocument(applyView(formProvider, fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Submission.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        Selectors.pElementIndex(3) -> p2AppealAgainstObligation,
        Selectors.pElementIndex(4) -> p3,
        Selectors.label -> uploadLabel,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }

    "display appeal against the obligation page when the appeal is for LPP" must {
      implicit val doc: Document = asDocument(applyView(formProvider, fakeRequest.withSession(
        SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment.toString,
        SessionKeys.isObligationAppeal -> "true")))

      val expectedContent = Seq(
        Selectors.title -> title,
        Selectors.h1 -> h1,
        Selectors.pElementIndex(2) -> p1,
        //TODO: change to relevant text for LPP variation of obligation appeal
        Selectors.pElementIndex(3) -> "",
        Selectors.pElementIndex(4) -> p3,
        Selectors.label -> uploadLabel,
        Selectors.button -> continueButton
      )

      behave like pageWithExpectedMessages(expectedContent)
    }
  }
}