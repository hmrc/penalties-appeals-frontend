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
import forms.WhenDidBecomeUnableForm
import forms.WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm
import forms.upscan.UploadDocumentForm
import helpers.{FormProviderHelper, UpscanMessageHelper}
import javax.inject.Inject
import models.{Mode, NormalMode, UserRequest}
import models.pages.{EvidencePage, WhenDidBecomeUnablePage, WhyWasReturnSubmittedLatePage}
import navigation.Navigation
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.other._
import views.html.reasonableExcuseJourneys.other.noJs.UploadFirstDocumentPage

import java.time.LocalDate

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class OtherReasonController @Inject()(whenDidBecomeUnablePage: WhenDidBecomeUnablePage,
                                      whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage,
                                      uploadEvidencePage: UploadEvidencePage,
                                      uploadFirstDocumentPage: UploadFirstDocumentPage,
                                      navigation: Navigation,
                                      upscanService: UpscanService,
                                      uploadJourneyRepository: UploadJourneyRepository)
                                     (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      appConfig: AppConfig,
                                      errorHandler: ErrorHandler,
                                      mcc: MessagesControllerComponents,
                                      ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoadForWhenDidBecomeUnable(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidBecomeUnableForm.whenDidBecomeUnableForm(),
        SessionKeys.whenDidBecomeUnable
      )
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(mode)
      Ok(whenDidBecomeUnablePage(formProvider, postAction))
    }
  }

  def onSubmitForWhenDidBecomeUnable(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(mode)
      WhenDidBecomeUnableForm.whenDidBecomeUnableForm().bindFromRequest().fold(
        formWithErrors => {
          BadRequest(whenDidBecomeUnablePage(formWithErrors, postAction))
        },
        dateUnable => {
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable]" +
            s" - Adding '$dateUnable' to session under key: ${SessionKeys.whenDidBecomeUnable}")
          Redirect(navigation.nextPage(WhenDidBecomeUnablePage, mode, Some(dateUnable.toString)))
            .addingToSession((SessionKeys.whenDidBecomeUnable, dateUnable.toString))
        }
      )
    }
  }

  def onPageLoadForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(whyReturnSubmittedLateForm(), SessionKeys.whyReturnSubmittedLate)
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhyReturnSubmittedLate(mode)
      Ok(whyReturnSubmittedLatePage(formProvider, postAction))
    }
  }

  def onSubmitForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      whyReturnSubmittedLateForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.OtherReasonController.onSubmitForWhyReturnSubmittedLate(mode)
          BadRequest(whyReturnSubmittedLatePage(formWithErrors, postAction))
        },
        whyReturnSubmittedLateReason => {
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable]" +
            s" - Adding '$whyReturnSubmittedLateReason' to session under key: ${SessionKeys.whyReturnSubmittedLate}")
          Redirect(navigation.nextPage(WhyWasReturnSubmittedLatePage, mode, Some(whyReturnSubmittedLateReason)))
            .addingToSession(SessionKeys.whyReturnSubmittedLate -> whyReturnSubmittedLateReason)
        })
    }
  }

  def onPageLoadForUploadEvidence(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      if(request.cookies.get("jsenabled").isEmpty) {
        Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload()))
      } else {
        for {
          previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
        } yield {
          val previousUploads = previousUploadsState.fold("[]")(previousUploads => Json.stringify(Json.toJson(previousUploads)))
          val initiateNextUploadUrl = controllers.routes.UpscanController.initiateCallToUpscan(request.session.get(SessionKeys.journeyId).get)
          val getStatusUrl = controllers.routes.UpscanController.getStatusOfFileUpload(request.session.get(SessionKeys.journeyId).get, _)
          val removeFileUrl = controllers.routes.UpscanController.removeFile(request.session.get(SessionKeys.journeyId).get, _)
          val postAction = navigation.nextPage(EvidencePage, mode)
          Ok(uploadEvidencePage(postAction, initiateNextUploadUrl, getStatusUrl, removeFileUrl, previousUploads))
        }
      }
    }
  }

  def onPageLoadForFirstFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      if(request.cookies.get("jsenabled").isDefined) {
        Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode))
      } else {
        val nextPageIfNoUpload = navigation.nextPage(EvidencePage, mode)
        val formProvider = UploadDocumentForm.form
        upscanService.initiateSynchronousCallToUpscan(request.session.get(SessionKeys.journeyId).get, isAddingAnotherDocument = false).map(
          _.fold(
            error => {
              logger.error("[OtherReasonController][onPageLoadForFirstFileUpload] - Received error back from initiate request rendering ISE.")
              errorHandler.showInternalServerError
            },
            upscanResponseModel => {
              val optErrorCode: Option[String] = request.session.get(SessionKeys.errorCodeFromUpscan)
              val optFailureFromUpscan: Option[String] = request.session.get(SessionKeys.failureMessageFromUpscan)
              if (optErrorCode.isEmpty && optFailureFromUpscan.isEmpty) {
                Ok(uploadFirstDocumentPage(upscanResponseModel, formProvider, nextPageIfNoUpload.url))
              } else if (optErrorCode.isDefined && optFailureFromUpscan.isEmpty) {
                val localisedFailureReason = UpscanMessageHelper.getUploadFailureMessage(optErrorCode.get)
                val formWithErrors = UploadDocumentForm.form.withError(FormError("file", localisedFailureReason))
                BadRequest(uploadFirstDocumentPage(upscanResponseModel, formWithErrors, nextPageIfNoUpload.url))
                  .removingFromSession(SessionKeys.errorCodeFromUpscan)
              } else {
                val formWithErrors = UploadDocumentForm.form.withError(FormError("file", optFailureFromUpscan.get))
                BadRequest(uploadFirstDocumentPage(upscanResponseModel, formWithErrors, nextPageIfNoUpload.url))
                  .removingFromSession(SessionKeys.failureMessageFromUpscan)
              }
            }
          )
        )
      }
    }
  }

  def onPageLoadForUploadComplete(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      Ok("successful upload page")
    }
  }

  def onSubmitForNoJSFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val nextPageIfNoUpload = navigation.nextPage(EvidencePage, mode)
      val postAction = controllers.routes.OtherReasonController.onSubmitForNoJSFileUpload()
      UploadDocumentForm.form.bindFromRequest().fold(
        formWithErrors => {
          logger.debug("[OtherReasonController][onSubmitForNoJSFileUpload] - User did not upload a file")
          BadRequest(uploadFirstDocumentPage(formWithErrors, postAction, nextPageIfNoUpload.url))
        },
        _ => {
          //TODO: need to change this to upload the file and do processing
          Redirect(navigation.nextPage(EvidencePage, mode))
        }
      )

    }
  }
}
