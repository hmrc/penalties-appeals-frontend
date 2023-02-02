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
import forms.MakingALateAppealForm
import helpers.{DateTimeHelper, FormProviderHelper}
import models.pages.{MakingALateAppealPage, PageMode}
import models.{Mode, NormalMode, UserRequest}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.MakingALateAppealPage

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MakingALateAppealController @Inject()(makingALateAppealPage: MakingALateAppealPage,
                                            dateTimeHelper: DateTimeHelper,
                                            sessionService: SessionService)
                                           (implicit mcc: MessagesControllerComponents,
                                            appConfig: AppConfig,
                                            authorise: AuthPredicate,
                                            dataRetrieval: DataRetrievalAction,
                                            dataRequired: DataRequiredAction,
                                            ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(MakingALateAppealPage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        MakingALateAppealForm.makingALateAppealForm(), SessionKeys.lateAppealReason, userRequest.answers)
      Ok(makingALateAppealPage(formProvider, getHeadingAndTitle(), pageMode(NormalMode)))
    }
  }

  def getHeadingAndTitle()(implicit userRequest: UserRequest[_], messages: Messages): String = {
    userRequest.answers.getAnswer[String](SessionKeys.doYouWantToAppealBothPenalties) match {
      case Some("yes") => {
        val dateOfFirstComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.firstPenaltyCommunicationDate).get
        val dateOfSecondComms = userRequest.answers.getAnswer[LocalDate](SessionKeys.secondPenaltyCommunicationDate).get
        if(dateOfFirstComms.isBefore(dateTimeHelper.dateNow.minusDays(appConfig.daysRequiredForLateAppeal)) &&
        dateOfSecondComms.isBefore(dateTimeHelper.dateNow.minusDays(appConfig.daysRequiredForLateAppeal))) {
          messages("makingALateAppeal.headingAndTitle.multi")
        } else {
          messages("makingALateAppeal.headingAndTitle.first")
        }
      }
      case Some("no") | None => messages("makingALateAppeal.headingAndTitle")
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      MakingALateAppealForm.makingALateAppealForm().bindFromRequest().fold(
        formWithErrors => Future(BadRequest(makingALateAppealPage(formWithErrors, getHeadingAndTitle(), pageMode(NormalMode)))),
        lateAppealReason => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.lateAppealReason, lateAppealReason)
          sessionService.updateAnswers(updatedAnswers).map(_ => Redirect(routes.CheckYourAnswersController.onPageLoad()))
        }
      )
    }
  }
}
