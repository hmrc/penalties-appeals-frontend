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

import config.featureSwitches.{FeatureSwitching, NonJSRouting}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.WhenDidBecomeUnableForm
import forms.WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm
import forms.upscan.{RemoveFileForm, UploadDocumentForm, UploadListForm}
import helpers.{FormProviderHelper, UpscanMessageHelper}
import models.pages._
import models.upload.{UploadJourney, UploadStatusEnum}
import models.upload.UploadStatusEnum.READY
import models.{CheckMode, Mode, NormalMode}
import navigation.Navigation
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.UploadJourneyRepository
import services.upscan.UpscanService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.other._
import views.html.reasonableExcuseJourneys.other.noJs.{UploadAnotherDocumentPage, UploadFirstDocumentPage, UploadListPage, UploadTakingLongerThanExpectedPage}
import viewtils.{EvidenceFileUploadsHelper, RadioOptionHelper}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherReasonController @Inject()(whenDidBecomeUnablePage: WhenDidBecomeUnablePage,
                                      whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage,
                                      uploadEvidencePage: UploadEvidencePage,
                                      uploadFirstDocumentPage: UploadFirstDocumentPage,
                                      uploadTakingLongerThanExpectedPage: UploadTakingLongerThanExpectedPage,
                                      uploadAnotherDocumentPage: UploadAnotherDocumentPage,
                                      uploadListPage: UploadListPage,
                                      navigation: Navigation,
                                      upscanService: UpscanService,
                                      featureSwitching: FeatureSwitching,
                                      evidenceFileUploadsHelper: EvidenceFileUploadsHelper,
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
      for {
        previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
      } yield {
        if (request.cookies.get("jsenabled").isEmpty || featureSwitching.isEnabled(NonJSRouting)) {
          mode match {
            case NormalMode => Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
            case CheckMode => if (previousUploadsState.exists(_.count(_.fileStatus == READY) > 0)) {
              Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode))
            } else {
              Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
            }
          }
        } else {
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
      if (request.cookies.get("jsenabled").isDefined && !featureSwitching.isEnabled(NonJSRouting)) {
        Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode)))
      } else {
        val nextPageIfNoUpload = navigation.nextPage(EvidencePage, mode)
        val formProvider = UploadDocumentForm.form
        upscanService.initiateSynchronousCallToUpscan(request.session.get(SessionKeys.journeyId).get, isAddingAnotherDocument = false, mode).map(
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

  def onPageLoadForAnotherFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request =>
      val formProvider = UploadDocumentForm.form
      upscanService.initiateSynchronousCallToUpscan(request.session.get(SessionKeys.journeyId).get, isAddingAnotherDocument = true, mode).map(
        _.fold(
          error => {
            logger.error("[OtherReasonController][onPageLoadForAnotherFileUpload] - Received error back from initiate request rendering ISE.")
            errorHandler.showInternalServerError
          },
          upscanResponseModel => {
            val optErrorCode: Option[String] = request.session.get(SessionKeys.errorCodeFromUpscan)
            val optFailureFromUpscan: Option[String] = request.session.get(SessionKeys.failureMessageFromUpscan)
            val fileListPage = navigation.nextPage(UploadAnotherDocumentPage, mode)
            if (optErrorCode.isEmpty && optFailureFromUpscan.isEmpty) {
              Ok(uploadAnotherDocumentPage(upscanResponseModel, formProvider, fileListPage.url))

            } else if (optErrorCode.isDefined && optFailureFromUpscan.isEmpty) {
              val localisedFailureReason = UpscanMessageHelper.getUploadFailureMessage(optErrorCode.get)
              val formWithErrors = UploadDocumentForm.form.withError(FormError("file", localisedFailureReason))
              BadRequest(uploadAnotherDocumentPage(upscanResponseModel, formWithErrors, fileListPage.url))
                .removingFromSession(SessionKeys.errorCodeFromUpscan)
            } else {
              val formWithErrors = UploadDocumentForm.form.withError(FormError("file", optFailureFromUpscan.get))
              BadRequest(uploadAnotherDocumentPage(upscanResponseModel, formWithErrors, fileListPage.url))
                .removingFromSession(SessionKeys.failureMessageFromUpscan)
            }
          }
        )
      )
  }

  def onPageLoadForUploadComplete(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        UploadListForm.youHaveUploadedForm,
        SessionKeys.nextFileUpload
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.OtherReasonController.onSubmitForUploadComplete(mode)
      for {
        previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
      } yield {
        if(previousUploadsState.isEmpty || previousUploadsState.exists(!_.exists(_.fileStatus == UploadStatusEnum.READY))) {
          Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
        } else {
          val uploadedFileNames: Seq[UploadJourney] = previousUploadsState.fold[Seq[UploadJourney]](Seq.empty)(_.filter(_.fileStatus == UploadStatusEnum.READY))
          val uploadRows: Seq[Html] = evidenceFileUploadsHelper.displayContentForFileUploads(uploadedFileNames.zipWithIndex, mode)
          Ok(uploadListPage(formProvider, radioOptionsToRender, postAction, uploadRows))
        }
      }
    }
  }

  def onSubmitForUploadComplete(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      UploadListForm.youHaveUploadedForm.bindFromRequest.fold(
        formHasErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formHasErrors)
          val postAction = controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode)
          for {
            previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
          } yield {
            val uploadedFileNames: Seq[UploadJourney] = previousUploadsState.fold[Seq[UploadJourney]](Seq.empty)(_.filter(_.fileStatus == UploadStatusEnum.READY))
            val uploadRows: Seq[Html] = evidenceFileUploadsHelper.displayContentForFileUploads(uploadedFileNames.zipWithIndex, mode)
            if (uploadRows.length < 5) {
              BadRequest(uploadListPage(formHasErrors, radioOptionsToRender, postAction, uploadRows))
            } else {
              Redirect(navigation.nextPage(FileListPage, mode))
            }
          }
        },
        nextFileUpload => {
          logger.debug(
            s"[YouHaveUploadedFilesController][onSubmitForNextFileUpload] - Adding '$nextFileUpload' to session under key: ${SessionKeys.nextFileUpload}")
          Future(Redirect(navigation.nextPage(FileListPage, mode, Some(nextFileUpload)))
            .addingToSession((SessionKeys.nextFileUpload, nextFileUpload)))
        }
      )
    }
  }

  def removeFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      if (request.cookies.get("jsenabled").isDefined && !featureSwitching.isEnabled(NonJSRouting)) {
        logger.debug(s"[OtherReasonController][removeFileUpload] - Redirecting to JS version - feature switch on: ${featureSwitching.isEnabled(NonJSRouting)} and cookie defined: ${request.cookies.get("jsenabled").isDefined}")
        Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode)))
      } else {
        val journeyId = request.session.get(SessionKeys.journeyId).get
        RemoveFileForm.form.bindFromRequest.fold(
          error => {
            logger.error("[OtherReasonController][removeFileUpload] - Tried to remove file but fileReference was not in the request")
            logger.debug(s"[OtherReasonController][removeFileUpload] - Form errors: ${error.errors}")
            Future(errorHandler.showInternalServerError)
          },
          fileReference => {
            upscanService.removeFileFromJourney(journeyId, fileReference).flatMap(_ => {
              upscanService.getAmountOfFilesUploadedForJourney(journeyId).map(
                amountOfFiles => {
                  if (amountOfFiles == 0) {
                    logger.debug("[OtherReasonController][removeFileUpload] - No files left in journey - redirecting to first document upload page")
                    Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
                  } else {
                    Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode))
                  }
                }
              )
            })
          }
        )
      }
    }
  }

  def onPageLoadForUploadTakingLongerThanExpected(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val postAction = controllers.routes.OtherReasonController.onSubmitForUploadTakingLongerThanExpected(mode)
      Ok(uploadTakingLongerThanExpectedPage(postAction))
    }
  }

  def onSubmitForUploadTakingLongerThanExpected(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      val isAddingAnotherDocument = request.session.get(SessionKeys.isAddingAnotherDocument).fold(false)(_ == "true")
      val timeoutForCheckingStatus = System.nanoTime() + (appConfig.upscanStatusCheckTimeout * 1000000000L)
      upscanService.waitForStatus(request.session.get(SessionKeys.journeyId).get,
        request.session.get(SessionKeys.fileReference).get,
        timeoutForCheckingStatus,
        mode,
        isAddingAnotherDocument,
        {
          (optFailureDetails, errorMessage) => {
            if (errorMessage.isDefined) {
              val failureReason = UpscanMessageHelper.getLocalisedFailureMessageForFailure(optFailureDetails.get.failureReason)
              if (isAddingAnotherDocument) {
                Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(mode))
                  .addingToSession(SessionKeys.failureMessageFromUpscan -> failureReason)
                  .removingFromSession(SessionKeys.isAddingAnotherDocument, SessionKeys.fileReference))
              } else {
                Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
                  .addingToSession(SessionKeys.failureMessageFromUpscan -> failureReason)
                  .removingFromSession(SessionKeys.isAddingAnotherDocument, SessionKeys.fileReference))
              }
            } else {
              Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode))
                .removingFromSession(SessionKeys.isAddingAnotherDocument, SessionKeys.fileReference))
            }
          }
        })
    }
  }
}
