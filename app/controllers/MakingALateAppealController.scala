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

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRequiredAction}
import forms.MakingALateAppealForm
import helpers.{DateTimeHelper, FormProviderHelper}
import models.{Mode, NormalMode, UserRequest}
import models.pages.{MakingALateAppealPage, PageMode}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.MakingALateAppealPage

import java.time.LocalDate
import javax.inject.Inject

class MakingALateAppealController @Inject()(makingALateAppealPage: MakingALateAppealPage,
                                            dateTimeHelper: DateTimeHelper)
                                           (implicit mcc: MessagesControllerComponents,
                                            appConfig: AppConfig,
                                            authorise: AuthPredicate,
                                            dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(MakingALateAppealPage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        MakingALateAppealForm.makingALateAppealForm(), SessionKeys.lateAppealReason)
      Ok(makingALateAppealPage(formProvider, getHeadingAndTitle, pageMode(NormalMode)))
    }
  }

  def getHeadingAndTitle()(implicit userRequest: UserRequest[_], messages: Messages): String = {
    userRequest.session.get(SessionKeys.doYouWantToAppealBothPenalties) match {
      case Some("yes") => {
        val dateOfFirstComms = LocalDate.parse(userRequest.session.get(SessionKeys.firstPenaltyCommunicationDate).get)
        val dateOfSecondComms = LocalDate.parse(userRequest.session.get(SessionKeys.secondPenaltyCommunicationDate).get)
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

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      MakingALateAppealForm.makingALateAppealForm().bindFromRequest().fold(
        formWithErrors => BadRequest(makingALateAppealPage(formWithErrors, getHeadingAndTitle, pageMode(NormalMode))),
        lateAppealReason => {
          Redirect(routes.CheckYourAnswersController.onPageLoad())
            .addingToSession(SessionKeys.lateAppealReason -> lateAppealReason)
        }
      )
    }
  }
}
