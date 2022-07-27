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
import forms.PenaltySelectionForm
import helpers.FormProviderHelper
import models.{Mode, PenaltyTypeEnum}
import models.pages._
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.{AppealCoverBothPenaltiesPage, AppealSinglePenaltyPage, PenaltySelectionPage}
import viewtils.RadioOptionHelper

import javax.inject.Inject

class PenaltySelectionController @Inject()(penaltySelectionPage: PenaltySelectionPage,
                                           appealCoverBothPenaltiesPage: AppealCoverBothPenaltiesPage,
                                           appealSinglePenaltyPage: AppealSinglePenaltyPage,
                                           navigation: Navigation)
                                          (implicit mcc: MessagesControllerComponents,
                                           appConfig: AppConfig,
                                           authorise: AuthPredicate,
                                           dataRequired: DataRequiredAction) extends FrontendController(mcc) with I18nSupport {

  val pageMode: (Page, Mode) => PageMode = (page: Page, mode: Mode) => PageMode(page, mode)

  def onPageLoadForPenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
        PenaltySelectionForm.doYouWantToAppealBothPenalties,
        SessionKeys.doYouWantToAppealBothPenalties
      )
      val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
      val firstPenalty = userRequest.session.get(SessionKeys.firstPenaltyAmount).get
      val secondPenalty = userRequest.session.get(SessionKeys.secondPenaltyAmount).get
      Ok(penaltySelectionPage(formProvider, radioOptions, firstPenalty, secondPenalty, pageMode(PenaltySelectionPage, mode)))
    }
  }

  def onSubmitForPenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      PenaltySelectionForm.doYouWantToAppealBothPenalties.bindFromRequest.fold(
        errors => {
          val radioOptions = RadioOptionHelper.yesNoRadioOptions(errors)
          val firstPenalty = userRequest.session.get(SessionKeys.firstPenaltyAmount).get
          val secondPenalty = userRequest.session.get(SessionKeys.secondPenaltyAmount).get
          BadRequest(penaltySelectionPage(errors, radioOptions, firstPenalty, secondPenalty, pageMode(PenaltySelectionPage, mode)))
        },
        answer => {
          Redirect(navigation.nextPage(PenaltySelectionPage, mode, Some(answer)))
            .addingToSession(SessionKeys.doYouWantToAppealBothPenalties -> answer)
        }
      )
    }
  }

  def onPageLoadForSinglePenaltySelection(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit userRequest => {
      val nextPageUrl: String = navigation.nextPage(AppealSinglePenaltyPage, mode).url
      val originalAppealPenalty = userRequest.session.get(SessionKeys.appealType).get
      val isLPP2 = originalAppealPenalty.equals(PenaltyTypeEnum.Additional.toString)
      val penaltyAmount = {
        if (isLPP2)
          userRequest.session.get(SessionKeys.secondPenaltyAmount).get
        else
          userRequest.session.get(SessionKeys.firstPenaltyAmount).get
      }
      Ok(appealSinglePenaltyPage(pageMode(AppealSinglePenaltyPage, mode), nextPageUrl, penaltyAmount, isLPP2))
    }
  }

  def onPageLoadForAppealCoverBothPenalties(mode: Mode): Action[AnyContent] = (authorise andThen dataRequired) {
    implicit request => {
      val nextPageUrl: String = navigation.nextPage(AppealCoverBothPenaltiesPage, mode).url
      Ok(appealCoverBothPenaltiesPage(pageMode(AppealCoverBothPenaltiesPage, mode), nextPageUrl))
    }
  }
}
