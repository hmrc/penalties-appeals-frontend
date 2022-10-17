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
import forms.upscan.RemoveFileQuestionForm
import models.{Mode, UserRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.upscan.UpscanService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.PagerDutyHelper
import utils.PagerDutyHelper.PagerDutyKeys._
import views.html.reasonableExcuseJourneys.other.RemoveFilePage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveFileController @Inject()(upscanService: UpscanService,
                                     errorHandler: ErrorHandler,
                                     removeFilePage: RemoveFilePage)(implicit mcc: MessagesControllerComponents,
                                                                     appConfig: AppConfig,
                                                                     authorise: AuthPredicate,
                                                                     dataRequired: DataRequiredAction,
                                                                     dataRetrieval: DataRetrievalAction,
                                                                     ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(fileReference: String, isJsEnabled: Boolean, mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      showPage(request.answers.journeyId, fileReference, RemoveFileQuestionForm.form, Ok, mode, isJsEnabled)
    }
  }

  def onSubmit(fileReference: String, isJsEnabled: Boolean, mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      RemoveFileQuestionForm.form.bindFromRequest.fold(
        formWithErrors => showPage(request.answers.journeyId, fileReference, formWithErrors, BadRequest, mode, isJsEnabled),
        answer => {
          if (answer == "yes") {
            upscanService.removeFileFromJourney(request.answers.journeyId, fileReference).map {
              _ => {
                routeToUploadPage(isJsEnabled, mode)
              }
            }.recover {
              case _ => {
                PagerDutyHelper.log("removeFileUpscan",FILE_REMOVAL_FAILURE_UPSCAN)
                logger.error("[RemoveFileController][onSubmit] - Could not remove file from user journey")
                errorHandler.showInternalServerError
              }
            }
          } else {
            Future(routeToUploadPage(isJsEnabled, mode))
          }
        }
      )
    }
  }


  private def showPage(journeyId: String,
                       fileReference: String,
                       form: Form[_],
                       statusOnSuccess: Status,
                       mode: Mode,
                       isJsEnabled: Boolean)(implicit userRequest: UserRequest[_]): Future[Result] = {
    val backUrl = {
      if (isJsEnabled) controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode)
      else controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode)
    }
    upscanService.getFileNameForJourney(journeyId, fileReference).map {
      _.fold(errorHandler.showInternalServerError)(nameOfFile => {
        statusOnSuccess(
          removeFilePage(
            form = form,
            radioOptions = RadioOptionHelper.yesNoRadioOptions(form),
            postAction = controllers.routes.RemoveFileController.onSubmit(fileReference, isJsEnabled, mode),
            fileName = nameOfFile,
            backLink = backUrl.url
          )
        )
      })
    }.recover {
      case _ => {
        PagerDutyHelper.log("RemoveFileController: onPageLoad", FILE_RETRIEVAL_FAILURE_UPSCAN)
        logger.error("[RemoveFileController][onPageLoad] - Failed to retrieve file name for file removal")
        errorHandler.showInternalServerError
      }
    }
  }

  private def routeToUploadPage(isJsEnabled: Boolean, mode: Mode): Result = {
    if (isJsEnabled) {
      Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode))
    } else {
      //Relying on upload list to handle further routing if no files are left
      Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode))
    }
  }
}
