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
import forms.WhenDidFireOrFloodHappenForm
import helpers.FormProviderHelper
import models.Mode
import models.pages.{Page, PageMode, WhenDidFireOrFloodHappenPage}
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.fireOrFlood.WhenDidFireOrFloodHappenPage

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FireOrFloodReasonController @Inject()(fireOrFloodPage: WhenDidFireOrFloodHappenPage,
                                            navigation: Navigation,
                                            sessionService: SessionService)
                                           (implicit authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction,
                                            dataRetrieval: DataRetrievalAction,
                                            executionContext: ExecutionContext,
                                            appConfig: AppConfig,
                                            mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) { implicit request =>
    val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
      WhenDidFireOrFloodHappenForm.whenFireOrFloodHappenedForm(),
      SessionKeys.dateOfFireOrFlood,
      request.answers
    )
    val postAction = controllers.routes.FireOrFloodReasonController.onSubmit(mode)
    Ok(fireOrFloodPage(formProvider, postAction, pageMode(WhenDidFireOrFloodHappenPage, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async { implicit request =>
    val postAction = controllers.routes.FireOrFloodReasonController.onSubmit(mode)
    WhenDidFireOrFloodHappenForm.whenFireOrFloodHappenedForm().bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(fireOrFloodPage(formWithErrors, postAction, pageMode(WhenDidFireOrFloodHappenPage, mode))))
      },
      dateOfFireOrFlood => {
        logger.debug(s"[FireOrFloodController][onSubmit] - Adding '$dateOfFireOrFlood' to session under key: ${SessionKeys.dateOfFireOrFlood}")
        val updatedAnswers = request.answers.setAnswer[LocalDate](SessionKeys.dateOfFireOrFlood, dateOfFireOrFlood)
        sessionService.updateAnswers(updatedAnswers).map {
          _ => Redirect(navigation.nextPage(WhenDidFireOrFloodHappenPage, mode))
        }
      }
    )
  }
}
