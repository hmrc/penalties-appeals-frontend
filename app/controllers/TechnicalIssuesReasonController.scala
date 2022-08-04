/*
 * Copyright 2022 HM Revenue & Customs
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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.{WhenDidTechnologyIssuesBeginForm, WhenDidTechnologyIssuesEndForm}
import helpers.FormProviderHelper
import models.Mode
import models.pages.{Page, PageMode, WhenDidTechnologyIssuesBeginPage, WhenDidTechnologyIssuesEndPage}
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.technicalIssues.TechnologyIssuesDatePage

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TechnicalIssuesReasonController @Inject()(technicalIssuesDatePage: TechnologyIssuesDatePage,
                                                navigation: Navigation,
                                                errorHandler: ErrorHandler,
                                                sessionService: SessionService)
                                               (implicit authorise: AuthPredicate,
                                                dataRequired: DataRequiredAction,
                                                dataRetrieval: DataRetrievalAction,
                                                appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm,
        SessionKeys.whenDidTechnologyIssuesBegin,
        userRequest.answers
      )
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      Ok(technicalIssuesDatePage(formProvider, postAction, "technicalIssues.begin", pageMode(WhenDidTechnologyIssuesBeginPage, mode)))
    }
  }

  def onSubmitForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm().bindFromRequest().fold(
        formWithErrors => Future(BadRequest(technicalIssuesDatePage(formWithErrors, postAction, "technicalIssues.begin", pageMode(WhenDidTechnologyIssuesBeginPage, mode)))),
        dateTechnicalIssuesBegan => {
          val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesBegin, dateTechnicalIssuesBegan)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhenDidTechnologyIssuesBeginPage, mode)))
        }
      )
    }
  }

  def onPageLoadForWhenTechnologyIssuesEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val techIssuesBegin = userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesBegin)
      techIssuesBegin.fold(errorHandler.showInternalServerError)(
        startDate => {
          val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
            WhenDidTechnologyIssuesEndForm.whenDidTechnologyIssuesEndForm(startDate),
            SessionKeys.whenDidTechnologyIssuesEnd,
            userRequest.answers
          )
          val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesEnded(mode)
          Ok(technicalIssuesDatePage(formProvider, postAction, "technicalIssues.end", pageMode(WhenDidTechnologyIssuesEndPage, mode)))
        }
      )
    }
  }

  def onSubmitForWhenTechnologyIssuesEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      userRequest.answers.getAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesBegin).fold(Future(errorHandler.showInternalServerError))(
        startDate => {
          val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesEnded(mode)
          WhenDidTechnologyIssuesEndForm.whenDidTechnologyIssuesEndForm(startDate).bindFromRequest().fold(
            formWithErrors => Future(BadRequest(technicalIssuesDatePage(formWithErrors, postAction, "technicalIssues.end", pageMode(WhenDidTechnologyIssuesEndPage, mode)))),
            dateTechnicalIssuesBegan => {
              val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenDidTechnologyIssuesEnd, dateTechnicalIssuesBegan)
              sessionService.updateAnswers(updatedAnswers).map {
                _ => Redirect(navigation.nextPage(WhenDidTechnologyIssuesEndPage, mode))
              }
            }
          )
        }
      )
    }
  }
}
