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

import config.featureSwitches.{FeatureSwitching, ShowReasonableExcuseHintText}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.ReasonableExcuseForm
import helpers.FormProviderHelper
import models.PenaltyTypeEnum.Late_Submission
import models.pages.{PageMode, ReasonableExcuseSelectionPage}
import models._
import play.api.Configuration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{AppealService, SessionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.ReasonableExcuseSelectionPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReasonableExcuseController @Inject()(
                                            reasonableExcuseSelectionPage: ReasonableExcuseSelectionPage,
                                            appealService: AppealService,
                                            errorHandler: ErrorHandler,
                                            sessionService: SessionService)
                                          (implicit mcc: MessagesControllerComponents,
                                           implicit val config: Configuration,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRetrieval: DataRetrievalAction,
                                           dataRequired: DataRequiredAction,
                                           implicit val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(ReasonableExcuseSelectionPage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      attemptToRetrieveReasonableExcuseList().flatMap(_.fold(
        identity,
        reasonableExcuses => {
          val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
            ReasonableExcuseForm.reasonableExcuseForm(reasonableExcuses.map(_.`type`)),
            SessionKeys.reasonableExcuse,
            userRequest.answers
          )
          val showAgentHintText = showAgentHintTextWording()
          val showHintText = isEnabled(ShowReasonableExcuseHintText)
          Future(Ok(reasonableExcuseSelectionPage(formProvider,
            ReasonableExcuse.optionsWithDivider(formProvider, "reasonableExcuses.breakerText", reasonableExcuses, showAgentHintText, showHintText), pageMode(NormalMode), showAgentHintText, showHintText)))
        }
      ))
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      attemptToRetrieveReasonableExcuseList().flatMap(_.fold(
        identity,
        reasonableExcuses => {
          val showAgentHintText = showAgentHintTextWording()
          val formProvider: Form[String] = ReasonableExcuseForm.reasonableExcuseForm(reasonableExcuses.map(_.`type`))
          formProvider.bindFromRequest().fold(
            formWithErrors => {
              val showHintText = isEnabled(ShowReasonableExcuseHintText)
              logger.debug(s"[ReasonableExcuseController][onSubmit] form errors ${formWithErrors.errors.head.message}")
              Future(BadRequest(reasonableExcuseSelectionPage(formWithErrors,
                ReasonableExcuse.optionsWithDivider(formProvider, "reasonableExcuses.breakerText", reasonableExcuses, showAgentHintText, showHintText), pageMode(NormalMode), showAgentHintText, showHintText)))
            },
            selection => {
              logger.debug(s"[ReasonableExcuseController][onSubmit] User selected $selection option - adding '$selection' to session.")
              val updatedAnswers = userRequest.answers.setAnswer(SessionKeys.reasonableExcuse, selection)
              sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(controllers.routes.HonestyDeclarationController.onPageLoad()))
            }
          )
        }
      ))
    }
  }

  private def attemptToRetrieveReasonableExcuseList()(implicit hc: HeaderCarrier,
                                                      request: Request[_]): Future[Either[Future[Result], Seq[ReasonableExcuse]]] = {
    appealService.getReasonableExcuseListAndParse().map {
      _.fold[Either[Future[Result], Seq[ReasonableExcuse]]]({
        logger.error("[ReasonableExcuseController][onPageLoad] - Received a None response from the appeal service when " +
          "trying to retrieve reasonable excuses - rendering ISE")
        Left(Future(errorHandler.showInternalServerError()))
      })(Right(_))
    }
  }

  private def showAgentHintTextWording()(implicit userRequest: UserRequest[_]): Boolean = {
    val optAnswerForWhoPlannedToSubmitVATReturn = userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn)
    val optAnswerForWhatCausedYouToMissTheDeadline = userRequest.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline)
    val isLateSubmissionPenaltyAppeal = userRequest.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(Late_Submission)
    userRequest.isAgent && (
        optAnswerForWhoPlannedToSubmitVATReturn.contains("client")
          || optAnswerForWhatCausedYouToMissTheDeadline.contains("client")
          || !isLateSubmissionPenaltyAppeal
      )
  }
}
