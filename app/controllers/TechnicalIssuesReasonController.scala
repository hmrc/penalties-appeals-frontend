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

package controllers

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.{WhenDidTechnologyIssuesBeginForm, WhenDidTechnologyIssuesEndForm}
import helpers.FormProviderHelper
import models.Mode
import models.pages.{WhenDidTechnologyIssuesBeginPage, WhenDidTechnologyIssuesEndPage}
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.technicalIssues.TechnologyIssuesDatePage

import java.time.LocalDate
import javax.inject.Inject

class TechnicalIssuesReasonController @Inject()(technicalIssuesDatePage: TechnologyIssuesDatePage,
                                                navigation: Navigation,
                                                errorHandler: ErrorHandler)
                                               (implicit authorise: AuthPredicate,
                                                dataRequired: DataRequiredAction,
                                                appConfig: AppConfig,
                                                mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm,
        SessionKeys.whenDidTechnologyIssuesBegin
      )
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      Ok(technicalIssuesDatePage(formProvider, postAction, "technicalIssues.begin"))
    }
  }

  def onSubmitForWhenTechnologyIssuesBegan(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesBegan(mode)
      WhenDidTechnologyIssuesBeginForm.whenDidTechnologyIssuesBeginForm().bindFromRequest().fold(
        formWithErrors => BadRequest(technicalIssuesDatePage(formWithErrors, postAction, "technicalIssues.begin")),
        dateTechnicalIssuesBegan => {
          Redirect(navigation.nextPage(WhenDidTechnologyIssuesBeginPage, mode))
            .addingToSession(SessionKeys.whenDidTechnologyIssuesBegin -> dateTechnicalIssuesBegan.toString)
        }
      )
    }
  }

  def onPageLoadForWhenTechnologyIssuesEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      userRequest.session.get(SessionKeys.whenDidTechnologyIssuesBegin).fold(errorHandler.showInternalServerError)(
        startDate => {
          val parsedStartDate: LocalDate = LocalDate.parse(startDate)
          val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
            WhenDidTechnologyIssuesEndForm.whenDidTechnologyIssuesEndForm(parsedStartDate),
            SessionKeys.whenDidTechnologyIssuesEnd
          )
          val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesEnded(mode)
          Ok(technicalIssuesDatePage(formProvider, postAction, "technicalIssues.end"))
        }
      )
    }
  }

  def onSubmitForWhenTechnologyIssuesEnded(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      userRequest.session.get(SessionKeys.whenDidTechnologyIssuesBegin).fold(errorHandler.showInternalServerError)(
        startDate => {
          val parsedStartDate: LocalDate = LocalDate.parse(startDate)
          val postAction = controllers.routes.TechnicalIssuesReasonController.onSubmitForWhenTechnologyIssuesEnded(mode)
          WhenDidTechnologyIssuesEndForm.whenDidTechnologyIssuesEndForm(parsedStartDate).bindFromRequest().fold(
            formWithErrors => BadRequest(technicalIssuesDatePage(formWithErrors, postAction, "technicalIssues.end")),
            dateTechnicalIssuesBegan => {
              Redirect(navigation.nextPage(WhenDidTechnologyIssuesEndPage, mode))
                .addingToSession(SessionKeys.whenDidTechnologyIssuesEnd -> dateTechnicalIssuesBegan.toString)
            }
          )
        })
    }
  }
}
