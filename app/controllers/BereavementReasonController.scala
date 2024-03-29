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
import forms.WhenDidThePersonDieForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.{PageMode, WhenDidThePersonDiePage}
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.bereavement.WhenDidThePersonDiePage

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BereavementReasonController @Inject()(whenDidThePersonDiePage: WhenDidThePersonDiePage,
                                            navigation: Navigation,
                                            sessionService: SessionService)
                                           (implicit authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction,
                                            dataRetrieval: DataRetrievalAction,
                                            ec: ExecutionContext,
                                            appConfig: AppConfig,
                                            mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(WhenDidThePersonDiePage, mode)

  def onPageLoadForWhenThePersonDied(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidThePersonDieForm.whenDidThePersonDieForm(),
        SessionKeys.whenDidThePersonDie,
        userRequest.answers
      )
      val postAction = controllers.routes.BereavementReasonController.onSubmitForWhenThePersonDied(mode)
      Ok(whenDidThePersonDiePage(formProvider, postAction, pageMode(mode)))
    }
  }

  def onSubmitForWhenThePersonDied(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val postAction = controllers.routes.BereavementReasonController.onSubmitForWhenThePersonDied(mode)
      WhenDidThePersonDieForm.whenDidThePersonDieForm().bindFromRequest().fold(
        formWithErrors => Future(BadRequest(whenDidThePersonDiePage(formWithErrors, postAction, pageMode(mode)))),
        whenPersonDied => {
          logger.debug(s"[BereavementReasonController]" +
            s"[onSubmitForWhenThePersonDied] - Adding '$whenPersonDied' to session under key: ${SessionKeys.whenDidThePersonDie}")
          val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenDidThePersonDie, whenPersonDied)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhenDidThePersonDiePage, mode)))
        }
      )
    }
  }
}
