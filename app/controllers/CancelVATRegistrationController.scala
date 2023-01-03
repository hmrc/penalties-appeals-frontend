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
import forms.CancelVATRegistrationForm
import helpers.FormProviderHelper
import models.pages.{CancelVATRegistrationPage, PageMode}
import models.{Mode, NormalMode}
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.CancelVATRegistrationPage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancelVATRegistrationController @Inject()(
                                                 cancelVATRegistrationPage: CancelVATRegistrationPage,
                                                 navigation: Navigation,
                                                 sessionService: SessionService
                                               )(implicit
                                                 authorise: AuthPredicate,
                                                 dataRequired: DataRequiredAction,
                                                 dataRetrieval: DataRetrievalAction,
                                                 appConfig: AppConfig,
                                                 mcc: MessagesControllerComponents,
                                                 executionContext: ExecutionContext
                                               ) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(CancelVATRegistrationPage, mode)

  def onPageLoadForCancelVATRegistration(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[String] =
        FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
          CancelVATRegistrationForm.cancelVATRegistrationForm,
          SessionKeys.cancelVATRegistration,
          userRequest.answers
        )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.CancelVATRegistrationController.onSubmitForCancelVATRegistration()
      Ok(cancelVATRegistrationPage(formProvider, radioOptionsToRender, postAction, pageMode(NormalMode)))
    }
  }

  def onSubmitForCancelVATRegistration(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async { implicit userRequest => {
    CancelVATRegistrationForm.cancelVATRegistrationForm
      .bindFromRequest()
      .fold(
        form => {
          val postAction =
            controllers.routes.CancelVATRegistrationController.onSubmitForCancelVATRegistration()
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(form)
          Future(BadRequest(cancelVATRegistrationPage(form, radioOptionsToRender, postAction, pageMode(NormalMode))))
        },
        cancelVATRegistration => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.cancelVATRegistration, cancelVATRegistration)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(CancelVATRegistrationPage, NormalMode, Some(cancelVATRegistration)))
          }
        }
      )
  }
  }
}
