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

import config.featureSwitches.{FeatureSwitching, NonJSRouting}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.WhenDidBecomeUnableForm
import forms.WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm
import forms.upscan.{RemoveFileForm, UploadDocumentForm, UploadEvidenceQuestionForm, UploadListForm}
import helpers.{FormProviderHelper, UpscanMessageHelper}
import models.pages.{UploadEvidenceQuestionPage, _}
import models.upload.UploadStatusEnum.READY
import models.upload.{UploadJourney, UploadStatusEnum}
import models.{CheckMode, Mode}
import navigation.Navigation
import play.api.Configuration
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.UploadJourneyRepository
import services.SessionService
import services.upscan.UpscanService
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.errors.ServiceUnavailablePage
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
                                      uploadEvidenceQuestionPage: UploadEvidenceQuestionPage,
                                      navigation: Navigation,
                                      upscanService: UpscanService,
                                      evidenceFileUploadsHelper: EvidenceFileUploadsHelper,
                                      uploadJourneyRepository: UploadJourneyRepository,
                                      serviceUnavailablePage: ServiceUnavailablePage,
                                      sessionService: SessionService)
                                     (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      dataRetrieval: DataRetrievalAction,
                                      appConfig: AppConfig,
                                      val config: Configuration,
                                      errorHandler: ErrorHandler,
                                      mcc: MessagesControllerComponents,
                                      ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForWhenDidBecomeUnable(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[LocalDate] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsDate(
        WhenDidBecomeUnableForm.whenDidBecomeUnableForm(),
        SessionKeys.whenDidBecomeUnable,
        userRequest.answers
      )
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(mode)
      Ok(whenDidBecomeUnablePage(formProvider, postAction, pageMode(WhenDidBecomeUnablePage, mode)))
    }
  }

  def onSubmitForWhenDidBecomeUnable(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhenDidBecomeUnable(mode)
      WhenDidBecomeUnableForm.whenDidBecomeUnableForm().bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(whenDidBecomeUnablePage(formWithErrors, postAction, pageMode(WhenDidBecomeUnablePage, mode))))
        },
        dateUnable => {
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable]" +
            s" - Adding '$dateUnable' to session under key: ${SessionKeys.whenDidBecomeUnable}")
          val updatedAnswers = userRequest.answers.setAnswer[LocalDate](SessionKeys.whenDidBecomeUnable, dateUnable)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhenDidBecomeUnablePage, mode, Some(dateUnable.toString))))
        }
      )
    }
  }

  def onPageLoadForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(whyReturnSubmittedLateForm(), SessionKeys.whyReturnSubmittedLate, userRequest.answers)
      val postAction = controllers.routes.OtherReasonController.onSubmitForWhyReturnSubmittedLate(mode)
      Ok(whyReturnSubmittedLatePage(formProvider, postAction, pageMode(WhyWasReturnSubmittedLatePage, mode)))
    }
  }

  def onSubmitForWhyReturnSubmittedLate(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      whyReturnSubmittedLateForm.bindFromRequest().fold(
        formWithErrors => {
          val postAction = controllers.routes.OtherReasonController.onSubmitForWhyReturnSubmittedLate(mode)
          Future(BadRequest(whyReturnSubmittedLatePage(formWithErrors, postAction, pageMode(WhyWasReturnSubmittedLatePage, mode))))
        },
        whyReturnSubmittedLateReason => {
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable]" +
            s" - Adding '$whyReturnSubmittedLateReason' to session under key: ${SessionKeys.whyReturnSubmittedLate}")
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.whyReturnSubmittedLate, whyReturnSubmittedLateReason)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(navigation.nextPage(WhyWasReturnSubmittedLatePage, mode, Some(whyReturnSubmittedLateReason))))
        }
      )
    }
  }

  def onPageLoadForUploadEvidence(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      for {
        previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      } yield {
        if (userRequest.cookies.get("jsenabled").isEmpty || isEnabled(NonJSRouting)) {
          val hasReadyUploads: Boolean = previousUploadsState.exists(_.count(_.fileStatus == READY) > 0)
          if (hasReadyUploads) {
            val call = controllers.routes.OtherReasonController.onPageLoadForUploadComplete(mode)
            if (mode == CheckMode && userRequest.session.get(SessionKeys.originatingChangePage).isEmpty) Redirect(call).addingToSession(SessionKeys.originatingChangePage -> FileListPage.toString) else Redirect(call)
          } else {
            val call = controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode)
            if (mode == CheckMode && userRequest.session.get(SessionKeys.originatingChangePage).isEmpty) Redirect(call).addingToSession(SessionKeys.originatingChangePage -> FileListPage.toString) else Redirect(call)
          }
        } else {
          val previousUploads = previousUploadsState.fold("[]")(previousUploads => {
            val previousUploadsWithoutDownloadURLAndUploadFields = previousUploads.map(_.copy(downloadUrl = None, uploadFields = None))
            Json.stringify(Json.toJson(previousUploadsWithoutDownloadURLAndUploadFields))
          })
          val initiateNextUploadUrl = controllers.routes.UpscanController.initiateCallToUpscan(userRequest.session.get(SessionKeys.journeyId).get)
          val getStatusUrl = controllers.routes.UpscanController.getStatusOfFileUpload(userRequest.session.get(SessionKeys.journeyId).get, _)
          val removeFileUrl = controllers.routes.UpscanController.removeFile(userRequest.session.get(SessionKeys.journeyId).get, _)
          val removeFilePageUrl = controllers.routes.RemoveFileController.onPageLoad(_, true, mode)
          val postAction = navigation.nextPage(EvidencePage, mode)
          val getDuplicatesUrl = controllers.routes.UpscanController.getDuplicateFiles(userRequest.session.get(SessionKeys.journeyId).get)
          val getErrorServiceUrl = controllers.routes.ProblemWithServiceController.onPageLoad()
          val call = uploadEvidencePage(postAction, initiateNextUploadUrl, getStatusUrl, removeFileUrl, removeFilePageUrl, previousUploads, getDuplicatesUrl, getErrorServiceUrl, pageMode(EvidencePage, mode))
          if (mode == CheckMode && userRequest.session.get(SessionKeys.originatingChangePage).isEmpty) Ok(call).addingToSession(SessionKeys.originatingChangePage -> EvidencePage.toString) else Ok(call)
        }
      }
    }
  }

  def onPageLoadForFirstFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      if (userRequest.cookies.get("jsenabled").isDefined && !isEnabled(NonJSRouting)) {
        Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode)))
      } else {
        val formProvider = UploadDocumentForm.form
        upscanService.initiateSynchronousCallToUpscan(userRequest.session.get(SessionKeys.journeyId).get, isAddingAnotherDocument = false, mode).map(
          _.fold(
            _ => {
              logger.error("[OtherReasonController][onPageLoadForFirstFileUpload] - Received error back from initiate request rendering ISE.")
              InternalServerError(serviceUnavailablePage())
            },
            upscanResponseModel => {
              val optErrorCode: Option[String] = userRequest.session.get(SessionKeys.errorCodeFromUpscan)
              val optFailureFromUpscan: Option[String] = userRequest.session.get(SessionKeys.failureMessageFromUpscan)
              val isJsEnabled = userRequest.cookies.get("jsenabled").isDefined
              if (optErrorCode.isEmpty && optFailureFromUpscan.isEmpty) {
                Ok(uploadFirstDocumentPage(upscanResponseModel, formProvider, pageMode(UploadFirstDocumentPage, mode)))
              } else if (optErrorCode.isDefined && optFailureFromUpscan.isEmpty) {
                val localisedFailureReason = UpscanMessageHelper.getUploadFailureMessage(optErrorCode.get, isJsEnabled)
                val formWithErrors = UploadDocumentForm.form.withError(FormError("file", localisedFailureReason))
                BadRequest(uploadFirstDocumentPage(upscanResponseModel, formWithErrors, pageMode(UploadFirstDocumentPage, mode)))
                  .removingFromSession(SessionKeys.errorCodeFromUpscan)
              } else {
                val formWithErrors = UploadDocumentForm.form.withError(FormError("file", optFailureFromUpscan.get))
                BadRequest(uploadFirstDocumentPage(upscanResponseModel, formWithErrors, pageMode(UploadFirstDocumentPage, mode)))
                  .removingFromSession(SessionKeys.failureMessageFromUpscan)
              }
            }
          )
        )
      }
    }
  }

  def onPageLoadForAnotherFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest =>
      val formProvider = UploadDocumentForm.form
      upscanService.initiateSynchronousCallToUpscan(userRequest.session.get(SessionKeys.journeyId).get, isAddingAnotherDocument = true, mode).map(
        _.fold(
          _ => {
            logger.error("[OtherReasonController][onPageLoadForAnotherFileUpload] - Received error back from initiate request rendering ISE.")
            InternalServerError(serviceUnavailablePage())
          },
          upscanResponseModel => {
            val optErrorCode: Option[String] = userRequest.session.get(SessionKeys.errorCodeFromUpscan)
            val optFailureFromUpscan: Option[String] = userRequest.session.get(SessionKeys.failureMessageFromUpscan)
            val fileListPage = navigation.nextPage(UploadAnotherDocumentPage, mode)
            val isJsEnabled = userRequest.cookies.get("jsenabled").isDefined
            if (optErrorCode.isEmpty && optFailureFromUpscan.isEmpty) {
              Ok(uploadAnotherDocumentPage(upscanResponseModel, formProvider, fileListPage.url, pageMode(UploadAnotherDocumentPage, mode)))

            } else if (optErrorCode.isDefined && optFailureFromUpscan.isEmpty) {
              val localisedFailureReason = UpscanMessageHelper.getUploadFailureMessage(optErrorCode.get, isJsEnabled)
              val formWithErrors = UploadDocumentForm.form.withError(FormError("file", localisedFailureReason))
              BadRequest(uploadAnotherDocumentPage(upscanResponseModel, formWithErrors, fileListPage.url, pageMode(UploadAnotherDocumentPage, mode)))
                .removingFromSession(SessionKeys.errorCodeFromUpscan)
            } else {
              val formWithErrors = UploadDocumentForm.form.withError(FormError("file", optFailureFromUpscan.get))
              BadRequest(uploadAnotherDocumentPage(upscanResponseModel, formWithErrors, fileListPage.url, pageMode(UploadAnotherDocumentPage, mode)))
                .removingFromSession(SessionKeys.failureMessageFromUpscan)
            }
          }
        )
      )
  }

  def onPageLoadForUploadComplete(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val formProvider = UploadListForm.youHaveUploadedForm
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.OtherReasonController.onSubmitForUploadComplete(mode)
      for {
        previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
      } yield {
        if (previousUploadsState.isEmpty ||
          previousUploadsState.exists(!_.exists(file => file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))) {
          Redirect(controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(mode))
        } else {
          val uploadedFileNames: Seq[UploadJourney] = previousUploadsState.fold[Seq[UploadJourney]](Seq.empty)(_.filter(file =>
            file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
          val optDuplicateFiles: Option[Html] = evidenceFileUploadsHelper.getInsetTextForUploads(uploadedFileNames.zipWithIndex)
          val uploadRows: Seq[Html] = evidenceFileUploadsHelper.displayContentForFileUploads(uploadedFileNames.zipWithIndex, mode)
          Ok(uploadListPage(formProvider, radioOptionsToRender, postAction, uploadRows, optDuplicateFiles, pageMode(FileListPage, mode)))
        }
      }
    }
  }

  def onSubmitForUploadComplete(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      UploadListForm.youHaveUploadedForm.bindFromRequest.fold(
        formHasErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formHasErrors)
          val postAction = controllers.routes.OtherReasonController.onSubmitForUploadComplete(mode)
          for {
            previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(userRequest.session.get(SessionKeys.journeyId))
          } yield {
            val uploadedFileNames: Seq[UploadJourney] = previousUploadsState.fold[Seq[UploadJourney]](Seq.empty)(_.filter(file =>
              file.fileStatus == UploadStatusEnum.READY || file.fileStatus == UploadStatusEnum.DUPLICATE))
            val optDuplicateFiles: Option[Html] = evidenceFileUploadsHelper.getInsetTextForUploads(uploadedFileNames.zipWithIndex)
            val uploadRows: Seq[Html] = evidenceFileUploadsHelper.displayContentForFileUploads(uploadedFileNames.zipWithIndex, mode)
            if (uploadRows.length < 5) {
              BadRequest(uploadListPage(formHasErrors, radioOptionsToRender, postAction, uploadRows, optDuplicateFiles, pageMode(FileListPage, mode)))
            } else {
              Redirect(navigation.nextPage(FileListPage, mode))
            }
          }
        },
        nextFileUpload => {
          Future(Redirect(navigation.nextPage(FileListPage, mode, Some(nextFileUpload))))
        }
      )
    }
  }

  def removeFileUpload(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      if (userRequest.cookies.get("jsenabled").isDefined && !isEnabled(NonJSRouting)) {
        logger.debug(s"[OtherReasonController][removeFileUpload] - Redirecting to JS version - feature switch on: ${isEnabled(NonJSRouting)} and cookie defined: ${userRequest.cookies.get("jsenabled").isDefined}")
        Future(Redirect(controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(mode)))
      } else {
        val journeyId = userRequest.session.get(SessionKeys.journeyId).get
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
                    logger.debug("[OtherReasonController][removeFileUpload] - More documents exist - reloading the upload list page")
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

  def onPageLoadForUploadTakingLongerThanExpected(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val postAction = controllers.routes.OtherReasonController.onSubmitForUploadTakingLongerThanExpected(mode)
      Ok(uploadTakingLongerThanExpectedPage(postAction))
    }
  }

  def onSubmitForUploadTakingLongerThanExpected(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      val isAddingAnotherDocument = userRequest.session.get(SessionKeys.isAddingAnotherDocument).fold(false)(_ == "true")
      val timeoutForCheckingStatus = System.nanoTime() + (appConfig.upscanStatusCheckTimeout * 1000000000L)
      val isJsEnabled = userRequest.cookies.get("jsenabled").isDefined
      upscanService.waitForStatus(userRequest.session.get(SessionKeys.journeyId).get,
        userRequest.session.get(SessionKeys.fileReference).get,
        timeoutForCheckingStatus,
        mode,
        isAddingAnotherDocument,
        {
          (optFailureDetails, errorMessage) => {
            if (errorMessage.isDefined) {
              val failureReason = UpscanMessageHelper.getLocalisedFailureMessageForFailure(optFailureDetails.get.failureReason, isJsEnabled)
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

  def onPageLoadForUploadEvidenceQuestion(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        UploadEvidenceQuestionForm.uploadEvidenceQuestionForm,
        SessionKeys.isUploadEvidence,
        userRequest.answers
      )
      val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val postAction = controllers.routes.OtherReasonController.onSubmitForUploadEvidenceQuestion(mode)
      Ok(uploadEvidenceQuestionPage(formProvider, radioOptionsToRender, postAction, pageMode(UploadEvidenceQuestionPage, mode)))
    }
  }

  def onSubmitForUploadEvidenceQuestion(mode: Mode): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      UploadEvidenceQuestionForm.uploadEvidenceQuestionForm.bindFromRequest().fold(
        formWithErrors => {
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formWithErrors)
          val postAction = controllers.routes.OtherReasonController.onSubmitForUploadEvidenceQuestion(mode)
          Future(BadRequest(uploadEvidenceQuestionPage(formWithErrors, radioOptionsToRender, postAction, pageMode(UploadEvidenceQuestionPage, mode))))
        },
        isUploadEvidenceQuestion => {
          logger.debug(
            s"[OtherReasonController][onSubmitForUploadEvidenceQuestion] - Adding '$isUploadEvidenceQuestion' to session under key: ${SessionKeys.isUploadEvidence}")
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.isUploadEvidence, isUploadEvidenceQuestion)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(UploadEvidenceQuestionPage, mode, Some(isUploadEvidenceQuestion)))
          }
        }
      )
    }
  }
}
