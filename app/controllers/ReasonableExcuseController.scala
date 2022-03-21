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
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.ReasonableExcuseForm
import helpers.FormProviderHelper
import models.pages.{PageMode, ReasonableExcuseSelectionPage}
import models.{Mode, NormalMode, ReasonableExcuse}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.AppealService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.ReasonableExcuseSelectionPage

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReasonableExcuseController @Inject()(
                                            reasonableExcuseSelectionPage: ReasonableExcuseSelectionPage,
                                            appealService: AppealService,
                                            errorHandler: ErrorHandler)
                                          (implicit mcc: MessagesControllerComponents,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(ReasonableExcuseSelectionPage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      attemptToRetrieveReasonableExcuseList().map(_.fold(
        identity,
        reasonableExcuses => {
          val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
            ReasonableExcuseForm.reasonableExcuseForm(reasonableExcuses.map(_.`type`)),
            SessionKeys.reasonableExcuse
          )
          Ok(reasonableExcuseSelectionPage(formProvider,
            ReasonableExcuse.optionsWithDivider(formProvider, "reasonableExcuses.breakerText", reasonableExcuses), pageMode(NormalMode)))
        }
      ))
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      attemptToRetrieveReasonableExcuseList().map(_.fold(
        identity,
        reasonableExcuses => {
          val formProvider: Form[String] = ReasonableExcuseForm.reasonableExcuseForm(reasonableExcuses.map(_.`type`))
          formProvider.bindFromRequest.fold(
            formWithErrors => {
              logger.debug(s"[ReasonableExcuseController][onSubmit] form errors ${formWithErrors.errors.head.message}")
              BadRequest(reasonableExcuseSelectionPage(formWithErrors,
                ReasonableExcuse.optionsWithDivider(formProvider, "reasonableExcuses.breakerText", reasonableExcuses), pageMode(NormalMode)))
            },
            selection => {
              logger.debug(s"[ReasonableExcuseController][onSubmit] User selected $selection option - adding '$selection' to session.")
              Redirect(controllers.routes.HonestyDeclarationController.onPageLoad())
                .addingToSession(SessionKeys.reasonableExcuse -> selection)
            }
          )
        }
      ))
    }
  }

  private def attemptToRetrieveReasonableExcuseList()(implicit hc: HeaderCarrier,
                                                      request: Request[_]): Future[Either[Result, Seq[ReasonableExcuse]]] = {
    appealService.getReasonableExcuseListAndParse().map {
      _.fold[Either[Result, Seq[ReasonableExcuse]]]({
        logger.error("[ReasonableExcuseController][onPageLoad] - Received a None response from the appeal service when " +
          "trying to retrieve reasonable excuses - rendering ISE")
        Left(errorHandler.showInternalServerError)
      })(Right(_))
    }
  }
}
