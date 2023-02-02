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

package controllers

import config.AppConfig
  import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.OtherRelevantInformationForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.{OtherRelevantInformationPage, PageMode}
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.obligation.OtherRelevantInformationPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealAgainstObligationController @Inject()(otherRelevantInformationPage: OtherRelevantInformationPage,
                                                  navigation: Navigation,
                                                  sessionService: SessionService)
                                                 (implicit authorise: AuthPredicate,
                                                  dataRequired: DataRequiredAction,
                                                  appConfig: AppConfig,
                                                  mcc: MessagesControllerComponents,
                                                  dataRetrieval: DataRetrievalAction,
                                                  executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(OtherRelevantInformationPage, mode)

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen  dataRetrieval andThen dataRequired) {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        OtherRelevantInformationForm.otherRelevantInformationForm(),
        SessionKeys.otherRelevantInformation,
        request.answers
      )
      val postAction = controllers.routes.AppealAgainstObligationController.onSubmit(mode)
      Ok(otherRelevantInformationPage(formProvider, postAction, pageMode(mode)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      OtherRelevantInformationForm.otherRelevantInformationForm().bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.AppealAgainstObligationController.onSubmit(mode)
          Future(BadRequest(otherRelevantInformationPage(formWithErrors, postAction, pageMode(mode))))
        },
        otherInformationForAppealAgainstObligation => {
          val updatedAnswers = request.answers.setAnswer[String](SessionKeys.otherRelevantInformation, otherInformationForAppealAgainstObligation)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(OtherRelevantInformationPage, mode))
          }
        }
      )
    }
  }
}
