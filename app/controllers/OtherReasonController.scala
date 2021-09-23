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

import java.time.LocalDate
import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.WhyReturnSubmittedLateForm.whyReturnSubmittedLateForm
import forms.{UploadEvidenceForm, WhenDidBecomeUnableForm}
import helpers.FormProviderHelper

import javax.inject.Inject
import models.Mode
import models.pages.{EvidencePage, WhenDidBecomeUnablePage, WhyWasReturnSubmittedLatePage}
import models.upload.UploadJourney
import navigation.Navigation
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UploadJourneyRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.SessionKeys
import views.html.reasonableExcuseJourneys.other._

import scala.concurrent.ExecutionContext

class OtherReasonController @Inject()(whenDidBecomeUnablePage: WhenDidBecomeUnablePage,
                                      whyReturnSubmittedLatePage: WhyReturnSubmittedLatePage,
                                      uploadEvidencePage: UploadEvidencePage,
                                      navigation: Navigation,
                                      uploadJourneyRepository: UploadJourneyRepository)
                                     (implicit authorise: AuthPredicate,
                                      dataRequired: DataRequiredAction,
                                      appConfig: AppConfig,
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
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable] - Adding '$dateUnable' to session under key: ${SessionKeys.whenDidBecomeUnable}")
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
          logger.debug(s"[OtherReasonController][onSubmitForWhenDidBecomeUnable] - Adding '$whyReturnSubmittedLateReason' to session under key: ${SessionKeys.whyReturnSubmittedLate}")
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
        val previousUploads = previousUploadsState.fold("[]")(previousUploads => Json.stringify(Json.toJson(previousUploads)))
        val initiateNextUploadUrl = controllers.routes.UpscanController.initiateCallToUpscan(request.session.get(SessionKeys.journeyId).get)
        val getStatusUrl = controllers.routes.UpscanController.getStatusOfFileUpload(request.session.get(SessionKeys.journeyId).get, _)
        val formProvider: Form[Option[String]] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsOptionString(
          UploadEvidenceForm.uploadEvidenceForm,
          SessionKeys.evidenceFileName
        )
        val postAction = controllers.routes.OtherReasonController.onSubmitForUploadEvidence(mode)
        Ok(uploadEvidencePage(formProvider, postAction, initiateNextUploadUrl, getStatusUrl, previousUploads))
      }
    }
  }

  def onSubmitForUploadEvidence(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired).async {
    implicit request => {
      for {
        previousUploadsState <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId))
        //TODO: Temp solution to make AT's pass, CYA needs to be driven from Repository and not session keys
        uploads <- uploadJourneyRepository.getUploadsForJourney(request.session.get(SessionKeys.journeyId)).map(_.map(_.map(_.uploadDetails.map(_.fileName))
          .collect { case Some(x) => x }))
      } yield {
        val previousUploads = previousUploadsState.fold("[]")(previousUploads => Json.stringify(Json.toJson(previousUploads)))
        val postAction = controllers.routes.OtherReasonController.onSubmitForUploadEvidence(mode)
        val initiateNextUploadUrl = controllers.routes.UpscanController.initiateCallToUpscan(request.session.get(SessionKeys.journeyId).get)
        val getStatusUrl = controllers.routes.UpscanController.getStatusOfFileUpload(request.session.get(SessionKeys.journeyId).get, _)
        //TODO: Temp solution to make AT's pass, CYA needs to be driven from Repository and not session keys
        uploads.fold(
          Redirect(navigation.nextPage(EvidencePage, mode, None)).addingToSession((SessionKeys.evidenceFileName, ""))
        )(
          fileNames =>
            Redirect(navigation.nextPage(EvidencePage, mode, fileNames.reverse.headOption))
              .addingToSession((SessionKeys.evidenceFileName, fileNames.reverse.headOption.getOrElse("")))
        )
      }
    }
  }
}
